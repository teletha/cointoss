/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.order.OrderType;
import cointoss.order.QuantityCondition;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.Retry;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Ⅲ;

public class VerifiableMarketService extends MarketService {

    /** The managed id. */
    private int id = 0;

    /** The order manager. */
    private final Collection<BackendOrder> orderActive = new ConcurrentLinkedDeque<>();

    /** The order manager. */
    private final Signaling<Order> orderUpdateRealtimely = new Signaling();

    /** The execution manager. */
    private final LinkedList<Execution> executeds = new LinkedList();

    /** The initial base currency. */
    public Num baseCurrency = Num.HUNDRED;

    /** The initial target currency. */
    public Num targetCurrency = Num.ZERO;

    /** The latest execution time. */
    private ZonedDateTime now = Chrono.MIN;

    /** The latest execution epoch mills. */
    private long nowMills = 0;

    /** The testable scheduler. */
    private final SchedulerEmulator scheduler = new SchedulerEmulator();

    /** The task queue. */
    private final PriorityQueue<Task> tasks = new PriorityQueue();

    /** The emulation for lag. */
    public Latency latency = Latency.zero();

    /** The emulation mode for exclusive execution. */
    public boolean exclusiveExecution = true;

    /**
     * 
     */
    public VerifiableMarketService() {
        super("TestableExchange", "TestableMarket", MarketSetting.with.baseCurrencyMinimumBidPrice(Num.ONE)
                .targetCurrencyMinimumBidSize(Num.ONE)
                .orderBookGroupRanges(Num.ONE)
                .retryPolicy(Retry.with.unlimit().delayMaximum(Duration.ZERO)));
    }

