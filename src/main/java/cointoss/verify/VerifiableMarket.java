
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
     * @param side
     * @param size
     * @param price
     */
    public VerifiableMarket execute(Execution e, int lag) {
        return execute(e, new TimeLag(lag));
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
            Execution e = new Execution();
            e.side = order.side;
            e.size = order.size;
            e.date = order.created.v;
            e.price = order.price.minus(order.side, service.setting.baseCurrencyMinimumBidPrice());

            execute(e);
        });
    }
}
