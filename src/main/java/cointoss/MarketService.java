/*
 * Copyright (C) 2021 cointoss Development Team
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLogRepository;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.ticker.data.Liquidation;
import cointoss.ticker.data.OpenInterest;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.RetryPolicy;
import cointoss.util.arithmetic.Num;
import cointoss.util.feather.FeatherStore;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.Signal;
import psychopath.Directory;
import psychopath.Locator;

public abstract class MarketService implements Comparable<MarketService>, Disposable {

    /** The logging system. */
    protected static final Logger logger = LogManager.getLogger(MarketService.class);

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
    private final ScheduledThreadPoolExecutor scheduler;

    /**
     * @param exchange
     * @param marketName
     */
    protected MarketService(Exchange exchange, String marketName, MarketSetting setting) {
        this.exchange = Objects.requireNonNull(exchange);
        this.marketName = Objects.requireNonNull(marketName);
        this.id = exchange + " " + marketName.replaceAll("_", "").toUpperCase();
        this.formattedId = id.replace(exchange + " ", StringUtils.rightPad(exchange.name(), 8) + "\t");
        this.setting = setting;
        this.scheduler = new ScheduledThreadPoolExecutor(2, task -> {
            Thread thread = new Thread(task);
            thread.setName(id + " Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler.allowCoreThreadTimeOut(true);
        this.scheduler.setKeepAliveTime(30, TimeUnit.SECONDS);

        this.log = new ExecutionLog(this);
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
     * @param key An execution sequencial key (i.e. ID, datetime etc).
     * @return This {@link Signal} will be completed immediately.
     */
    public abstract Signal<Execution> executions(long startId, long endId);

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
        return connectExecutionRealtimely().effectOnObserve(disposer::add)
                .retryWhen(autoReconnect ? retryPolicy(500, "ExecutionRealtimely") : null);
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
        ExecutionLogRepository external = externalRepository();
        if (external == null) {
            return executionLatest().flatMap(latest -> searchInitialExecution(1, latest));
        } else {
            return external.first().flatMap(external::convert).first();
        }
    }

    /**
     * Retrieve the initial {@link Execution}.
     * 
     * @return
     */
    private Signal<Execution> searchInitialExecution(long start, Execution end) {
        long middle = (start + end.id) / 2;
        logger.info("{} searches for the initial execution log. [{} ~ {} ~ {}]", this, start, middle, end.date);

        return executionsBefore(middle).buffer().skipError().or(List.of()).flatMap(result -> {
            int size = result.size();
            if (size == 0) {
                // Since there is no log prior to the middle ID, we can assume that
                // the initial execution exists between middle and latest.
                return searchInitialExecution(middle, end);
            } else if (setting.acquirableExecutionSize <= size) {
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
        ExecutionLogRepository external = externalRepository();
        if (external == null) {
            return executionLatest().concatMap(latest -> executionsBefore(latest.id))
                    .buffer()
                    .flatMap(list -> searchNearestExecution(target, list.get(0), list.get(list.size() - 1), 0));
        } else {
            return external.convert(target).first();
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
        logger.info("{} searches for the execution log closest to {}. [{} ~ {}]", this, target.toLocalDate(), sampleStart.date
                .toLocalDateTime(), sampleEnd.date.toLocalDateTime());

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
                    return executions(last.id - 1, last.id + setting.acquirableExecutionSize).takeWhile(e -> e.date.isBefore(target));
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
        return connectOrdersRealtimely().effectOnObserve(disposer::add).retryWhen(retryPolicy(500, "OrderRealtimely"));
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
    public abstract Signal<OrderBookPageChanges> orderBook();

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @return A shared realtime order books.
     */
    public final synchronized Signal<OrderBookPageChanges> orderBookRealtimely() {
        return orderBookRealtimely(true);
    }

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @param autoReconnect Need to reconnect automatically.
     * @return A shared realtime order books.
     */
    public final synchronized Signal<OrderBookPageChanges> orderBookRealtimely(boolean autoReconnect) {
        return orderBook().concat(connectOrderBookRealtimely())
                .effectOnObserve(disposer::add)
                .retryWhen(autoReconnect ? retryPolicy(500, "OrderBookRealtimely") : null);
    }

    /**
     * Connect to the realtime order book stream.
     * 
     * @return A realtime order books.
     */
    protected abstract Signal<OrderBookPageChanges> connectOrderBookRealtimely();

    public Signal<Liquidation> liquidations(ZonedDateTime startExcluded, ZonedDateTime endExcluded) {
        return I.signal();
    }

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @param autoReconnect Need to reconnect automatically.
     * @return A shared realtime order books.
     */
    public final synchronized Signal<Liquidation> liquidationRealtimely() {
        return this.connectLiquidation().effectOnObserve(disposer::add).retryWhen(retryPolicy(500, "Liquidation"));
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
     * Provide the market specific tick related data if needed.
     */
    public Signal<OpenInterest> provideOpenInterest(ZonedDateTime startExcluded) {
        return I.signal();
    }

    public final FeatherStore<OpenInterest> openInterest = initializeOpenInterestStore();

    protected FeatherStore<OpenInterest> initializeOpenInterestStore() {
        return null;
    }

    /**
     * Provide the market specific tick related data infinitely.
     * 
     * @return A shared realtime stream.
     */
    public final synchronized Signal<OpenInterest> openInterestRealtimely() {
        return this.connectOpenInterest().effectOnObserve(disposer::add).retryWhen(retryPolicy(500, "OpenInterest"));
    }

    /**
     * Provide the market specific tick related data infinitely.
     * 
     * @return A shared realtime stream.
     */
    protected Signal<OpenInterest> connectOpenInterest() {
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
     * Get the external log repository.
     * 
     * @return
     */
    public ExecutionLogRepository externalRepository() {
        return null;
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
    public final Directory directory() {
        return Locator.directory(".log").directory(exchange.name()).directory(marketName.replace(':', '-'));
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
     * @param max The maximum number to retry.
     * @return
     */
    public final RetryPolicy retryPolicy(int max) {
        return retryPolicy(max, null);
    }

    /**
     * Create new {@link RetryPolicy}.
     * 
     * @param max The maximum number to retry.
     * @return
     */
    public RetryPolicy retryPolicy(int max, String name) {
        return RetryPolicy.with.limit(max)
                .delayLinear(Duration.ofSeconds(2))
                .scheduler(scheduler())
                .name(name == null || name.length() == 0 ? null : id + " : " + name);
    }

    /**
     * Checking support for historical trade.
     * 
     * @return
     */
    public boolean supportHistoricalTrade() {
        return true;
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