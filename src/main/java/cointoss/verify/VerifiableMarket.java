
/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.orderbook.OrderBookManager;
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
    protected OrderBookManager createOrderBookManager() {
        return new OrderBookManager(super.service, Signal.never());
    }

    /**
     * Emulate execution event.
     */
    public VerifiableMarket perform(Execution e) {
        return perform(e, e.date.isAfter(service.now()) ? e.date : service.now());
    }

    /**
     * Emulate execution event with time lag.
     * 
     * @param e An emulated {@link Execution}.
     * @param lag A lag time. (second)
     * @return
     */
    public VerifiableMarket perform(Execution e, int lag) {
        return perform(e, service.now().plusSeconds(lag));
    }

    private VerifiableMarket perform(Execution e, ZonedDateTime date) {
        ((Execution.ÅssignableÅrbitrary) e).date(date);
        service.emulate(e, timelineObservers);
        return this;
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
                Execution copy = Execution.with.direction(e.orientation, e.size)
                        .price(e.price)
                        .id(e.id ^ (97 + i))
                        .date(service.now())
                        .delay(e.delay)
                        .consecutive(e.orientation.isPositive() ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller);

                perform(copy);
            }
        }
    }

    /**
     * Emulate order and execution event.
     */
    public void requestAndExecution(Order order) {
        request(order).to(id -> {
            Execution e = Execution.with.direction(order.orientation, order.size)
                    .date(service.now())
                    .price(order.price.minus(order.orientation, service.setting.base.minimumSize));

            perform(e);
        });
    }

    /**
     * Elapse market time.
     * 
     * @param time
     * @param unit
     * @return
     */
    public final VerifiableMarket elapse(long time, TimeUnit unit) {
        service.elapse(time, unit);
        return this;
    }

    /**
     * Elapse market time.
     * 
     * @param time
     * @param unit
     * @return
     */
    public final VerifiableMarket elapse(long time, ChronoUnit unit) {
        service.elapse(time, TimeUnit.of(unit));
        return this;
    }

    /**
     * Elapse market time.
     * 
     * @param time
     * @return
     */
    public final VerifiableMarket elapse(Duration time) {
        service.elapse(time.toMillis(), TimeUnit.MILLISECONDS);
        return this;
    }
}