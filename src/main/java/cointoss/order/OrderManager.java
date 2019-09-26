/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import static cointoss.order.OrderState.*;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.util.PerformanceSensitive;

import com.google.common.annotations.VisibleForTesting;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * 
 */
public final class OrderManager {

    /** The actual service. */
    private final MarketService service;

    /** The actual order manager. */
    private final List<Order> managed = new CopyOnWriteArrayList();

    /** The unmodifiable exposed active orders. */
    public final List<Order> items = Collections.unmodifiableList(managed);

    /** The order adding event. */
    private final Signaling<Order> addition = new Signaling();

    /** The order adding event. */
    public final Signal<Order> add = addition.expose;

    /** The order removed event. */
    private final Signaling<Order> remove = new Signaling();

    /** The order removed event. */
    public final Signal<Order> removed = remove.expose;

    /** The actual position manager. */
    private final List<Position> internalPositions = new CopyOnWriteArrayList();

    /** The unmodifiable open positions. */
    public final List<Position> positions = Collections.unmodifiableList(internalPositions);

    /** The position remove event. */
    private final Signaling<Position> positionRemove = new Signaling();

    /** The position remove event. */
    public final Signal<Position> positionRemoved = positionRemove.expose;

    /** The position add event. */
    private final Signaling<Position> positionAdd = new Signaling();

    /** The position add event. */
    public final Signal<Position> positionAdded = positionAdd.expose;

    /** The current position total size. */
    public final Variable<Num> positionSize = Variable.of(Num.ZERO);

    /** The current position average price. */
    public final Variable<Num> positionPrice = Variable.of(Num.ZERO);

    /**
     * @param service
     */
    public OrderManager(MarketService service) {
        this.service = service;
        add.to(managed::add);
        removed.to(managed::remove);

        // retrieve orders on server
        // don't use orders().to(addition); it completes addition signaling itself
        service.orders().to(addition::accept);

        // // retrieve orders on realtime
        // service.add(service.executionsRealtimelyForMe().to(v -> {
        // // manage position
        // String id = v.ⅱ;
        // Execution execution = v.ⅲ;
        //
        // // manage order
        // for (Order order : managed) {
        // if (order.id.equals(id)) {
        // Num executed = execution.size;
        //
        // if (order.type.isTaker() && executed.isNot(0)) {
        // order.assignPrice(n -> n.multiply(order.executedSize)
        // .plus(execution.price.multiply(executed))
        // .divide(executed.plus(order.executedSize)));
        // }
        // order.executed(execution);
        //
        // if (order.remainingSize.is(Num.ZERO)) {
        // order.assignState(COMPLETED);
        // }
        // return;
        // }
        // }
        // }));
    }

    /**
     * Stream for the current managed {@link Order}s and the incoming {@link Order}s.
     * 
     * @return
     */
    public Signal<Order> manages() {
        return I.signal(managed).merge(add);
    }

    /**
     * Build the {@link Signal} which requests the specified {@link Order} to the market. This
     * method DON'T request order, you MUST subscribe {@link Signal}. If you want to request
     * actually, you can use {@link #requestNow(Order)}.
     * 
     * @param order A order to request.
     * @return A order request process.
     * @see #requestNow(Order)
     */
    public Signal<Order> request(Order order) {
        if (order.state == OrderState.INIT || order.state == OrderState.REQUESTING) {
            order.assignState(REQUESTING);
            addition.accept(order);

            // If the new order data comes to the real-time API before the oreder's response, the
            // order cannot be identified from the real-time API.
            // Record all order data from request to response, and check if there is already an
            // order data at response.
            Queue<Order> complementOrdersWhileRequestAndResponse = service.ordersRealtimely()
                    .takeUntil(order.observeActivating())
                    .toCollection(new ConcurrentLinkedQueue());

            return service.request(order, order::assignState).retryWhen(service.setting.retryPolicy()).map(id -> {
                order.assignId(id);
                order.assignCreationTime(service.now());

                // Because ID registration has been completed, it is possible to detect contracts
                // from the real-time API. Check if there is an order in the buffered data after
                // placing the order.
                I.signal(complementOrdersWhileRequestAndResponse)
                        .effectOnComplete(() -> order.assignState(ACTIVE))
                        .merge(service.ordersRealtimely())
                        .takeUntil(order.observeTerminating())
                        .take(o -> o.id.equals(id))
                        .effectOnTerminate(() -> remove.accept(order))
                        .to(o -> {
                            order.setExecutedSize(o.executedSize);
                            order.setRemainingSize(o.remainingSize);
                            order.assignState(o.state);
                        });
                return order;
            }).effectOnError(e -> {
                order.assignState(CANCELED);
            });
        } else {
            return I.signal(order);
        }
    }

    /**
     * Request the specified {@link Order} to the market actually.
     * 
     * @param order A order to request.
     * @return A requested {@link Order}.
     * @see #request(Order)
     */
    public Order requestNow(Order order) {
        request(order).to(I.NoOP);

        return order;
    }

