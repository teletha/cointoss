/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.execution.Execution;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;

public abstract class VerifiableScenario extends Scenario {

    protected VerifiableMarket market = new VerifiableMarket();

    protected Scenario verify() {
        activate(market);

        return scenarios.peekLast();
    }

    /**
     * Timing function.
     * 
     * @return
     */
    protected final Signal<?> now() {
        return I.signal("ok");
    }

    protected final void entry(Execution e) {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(e, e.size, Orderable::take);
            }

            @Override
            protected void exit() {
            }
        });
        market.perform(e);
    }
}