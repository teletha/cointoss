/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.MarketService;
import cointoss.util.arithmetic.Num;
import kiss.Variable;

public class Funds {

    public final Variable<Num> assets = Variable.of(Num.ZERO);

    public final Variable<Num> acceptableRiskAssetsRatio = Variable.of(Num.of("0.01"))
            .intercept((prev, now) -> Num.between(Num.of("0.001"), now, Num.of("0.05")));

    /**
     * Load markert info.
     * 
     * @param service
     */
    public final void assign(MarketService service) {
        service.baseCurrency().to(assets::set);
    }

    /**
     * Estimate the total risk assets.
     * 
     * @return
     */
    public Num riskAsserts() {
        return assets.v.multiply(acceptableRiskAssetsRatio.v);
    }
}