    /**
     * Build the {@link Signal} which cancels the specified {@link Order} from the market. This
     * method DON'T cancel order, you MUST subscribe {@link Signal}. If you want to cancel actually,
     * you can use {@link #cancelNow(Order)}.
     * 
     * @param order A order to cancel.
     * @return A order cancel process.
     * @see #cancelNow(Order)
     */
    public Signal<Order> cancel(Order order) {
        if (order.state == ACTIVE || order.state == REQUESTING) {
            OrderState previous = order.state;
            order.assignState(REQUESTING);

            return service.cancel(order).retryWhen(service.setting.retryPolicy()).effect(o -> {
                managed.remove(o);
                o.assignState(CANCELED);
            }).effectOnError(e -> {
                order.assignState(previous);
            });
        } else {
            return I.signal(order);
        }
    }

    /**
     * Cancel the specified {@link Order} from the market actually.
     * 
     * @param order A order to request.
     * @return A canceled {@link Order}.
     * @see #cancel(Order)
     */
    public Order cancelNow(Order order) {
        cancel(order).to(I.NoOP);

        return order;
    }

    /**
     * Cancel all orders.
     */
    public void cancelNowAll() {
        for (Order order : items) {
            cancelNow(order);
        }
    }

    /**
     * Check the order state.
     * 
     * @return A result.
     */
    public boolean hasActiveOrder() {
        return managed.isEmpty() == false;
    }

    /**
     * Check the order state.
     * 
     * @return A result.
     */
    public boolean hasNoActiveOrder() {
        return managed.isEmpty() == true;
    }

    /**
     * Check the position state.
     * 
     * @return A result.
     */
    public boolean hasPosition() {
        return internalPositions.isEmpty() == false;
    }

    /**
     * Check the position state.
     * 
     * @return A result.
     */
    public boolean hasNoPosition() {
        return internalPositions.isEmpty() == true;
    }

    /**
     * Cehck the position state.
     * 
     * @return
     */
    public boolean hasLongPosition() {
        return hasPosition() && positions.get(0).isBuy();
    }

    /**
     * Cehck the position state.
     * 
     * @return
     */
    public boolean hasShortPosition() {
        return hasPosition() && positions.get(0).isSell();
    }

    public Direction positionDirection() {
        return hasLongPosition() ? Direction.BUY : Direction.SELL;
    }

    /**
     * Calculate total profit or loss on the current price.
     * 
     * @param currentPrice A current price.
     * @return A total profit or loss of this entry.
     */
    @PerformanceSensitive
    public final Num profit(Num currentPrice) {
        Num total = Num.ZERO;
        for (Position position : internalPositions) {
            total = total.plus(position.profit(currentPrice));
        }
        return total;
    }

    /**
     * Add {@link Position} manually.
     * 
     * @param position
     */
    public void add(Position position) {
        if (position != null) {
            internalPositions.add(position);
            positionAdd.accept(position);
            calculate();
        }
    }

    /**
     * For test.
     * 
     * @param e
     */
    @VisibleForTesting
    void add(Execution e) {
        add(e.direction, e);
    }

    /**
     * <p>
     * Update position by the specified my execution.
     * </p>
     * <p>
     * This method is separate for test.
     * </p>
     * 
     * @param exe A my execution.
     */
    public void add(Direction direction, Execution e) {
        if (e != null) {
            Num size = e.size;

            for (Position position : internalPositions) {
                if (position.direction == direction) {
                    // check same price position
                    if (position.price.is(e.price)) {
                        position.assignSize(position.size.plus(size));
                        calculate();
                        return;
                    }
                } else {
                    Num remaining = size.minus(position.size);

                    if (remaining.isPositive()) {
                        size = remaining;
                        position.assignSize(Num.ZERO);

                        internalPositions.remove(position);
                        positionRemove.accept(position);
                    } else if (remaining.isZero()) {
                        size = remaining;
                        position.assignSize(Num.ZERO);

                        internalPositions.remove(position);
                        positionRemove.accept(position);
                        calculate();
                        return;
                    } else {
                        position.assignSize(remaining.negate());
                        calculate();
                        return;
                    }
                }
            }

            if (size.isPositive()) {
                Position position = Position.with.direction(direction).price(e.price).size(size).date(e.date);

                internalPositions.add(position);
                positionAdd.accept(position);
                calculate();
            }
        }
    }

    /**
     * Calculate some variables.
     */
    private void calculate() {
        Num size = Num.ZERO;
        Num price = Num.ZERO;

        for (Position position : internalPositions) {
            size = size.plus(position.size);
            price = price.plus(position.price.multiply(position.size));
        }

        this.positionSize.set(size);
        this.positionPrice.set(size.isZero() ? Num.ZERO : price.divide(size));
    }
}
