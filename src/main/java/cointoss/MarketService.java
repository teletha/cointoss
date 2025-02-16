/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.execution.LogHouse;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.orderbook.OrderBookChanges;
import cointoss.ticker.data.Liquidation;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.RetryPolicy;
import hypatia.Num;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.Observer;
import kiss.Scheduler;
import kiss.Signal;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

public abstract class MarketService implements Comparable<MarketService>, Disposable {

    /** The default max number of retry. */
    private static final int retryMax = 10;

    /** The exchange. */
    public final Exchange exchange;

    /** The market name. */
    public final String marketName;

    /** The human-readable market identifier. */
    public final String id;

    /** The formatted market identifier. */
    public final String formattedId;

    /** The execution log. */
    public final ExecutionLog log;

    /** The service disposer. */
    protected final Disposable disposer = Disposable.empty();

    /** The market configuration. */
    public final MarketSetting setting;

    /** The market specific scheduler. */
    private final Scheduler scheduler;

    /** The shared stream. */
    private Signal<Execution> executionCollector;

    /** The shared stream. */
    private Signal<Execution> executionRealtimely;

    /** The shared stream. */
    private Signal<OrderBookChanges> orderbookRealtimely;

    /** The shared stream. */
    private Signal<Liquidation> liquidationRealtimely;

    /**
     * The minimum value of the execution history that can be retrieved in a single request.
     */
    protected int executionMinRequest = 1;

    /**
     * The maximum value of the execution history that can be retrieved in a single request.
     */
    protected int executionMaxRequest = 100;

    /**
     * If this service obtains all Executions in the specified ID range for an Execution
     * request without excess or deficiency, setting the value to True can be expected
     * to result in efficient Log acquisition.
     */
    protected boolean enoughExecutionRequest;

    /**
     * Normally, when acquiring execution history, the range of acquisition is specified by ID.
     * However, if the market API only allows specifying the range by date and time, some markets
     * may only be able to retrieve data below the specified end value. In this case, it is possible
     * that all data from the specified start value has not been retrieved, so the data is divided
     * into multiple requests.
     * 
     * If the amount of data to be acquired is set to the maximum value specified by the API, there
     * will be a lot of data to discard, which is inefficient. By setting this value to 1 or more,
     * the number of data that can be acquired at one time may be greater than the API's maximum
     * value, enabling efficient data collection.
     */
    protected int executionRequestCoefficient = 1;

    /**
     * @param exchange
     * @param marketName
     * @param setting
     */
    protected MarketService(Exchange exchange, String marketName, MarketSetting setting) {
        this.exchange = Objects.requireNonNull(exchange);
        this.marketName = Objects.requireNonNull(marketName);
        String normalized = marketName.replaceAll("_", "").toUpperCase();
        this.id = exchange + " " + normalized;
        this.formattedId = String.format("%-10s %s", exchange, normalized);
        this.setting = setting;
        this.scheduler = new Scheduler();
        this.log = createExecutionLog();
    }

    /**
     * Create {@link ExecutionLog} for this service.
     * 
     * @return
     */
    protected ExecutionLog createExecutionLog() {
        return new ExecutionLog(this);
    }