    /**
     * 
     */
    public VerifiableMarketService(MarketService delegation) {
        super(delegation.exchangeName, delegation.marketName, delegation.setting
                .withRetryPolicy(Retry.with.unlimit().delayMaximum(Duration.ZERO)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return I.signal(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Order> connectOrdersRealtimely() {
        return orderUpdateRealtimely.expose;
    }

    /** The prepared execution store. */
    private final LinkedList<Execution> executionsBeforeOrderResponse = new LinkedList();

    /**
     * Prepare executions which are reveived before order response.
     * 
     * @param e
     */
    public void emulateExecutionBeforeOrderResponse(Execution e) {
        if (e != null) {
            executionsBeforeOrderResponse.add(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order, Consumer<OrderState> state) {
        return I.signal(order).map(o -> {
            BackendOrder child = new BackendOrder(order);
            child.id = "LOCAL-ACCEPTANCE-" + id++;
            child.state = OrderState.ACTIVE;
            child.createTimeMills = nowMills + latency.lag();
            child.remainingSize = order.size;

            orderActive.add(child);

            if (!executionsBeforeOrderResponse.isEmpty()) {
                for (Execution execution : executionsBeforeOrderResponse) {
                    emulate(execution);
                }
                executionsBeforeOrderResponse.clear();
            }

            return child.id;
        });
    }

    /** The prepared execution store. */
    private final LinkedList<Execution> executionsAfterOrderCancelResponse = new LinkedList();

    /**
     * Prepare executions which are reveived after order cancel response.
     * 
     * @param e
     */
    public void emulateExecutionAfterOrderCancelResponse(Execution e) {
        if (e != null) {
            executionsAfterOrderCancelResponse.add(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        BackendOrder backend = findBy(order);

        // associated backend order is not found, do nothing
        if (backend == null || backend.type.isTaker()) {
            return I.signal();
        }

        // when latency is zero, cancel order immediately
        ZonedDateTime delay = latency.emulate(now);

        if (delay == now) {
            backend.cancel();
            orderUpdateRealtimely.accept(Order.with.direction(backend.direction, backend.size)
                    .id(backend.id)
                    .state(OrderState.CANCELED)
                    .remainingSize(backend.remainingSize)
                    .executedSize(backend.executedSize));
            return I.signal(order);
        }

        // backend order will be canceled in the specified delay
        backend.cancelTimeMills = Chrono.epochMills(delay);

        return backend.canceling.expose.mapTo(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsRealtimely() {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return I.signal(orderActive).map(o -> {
            Order order = Order.with.direction(o.direction, o.size)
                    .price(o.price)
                    .quantityCondition(o.condition)
                    .remainingSize(o.remainingSize)
                    .executedSize(o.executedSize)
                    .id(o.id)
                    .state(o.state);

            return order;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders(OrderState state) {
        return I.signal(orderActive).take(o -> o.state == state).map(o -> {
            Order order = Order.with.direction(o.direction, o.size)
                    .price(o.price)
                    .quantityCondition(o.condition)
                    .remainingSize(o.remainingSize)
                    .executedSize(o.executedSize)
                    .id(o.id)
                    .state(o.state);

            return order;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return I.signal(baseCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal(targetCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChange> orderBook() {
        return Signal.never();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime now() {
        return now;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /**
     * Elapse market time.
     * 
     * @param time
     * @param unit
     * @return
     */
    final void elapse(long time, TimeUnit unit) {
        now = now.plus(time, unit.toChronoUnit());
        nowMills = nowMills + unit.toMillis(time);

        while (!tasks.isEmpty() && tasks.peek().activeTime <= nowMills) {
            tasks.poll().run();
        }
    }

    /**
     * Emulate {@link Execution}.
     * 
     * @param e
     * @return
     */
    public Execution emulate(Execution e) {
        now = e.date;
        nowMills = e.mills;

        // emulate market execution
        Iterator<BackendOrder> iterator = orderActive.iterator();

        while (iterator.hasNext()) {
            BackendOrder order = iterator.next();

            // time base filter
            if (e.mills < order.createTimeMills) {
                continue;
            }

            // check canceling time
            if (order.cancelTimeMills != 0 && order.cancelTimeMills <= e.mills) {
                order.cancel();
                continue;
            }

            // check quantity condition
            if (order.condition == QuantityCondition.FillOrKill && !validateTradable(order, e)) {
                iterator.remove();
                continue;
            }

            if (order.condition == QuantityCondition.ImmediateOrCancel) {
                if (validateTradableByPrice(order, e)) {
                    order.remainingSize = Num.min(e.size, order.remainingSize);
                } else {
                    iterator.remove();
                    continue;
                }
            }

            if (validateTradableByPrice(order, e)) {
                Num executedSize = Num.min(e.size, order.remainingSize);
                if (order.type.isTaker() && executedSize.isNot(0)) {
                    order.marketMinPrice = order.isBuy() ? Num.max(order.marketMinPrice, e.price) : Num.min(order.marketMinPrice, e.price);
                    order.price = order.price.multiply(order.executedSize)
                            .plus(order.marketMinPrice.multiply(executedSize))
                            .divide(executedSize.plus(order.executedSize));
                }
                order.executedSize = order.executedSize.plus(executedSize);
                order.remainingSize = order.remainingSize.minus(executedSize);

                Execution exe = Execution.with.direction(order.direction(), executedSize)
                        .price(order.type.isTaker() ? order.marketMinPrice : order.price)
                        .date(e.date);
                executeds.add(exe);

                if (order.remainingSize.isZero()) {
                    order.state = OrderState.COMPLETED;
                    iterator.remove();
                }

                orderUpdateRealtimely.accept(Order.with.direction(order.direction, order.size)
                        .id(order.id)
                        .price(order.price)
                        .remainingSize(order.remainingSize)
                        .executedSize(order.executedSize)
                        .state(order.state));

                if (!exclusiveExecution) {
                    continue;
                }

                while (!tasks.isEmpty() && tasks.peek().activeTime <= nowMills) {
                    tasks.poll().run();
                }

                // replace execution info
                return Execution.with.direction(e.direction, exe.size)
                        .price(exe.price)
                        .date(e.date)
                        .id(e.id)
                        .consecutive(e.consecutive)
                        .delay(e.delay);
            }
        }

        while (!tasks.isEmpty() && tasks.peek().activeTime <= nowMills) {
            tasks.poll().run();
        }

        return e;
    }

    /**
     * Test whether this order can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradable(BackendOrder order, Execution e) {
        return validateTradableBySize(order, e) && validateTradableByPrice(order, e);
    }

    /**
     * Test whether this order price can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradableByPrice(BackendOrder order, Execution e) {
        if (order.type == OrderType.Taker) {
            return true;
        }

        if (order.isBuy()) {
            Num price = order.price;

            return price.isGreaterThan(e.price) || price.is(setting.baseCurrencyMinimumBidPrice());
        } else {
            return order.price.isLessThan(e.price);
        }
    }

    /**
     * Test whether this order size can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradableBySize(BackendOrder order, Execution e) {
        return order.size.isLessThanOrEqual(e.size);
    }

    /**
     * Find {@link BackendOrder} by fronend {@link Order}.
     * 
     * @param order
     * @return
     */
    private BackendOrder findBy(Order order) {
        for (BackendOrder back : orderActive) {
            if (back.front == order) {
                return back;
            }
        }
        return null;
    }

    /**
     * For test.
     */
    private class BackendOrder implements Directional {

        /** The frontend order. */
        private final Order front;

        /** The order direction. */
        private final Direction direction;

        /** The order size. */
        private final Num size;

        /** The order id. */
        private String id;

        /** The order price. */
        private Num price;

        /** The order type. */
        private OrderType type;

        /** The order type. */
        private QuantityCondition condition;

        /** The order state. */
        private OrderState state;

        /** The order state. */
        private Num remainingSize;

        /** The order state. */
        private Num executedSize;

        /** The minimum price for market order. */
        private Num marketMinPrice;

        /**
         * The time which this order is created, Using epoch mills to make time-related calculation
         * faster.
         */
        private long createTimeMills;

        /**
         * The time which this order will be canceled completely. Using epoch mills to make
         * time-related calculation faster.
         */
        private long cancelTimeMills;

        /** The cancel event emitter. */
        private final Signaling<Ⅲ<String, OrderState, Num>> canceling = new Signaling();

        /** The prepared execution store. */
        private final LinkedList<Execution> executionsAfterOrderCancelResponse = new LinkedList();

        /**
         * Create backend managed order.
         * 
         * @param o
         */
        private BackendOrder(Order o) {
            this.front = o;
            this.direction = o.direction;
            this.size = o.size;
            this.remainingSize = o.remainingSize;
            this.executedSize = o.executedSize;
            this.price = o.price;
            this.type = o.type;
            this.condition = o.quantityCondition;
            this.createTimeMills = o.creationTime.toInstant().toEpochMilli();
            this.marketMinPrice = isBuy() ? Num.ZERO : Num.MAX;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Direction direction() {
            return direction;
        }

        /**
         * Cancel this order actually.
         */
        private void cancel() {
            I.signal(orderActive).take(o -> o.id.equals(id)).take(1).to(o -> {
                o.state = OrderState.CANCELED;
                canceling.accept(I.pair(id, OrderState.CANCELED, o.remainingSize));
                canceling.complete();
                orderActive.remove(o);

            });
        }

        private void cancelBeforeEexecution() {
            I.signal(orderActive).take(o -> o.id.equals(id)).take(1).to(o -> {

            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "BackendOrder [direction=" + direction + ", size=" + size + ", id=" + id + ", price=" + price + ", type=" + type + ", condition=" + condition + ", state=" + state + ", remainingSize=" + remainingSize + ", executedSize=" + executedSize + ", marketMinPrice=" + marketMinPrice + ", createTimeMills=" + createTimeMills + ", cancelTimeMills=" + cancelTimeMills + "]";
        }
    }

    /**
     * 
     */
    private class SchedulerEmulator implements ScheduledExecutorService {

        /**
         * {@inheritDoc}
         */
        @Override
        public void shutdown() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isShutdown() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isTerminated() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> submit(Runnable task) {
            return submit(task, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(Runnable command) {
            schedule(command, 0, TimeUnit.SECONDS);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            Task future = new Task(unit.toMillis(delay), command);

            if (delay <= 0) {
                future.run();
            } else {
                tasks.add(future);
            }
            return future;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            Task future = new Task(unit.toMillis(delay), callable);

            if (delay <= 0) {
                future.run();
            } else {
                tasks.add(future);
            }
            return future;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return null;
        }
    }

    /**
     * 
     */
    private class Task<V> implements ScheduledFuture, Callable<V>, Runnable {

        /** The active time by epoch mills. */
        private final long activeTime;

        private final Callable<V> action;

        private boolean cancelled;

        private boolean done;

        /**
         * @param mills
         * @param action
         */
        private Task(long delay, Runnable action) {
            this(delay, () -> {
                action.run();
                return null;
            });
        }

        /**
         * @param delayMills
         * @param action
         */
        private Task(long delayMills, Callable<V> action) {
            this.activeTime = nowMills + delayMills;
            this.action = action;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                call();
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V call() throws Exception {
            return action.call();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDelay(TimeUnit unit) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Delayed o) {
            Task other = (Task) o;

            if (activeTime == other.activeTime) {
                return 0;
            }
            return activeTime < other.activeTime ? -1 : 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            return tasks.remove(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDone() {
            return done;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get() throws InterruptedException, ExecutionException {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Task[" + activeTime + "]";
        }
    }
}
