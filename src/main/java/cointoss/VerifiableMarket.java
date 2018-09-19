
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

import static cointoss.backtest.Time.*;

import cointoss.backtest.Time;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Num;
import kiss.Table;

/**
 * @version 2018/09/18 21:18:07
 */
public class VerifiableMarket extends Market {

    /** Hide super class field. */
    public final VerifiableMarketService service;

    /**
     * Create {@link VerifiableMarket} with default {@link VerifiableMarketService}.
     */
    public VerifiableMarket() {
        this(new VerifiableMarketService());
    }

    /**
     * Create {@link VerifiableMarket} with default {@link VerifiableMarketService}.
     */
    public VerifiableMarket(MarketService service) {
        super(new VerifiableMarketService(service));

        this.service = (VerifiableMarketService) super.service;
    }

    /**
     * Emulate execution event.
     * 
     * @param time
     * @param size
     * @param price
     */
    public VerifiableMarket execute(int size, int price) {
        return execute(Side.random(), size, price);
    }

    /**
     * Emulate execution event.
     * 
     * @param time
     * @param size
     * @param price
     */
    public VerifiableMarket execute(int size, int price, int time) {
        return execute(Side.random(), size, price, at(time));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Side side, int size, int price) {
        return execute(side, size, price, at(0));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Side side, Num size, Num price) {
        return execute(side, size, price, at(0));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Side side, int size, int price, Time lag) {
        return execute(side, Num.of(size), Num.of(price), lag);
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Side side, Num size, Num price, Time lag) {
        Execution e = new Execution();
        e.side = side;
        e.size = e.cumulativeSize = size;
        e.price = price;
        e.date = lag.to();

        return execute(e);
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Execution e) {
        timelineObservers.accept(service.emulate(e));
        return this;
    }

    /**
     * Emulate order and execution event.
     * 
     * @param limitShort
     */
    public void requestAndExecution(Order order) {
        request(order).to(id -> {
            execute(order.side, order.size, order.price.v);
        });
    }

    /**
     * <p>
     * Helper method to request {@link Order}.
     * </p>
     * 
     * @param order
     */
    public Order requestTo(Order order) {
        request(order).to();

        return order;
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
    public boolean validateOrderState(int active, int completed, int canceled, int expired, int rejected) {
        Table<OrderState, Order> state = service.orders().toTable(o -> o.state.v);

        assert state.get(OrderState.ACTIVE).size() == active;
        assert state.get(OrderState.COMPLETED).size() == completed;
        assert state.get(OrderState.CANCELED).size() == canceled;
        assert state.get(OrderState.EXPIRED).size() == expired;
        assert state.get(OrderState.REJECTED).size() == rejected;

        return true;
    }

    /**
     * Validate execution state by size.
     * 
     * @param i
     * @return
     */
    public boolean validateExecutionState(int executed) {
        assert service.executionsRealtimely().toList().size() == executed;

        return true;
    }
}