    /**
     * Monitor executions from snapshot and realtime.
     * 
     * @return
     */
    public synchronized Signal<Execution> executions() {
        if (executionCollector == null) {
            executionCollector = new Signal<Execution>((observer, disposer) -> {
                BufferFromRestToRealtime buffer = new BufferFromRestToRealtime(observer::error);

                // If you connect to the real-time API first, two errors may occur at the same time
                // for the real-time API and the REST API (because the real-time API is
                // asynchronous).
                // In that case, there is a possibility that the retry operation may be hindered.
                // Therefore, the real-time API will connect after the connection of the REST API
                // is confirmed.
                // disposer.add(service.executionsRealtimely().to(buffer, observer::error));
                boolean activeRealtime = false;

                // read from REST API
                int size = executionMaxRequest * executionRequestCoefficient;
                long cacheId = log.estimateLastID();
                long startId = cacheId != -1 ? cacheId : searchInitialExecution().map(e -> e.id).to().next();
                Num coefficient = Num.ONE;
                ArrayDeque<Execution> rests = new ArrayDeque(size);
                while (!disposer.isDisposed()) {
                    rests.clear();

                    long range = Math.round(executionMaxRequest * coefficient.doubleValue());
                    executionsAfter(startId, startId + range).waitForTerminate().to(rests::add, observer::error);

                    // Since the synchronous REST API did not return an error, it can be determined
                    // that the server is operating normally, so the real-time API is also
                    // connected.
                    if (activeRealtime == false) {
                        activeRealtime = true;
                        disposer.add(executionsRealtimely(false).to(buffer, observer::error));
                    }
                    int retrieved = rests.size();

                    if (retrieved != 0) {
                        // REST API returns some executions
                        if (!enoughExecutionRequest && size <= retrieved && coefficient.isGreaterThan(1)) {
                            // Since there are too many data acquired,
                            // narrow the data range and get it again.
                            coefficient = Num
                                    .max(Num.ONE, coefficient.isGreaterThan(30) ? coefficient.divide(2).scale(0) : coefficient.minus(6));
                            continue;
                        } else {
                            I.info(id + " \t" + rests.getFirst().date + " size " + retrieved + "(" + coefficient + ")");

                            for (Execution execution : rests) {
                                if (!buffer.canSwitch(execution)) {
                                    observer.accept(execution);
                                } else {
                                    // REST API has caught up with the real-time API,
                                    // we must switch to realtime API.
                                    buffer.switchToRealtime(execution.id, observer);
                                    return disposer;
                                }
                            }

                            long latestId = rests.peekLast().id;
                            if (retrieved <= executionMinRequest && buffer.realtime
                                    .isEmpty() && startId == latestId || !supportStableExecutionQuery()) {
                                // REST API has caught up with the real-time API,
                                // we must switch to realtime API.
                                buffer.switchToRealtime(latestId, observer);
                                return disposer;
                            }
                            startId = latestId;

                            // The number of acquired data is too small,
                            // expand the data range slightly from next time.
                            if (retrieved < size * 0.05) {
                                coefficient = coefficient.plus("6");
                            } else if (retrieved < size * 0.1) {
                                coefficient = coefficient.plus("4");
                            } else if (retrieved < size * 0.3) {
                                coefficient = coefficient.plus("2");
                            } else if (retrieved < size * 0.5) {
                                coefficient = coefficient.plus("0.5");
                            } else if (retrieved < size * 0.7) {
                                coefficient = coefficient.plus("0.1");
                            } else if (retrieved < size * 2) {
                                // The number of acquired data is too large,
                                // shrink the data range slightly from next time.
                                coefficient = Num.max(coefficient.minus("0.1"), Num.of("0.1"));
                            }
                        }
                    } else {
                        // REST API returns empty execution
                        if (startId < buffer.realtimeFirstId() && supportStableExecutionQuery()) {
                            // Although there is no data in the current search range,
                            // since it has not yet reached the latest execution,
                            // shift the range backward and search again.
                            startId += range - 1;
                            coefficient = coefficient.plus("50");
                            continue;
                        }

                        // REST API has caught up with the real-time API,
                        // we must switch to realtime API.
                        buffer.switchToRealtime(startId, observer);
                        break;
                    }
                }
                return disposer;
            }) //
                    .effectOnError(e -> e.printStackTrace())
                    .retry(withPolicy(retryMax, "ExecutionLog"))
                    .effect(log::store)
                    .share();
        }
        return executionCollector;
    }

    /**
     * 
     */
    private class BufferFromRestToRealtime implements Observer<Execution> {

        /** The upper error handler. */
        private final Consumer<? super Throwable> error;

        /** The actual realtime execution buffer. */
        private ConcurrentLinkedDeque<Execution> realtime = new ConcurrentLinkedDeque();

        /** The execution event receiver. */
        private Observer<? super Execution> destination = realtime::add;

        /** The no-realtime latest execution id. */
        private long latestId = -1;

