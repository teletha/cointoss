
/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import static cointoss.Time.*;

import java.time.ZonedDateTime;

import cointoss.Time.At;
import cointoss.order.Order;
import cointoss.order.Order.State;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Table;

/**
 * @version 2017/07/24 23:50:33
 */
class TestableMarket extends Market {

    /** The starting time. */
    private final ZonedDateTime base = ZonedDateTime.now().withSecond(0).withNano(0);

    /**
     * @param backend
     * @param builder
     * @param strategy
     */
    TestableMarket() {
        super(new TestableMarketBackend(Time.lag(0)), Signal.EMPTY, new TestableMarketTrading());
    }

    /**
     * @param delay
     */
    TestableMarket(int delay) {
        super(new TestableMarketBackend(Time.lag(delay)), Signal.EMPTY, new TestableMarketTrading());
    }

    /**
     * Emulate execution event.
     * 
     * @param time
     * @param size
     * @param price
     */
    TestableMarket execute(int size, int price) {
        return execute(Side.random(), size, price);
    }

    /**
     * Emulate execution event.
     * 
     * @param time
     * @param size
     * @param price
     */
    TestableMarket execute(int size, int price, int time) {
        return execute(Side.random(), size, price, at(time));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Side side, int size, int price) {
        return execute(side, size, price, at(0));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Side side, Num size, Num price) {
        return execute(side, size, price, at(0), "", "");
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Side side, int size, int price, String buyId, String sellId) {
        return execute(side, Num.of(size), Num.of(price), at(0), buyId, sellId);
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Side side, Num size, Num price, String buyId, String sellId) {
        return execute(side, size, price, at(0), buyId, sellId);
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Side side, int size, int price, At time) {
        return execute(side, Num.of(size), Num.of(price), time, "", "");
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Side side, Num size, Num price, At time, String buyId, String sellId) {
        Execution e = new Execution();
        e.side = side;
        e.size = e.cumulativeSize = size;
        e.price = price;
        e.exec_date = time.to();
        e.buy_child_order_acceptance_id = buyId;
        e.sell_child_order_acceptance_id = sellId;

        return execute(e);
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    TestableMarket execute(Execution e) {
        tick(((TestableMarketBackend) backend).emulate(e));
        return this;
    }

    /**
     * Emulate order and execution event.
     * 
     * @param limitShort
     */
    void requestAndExecution(Order order) {
        request(order).to(id -> {
            execute(order.side, order.size, order.price, order.side.isBuy() ? id.id
                    : "", order.side().isSell() ? id.id : "");
        });
    }

    /**
     * <p>
     * Helper method to emit {@link Order}.
     * </p>
     * 
     * @param order
     */
    Order requestSuccessfully(Order order) {
        request(order).to();

        return order;
    }

    /**
     * <p>
     * Helper method to emit {@link Order}.
     * </p>
     * 
     * @param order
     */
    void requestSuccessfully(Order order, Time.Lag lag) {
        request(order).to();

    }

    /**
     * Validate order state by size.
     * 
     * @param active
     * @param completed
     * @param canceled
     * @param expired
     * @param rejected
     * @return
     */
    boolean validateOrderState(int active, int completed, int canceled, int expired, int rejected) {
        Table<State, Order> state = backend.orders().toTable(o -> o.state.v);

        assert state.get(State.ACTIVE).size() == active;
        assert state.get(State.COMPLETED).size() == completed;
        assert state.get(State.CANCELED).size() == canceled;
        assert state.get(State.EXPIRED).size() == expired;
        assert state.get(State.REJECTED).size() == rejected;

        return true;
    }

    /**
     * Validate execution state by size.
     * 
     * @param i
     * @return
     */
    boolean validateExecutionState(int executed) {
        assert backend.getExecutions().toList().size() == executed;

        return true;
    }
}
