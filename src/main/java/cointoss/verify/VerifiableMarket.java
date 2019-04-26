
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

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.position.Entry;
import kiss.Signal;

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
     * @param direction
     * @param size
     * @param price
     */
    public VerifiableMarket perform(Execution e) {
        timelineObservers.accept(service.emulate(e));
        return this;
    }

    /**
     * Emulate execution event with time lag.
     * 
     * @param e An emulated {@link Execution}.
     * @param lag A lag time. (second)
     * @return
     */
    public VerifiableMarket perform(Execution e, int lag) {
        e.date = service.now().plusSeconds(lag);

        return perform(e);
    }

    /**
     * Emulate execution event.
     * 
     * @param count A number of {@link Execution}s.
     * @param e An execution mold.
     */
    public void performSequencially(int count, Execution e) {
        if (e != null) {
            perform(e);

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

                perform(copy);
            }
        }
    }

    public Signal<Entry> performEntry(Order entryOrder, Order exitOrder) {
        return orders.requestEntry(entryOrder).effect(entry -> {
            Execution exe = new Execution();
            exe.side = entryOrder.direction;
            exe.size = entryOrder.size;
            exe.date = entryOrder.creationTime.v;
            exe.price = entryOrder.price.minus(entryOrder.direction, service.setting.baseCurrencyMinimumBidPrice());

            perform(exe);
        }).flatMap(entry -> entry.requestExit(exitOrder));
    }

    /**
     * Emulate order and execution event.
     * 
     * @param limitShort
     */
    public void requestAndExecution(Order order) {
        request(order).to(id -> {
            Execution e = new Execution();
            e.side = order.direction;
            e.size = order.size;
            e.date = order.creationTime.v;
            e.price = order.price.minus(order.direction, service.setting.baseCurrencyMinimumBidPrice());

            perform(e);
        });
    }
}