        /**
         * Build {@link BufferFromRestToRealtime}.
         * 
         * @param sequntial
         */
        private BufferFromRestToRealtime(Consumer<? super Throwable> error) {
            this.error = error;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(Execution e) {
            destination.accept(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable e) {
            error.accept(e);
        }

        /**
         * Test whether the specified execution is queued in realtime buffer or not.
         * 
         * @param e An execution which is retrieved by REST API.
         * @return
         */
        private boolean canSwitch(Execution e) {
            // realtime buffer is empty
            Execution first = realtime.peekFirst();
            if (first == null) {
                return false;
            }
            return checkEquality(e, first);
        }

        /**
         * Switch to realtime API.
         * 
         * @param currentId
         * @param observer
         */
        private void switchToRealtime(long currentId, Observer<? super Execution> observer) {
            while (!realtime.isEmpty()) {
                ConcurrentLinkedDeque<Execution> buffer = realtime;
                realtime = new ConcurrentLinkedDeque();
                latestId = -1;
                for (Execution e : buffer) {
                    observer.accept(e);
                }
                I.info(id + " \t" + buffer.peek().date + " size " + buffer.size());
            }
            destination = observer;
        }

        /**
         * Compute the first execution id in realtime buffer.
         * 
         * @return
         */
        private long realtimeFirstId() {
            if (!realtime.isEmpty()) {
                return realtime.peek().id;
            } else if (0 < latestId) {
                return latestId;
            } else {
                return latestId = executionLatest().map(e -> e.id).waitForTerminate().to().or(-1L);
            }
        }
    }

    /**
     * Get the maximum value of the execution history that can be retrieved in a single request.
     * 
     * @return
     */
    public final int executionRequestLimit() {
        return executionMaxRequest;
    }

    /**
     * Acquier the latest execution log.
     * 
     * @return A latest execution log.
     */
    public abstract Signal<Execution> executionLatest();

    /**
     * Acquire the execution log between start (exclusive) and end (exclusive) key.
     * 
     * @return This {@link Signal} will be completed immediately.
     */
    public abstract Signal<Execution> executionsAfter(long startId, long endId);

    /**
     * Retrieves the execution log before the specified ID. (The specified ID is excluded)
     * 
     * @return A single execution log.
     */
    public abstract Signal<Execution> executionsBefore(long id);

    /**
     * Acquire execution log in realtime. This is infinitely.
     * 
     * @return A shared realtime execution logs.
     */
    public final synchronized Signal<Execution> executionsRealtimely() {
        return executionsRealtimely(true);
    }

    /**
     * Acquire execution log in realtime. This is infinitely.
     * 
     * @param autoReconnect Need to reconnect automatically.
     * @return A shared realtime execution logs.
     */
    public final synchronized Signal<Execution> executionsRealtimely(boolean autoReconnect) {
        if (executionRealtimely == null) {
            executionRealtimely = connectExecutionRealtimely().effectOnObserve(disposer::add).share();
        }
        return executionRealtimely.retry(autoReconnect ? withPolicy(retryMax, "ExecutionRealtimely") : null);
    }

    /**
     * Connect to the realtime execution log stream.
     * 
     * @return A realtime execution logs.
     */
    protected abstract Signal<Execution> connectExecutionRealtimely();

    /**
     * Checks whether the specified {@link Execution}s are the same.
     * 
     * @param one Non-null target.
     * @param other Non-null target.
     * @return Result.
     */
    public boolean checkEquality(Execution one, Execution other) {
        return one.id == other.id;
    }

    /**
     * Retrieve the initial {@link Execution}.
     * 
     * @return
     */
    public Signal<Execution> searchInitialExecution() {
        LogHouse external = loghouse();
        if (external.isValid()) {
            return external.first().flatMap(external::convert).first();
        } else {
            return executionLatest().flatMap(latest -> searchInitialExecution(1, latest));
        }
    }

    /**
     * Retrieve the initial {@link Execution}.
     * 
     * @return
     */
    private Signal<Execution> searchInitialExecution(long start, Execution end) {
        long middle = (start + end.id) / 2;

        return executionsBefore(middle).buffer().or(List.of()).recover(List.of()).flatMap(result -> {
            int size = result.size();
            I.info(this + " searches the initial execution (" + size + "). [" + start + " ~ " + middle + " ~ " + end.id + " at " + end.date + "]");
            if (size == 0) {
                // Since there is no log prior to the middle ID, we can assume that
                // the initial execution exists between middle and latest
                return searchInitialExecution(middle, end);
            } else if (executionMaxRequest <= size) {
                // Since it is equal to the maximum number of execution log that can be
                // retrieved at one time, there is a possibility that old log still exists.
                return searchInitialExecution(start, result.get(0));
            } else {
                // Since there are fewer execution log than can be retrieved at one time,
                // the oldest of these is determined to be the first log.
                return I.signal(result.get(0));
            }
        });
    }

    /**
     * Get the ID around the specified date and time.
     * 
     * @param target
     * @return
     */
    public Signal<Execution> searchNearestExecution(ZonedDateTime target) {
        LogHouse external = loghouse();
        if (external.isValid()) {
            return external.convert(target).first();
        } else {
            return executionLatest().concatMap(latest -> executionsBefore(latest.id))
                    .buffer()
                    .flatMap(list -> searchNearestExecution(target, list.get(0), list.get(list.size() - 1), 0));
        }
    }

    /**
     * INTERNAL: Estimate the nearest {@link Execution} at the specified time.
     * 
     * @param target
     * @param current
     * @return
     */
    private Signal<Execution> searchNearestExecution(ZonedDateTime target, Execution sampleStart, Execution sampleEnd, int count) {
        I.info(this + " searches for the execution log closest to " + target.toLocalDate() + ". [" + sampleStart.date
                .toLocalDateTime() + " ~ " + sampleEnd.date.toLocalDateTime() + "]");

        double timeDistance = sampleEnd.mills - sampleStart.mills;
        double idDistance = sampleEnd.id - sampleStart.id;
        double targetDistance = sampleEnd.mills - target.toInstant().toEpochMilli();
        long estimatedTargetId = Math.round(sampleEnd.id - idDistance * (targetDistance / timeDistance));

        return executionsBefore(estimatedTargetId).buffer().or(List.of()).flatMap(candidates -> {
            Execution first = candidates.get(0);
            Execution last = candidates.get(candidates.size() - 1);

            if (target.isBefore(first.date)) {
                return searchNearestExecution(target, first, last, count);
            } else if (last.date.isBefore(target)) {
                if (last.equals(sampleEnd) || Duration.between(last.date, target).toMinutes() < 1) {
                    return executionsAfter(last.id - 1, last.id + executionMaxRequest).takeWhile(e -> e.date.isBefore(target));
                } else {
                    return searchNearestExecution(target, first, last, count);
                }
            } else {
                for (int i = 0; i < candidates.size(); i++) {
                    if (!candidates.get(i).date.isBefore(target)) {
                        return I.signal(candidates.get(i - 1));
                    }
                }
                // If this exception will be thrown, it is bug of this program. So we must
                // rethrow the wrapped error in here.
                throw new Error("Here is unreachable.");
            }
        });
    }

    /**
     * Request order actually.
     * 
     * @param order A order to request.
     * @return A requested order.
     */
    public Signal<String> request(Order order) {
        return I.signal();
    }

    /**
     * Request canceling order actually.
     * 
     * @param order A order to cancel.
     * @return A cancelled order result (state, remainingSize, executedSize).
     */
    public Signal<Order> cancel(Order order) {
        return I.signal();
    }

    /**
     * Request canceling all orders actually.
     * 
     * @return A cancelled order result (state, remainingSize, executedSize).
     */
    public Signal<Boolean> cancelAll() {
        return I.signal();
    }

    /**
     * Request all orders.
     * 
     * @return
     */
    public Signal<Order> orders() {
        return I.signal();
    }

    /**
     * Request all orders with the specified state.
     * 
     * @return
     */
    public Signal<Order> orders(OrderState state) {
        return I.signal();
    }

    /**
     * Acquire the order state in realtime. This is infinitely.
     * 
     * @return A event stream of order state.
     */
    public final synchronized Signal<Order> ordersRealtimely() {
        return connectOrdersRealtimely().effectOnObserve(disposer::add).retry(withPolicy(retryMax, "OrderRealtimely"));
    }

    /**
     * Connect to the realtime order stream.
     * 
     * @return
     */
    protected Signal<Order> connectOrdersRealtimely() {
        return I.signal();
    }

    /**
     * Get amount of the base and target currency.
     * 
     * @return
     */
    public abstract Signal<OrderBookChanges> orderBook();

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @return A shared realtime order books.
     */
    public final synchronized Signal<OrderBookChanges> orderBookRealtimely() {
        return orderBookRealtimely(true);
    }

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @param autoReconnect Need to reconnect automatically.
     * @return A shared realtime order books.
     */
    public final synchronized Signal<OrderBookChanges> orderBookRealtimely(boolean autoReconnect) {
        if (orderbookRealtimely == null) {
            orderbookRealtimely = orderBook().concat(connectOrderBookRealtimely()).effectOnObserve(disposer::add).share();
        }
        return orderbookRealtimely.retry(autoReconnect ? withPolicy(retryMax, "OrderBookRealtimely") : null);
    }

    /**
     * Connect to the realtime order book stream.
     * 
     * @return A realtime order books.
     */
    protected abstract Signal<OrderBookChanges> connectOrderBookRealtimely();

    public Signal<Liquidation> liquidations(ZonedDateTime startExcluded, ZonedDateTime endExcluded) {
        return I.signal();
    }

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @return A shared realtime order books.
     */
    public final synchronized Signal<Liquidation> liquidationRealtimely() {
        if (liquidationRealtimely == null) {
            liquidationRealtimely = connectLiquidation().effectOnObserve(disposer::add).retry(withPolicy(retryMax, "Liquidation")).share();
        }
        return liquidationRealtimely;
    }

    /**
     * Connect to the realtime order book stream.
     * 
     * @return A realtime order books.
     */
    protected Signal<Liquidation> connectLiquidation() {
        return I.signal();
    }

    /**
     * Get amount of the base currency.
     */
    public Signal<Num> baseCurrency() {
        return I.signal(Num.ZERO);
    }

    /**
     * Get amount of the target currency.
     */
    public Signal<Num> targetCurrency() {
        return I.signal(Num.ZERO);
    }

    /**
     * Get the external {@link LogHouse}.
     * 
     * @return
     */
    public LogHouse loghouse() {
        return LogHouse.INVALID;
    }

    /**
     * Return the http communicator.
     * 
     * @return
     */
    protected HttpClient client() {
        return null;
    }

    /**
     * Return the realtime communicator.
     * 
     * @return
     */
    protected abstract EfficientWebSocket clientRealtimely();

    /**
     * Returns the root directory of this service.
     * 
     * @return
     */
    public Directory directory() {
        return Locator.directory(I.env("MarketLog", ".log")).directory(exchange.name()).directory(marketName.replace(':', '-'));
    }

    /**
     * Returns the sub directory of this service.
     * 
     * @return
     */
    public final Directory directory(String name) {
        return directory().directory(name);
    }

    /**
     * Returns the file of this service.
     * 
     * @return
     */
    public final File file(String name) {
        return directory().file(name);
    }

    /**
     * Get the current time.
     * 
     * @return The current time.
     */
    public ZonedDateTime now() {
        return Chrono.utcNow();
    }

    /**
     * Get the market scheduler.
     * 
     * @return A scheduler.
     */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /**
     * Create new {@link RetryPolicy}.
     * 
     * @return
     */
    protected final RetryPolicy withPolicy() {
        return withPolicy(retryMax, formattedId);
    }

    /**
     * Create new {@link RetryPolicy}.
     * 
     * @param max The maximum number to retry.
     * @return
     */
    protected RetryPolicy withPolicy(int max, String name) {
        return RetryPolicy.with.limit(max)
                .delay(x -> Duration.ofSeconds(x < 30 ? (x + 1) * (x + 1) : 900))
                .scheduler(scheduler())
                .name(name == null || name.length() == 0 ? null : id + " : " + name);
    }

    /**
     * Check for support of retriving execution history over time, which is capable of building
     * execution history.
     * 
     * @return
     */
    public boolean supportStableExecutionQuery() {
        return true;
    }

    /**
     * Check for support of external log repositories, which is capable of building execution
     * history.
     * 
     * @return
     */
    public boolean supportExternalLogHouse() {
        return false;
    }

    /**
     * Check for support for of realtime orderbook fixing. The specific API does not tell you the
     * information that the quantity has reached zero, so you should erase any existing data that is
     * within the range of the retrieved data.
     * 
     * @return
     */
    public boolean supportOrderBookFix() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void vandalize() {
        disposer.dispose();
        scheduler.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int compareTo(MarketService o) {
        return id.compareTo(o.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return id;
    }

    /**
     * Codec.
     */
    @SuppressWarnings("unused")
    private static class Codec implements Decoder<MarketService>, Encoder<MarketService> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(MarketService value) {
            return value.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MarketService decode(String value) {
            return MarketServiceProvider.by(value).or((MarketService) null);
        }
    }
}