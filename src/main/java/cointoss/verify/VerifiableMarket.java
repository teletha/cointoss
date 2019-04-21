
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Num;

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
     * {@inheritDoc}
     */
    @Override
    protected void readOrderBook() {
        // do nothing
    }

    /**
     * Emulate execution event.
     * 
     * @param time
     * @param size
     * @param price
     */
    public VerifiableMarket execute(int size, int price) {
        return execute(Direction.random(), size, price);
    }

    /**
     * Emulate execution event.
     * 
     * @param time
     * @param size
     * @param price
     */
    public VerifiableMarket execute(int size, int price, int time) {
        return execute(Direction.random(), size, price, new TimeLag(time));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Direction side, int size, int price) {
        return execute(side, size, price, new TimeLag(0));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Direction side, Num size, Num price) {
        return execute(side, size, price, new TimeLag(0));
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Direction side, int size, int price, TimeLag lag) {
        return execute(side, Num.of(size), Num.of(price), lag);
    }

    /**
     * Emulate execution event.
     * 
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Direction side, Num size, Num price, TimeLag lag) {
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
    public VerifiableMarket execute(Execution e, TimeLag lag) {
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
     * Emulate execution event.
     * 
     * @param count A number of {@link Execution}s.
     * @param e An execution mold.
     */
    public void executeSequencially(int count, Execution e) {
        if (e != null) {
            execute(e);

            for (int i = 1; i < count; i++) {
                Execution copy = new Execution();
                copy.size = copy.cumulativeSize = e.size;
                copy.date = e.date;
                copy.delay = e.delay;
                copy.id = e.id ^ (97 + i);
                copy.mills = e.mills;
                copy.price = e.price;
                copy.side = e.side;
                copy.consecutive = e.side.isBuy() ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;

                execute(copy);
            }
        }
    }

    /**
     * Emulate order and execution event.
     * 
     * @param limitShort
     */
    public void requestAndExecution(Order order) {
        request(order).to(id -> {
            execute(order.side, order.size, order.price.v.minus(order.side, service.setting.baseCurrencyMinimumBidPrice()));
        });
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
        Map<OrderState, List<Order>> state = new HashMap();

        service.orders().to(o -> state.computeIfAbsent(o.state.v, s -> new ArrayList()).add(o));

        assert state.getOrDefault(OrderState.ACTIVE, Collections.EMPTY_LIST).size() == active;
        assert state.getOrDefault(OrderState.COMPLETED, Collections.EMPTY_LIST).size() == completed;
        assert state.getOrDefault(OrderState.CANCELED, Collections.EMPTY_LIST).size() == canceled;
        assert state.getOrDefault(OrderState.EXPIRED, Collections.EMPTY_LIST).size() == expired;
        assert state.getOrDefault(OrderState.REJECTED, Collections.EMPTY_LIST).size() == rejected;

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
