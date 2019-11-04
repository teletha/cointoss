/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.time.ZonedDateTime;

import cointoss.execution.Execution;
import cointoss.order.OrderStrategy.Orderable;

public abstract class TraderTestSupport {

    /**
     * Config delay.
     * 
     * @param delay
     * @return
     */
    protected final ZonedDateTime second(long delay) {
        return market.service.now().plusSeconds(delay);
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final VerifiableScenario entryAndExit(Execution e, Execution exit) {
        VerifiableScenario verifiable = new VerifiableScenario() {
            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(e, e.size, Orderable::take);
                });
            }

            @Override
            protected void exit() {
                exitAt(exit.price, Orderable::take);
            }
        };

        verifiable.market.perform(e);
        verifiable.market.perform(exit);
        verifiable.market.perform(exit);

        return verifiable;
    }
}
