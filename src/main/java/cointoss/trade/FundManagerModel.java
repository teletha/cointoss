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

import cointoss.util.Num;
import icy.manipulator.Icy;

@Icy
public interface FundManagerModel {

    @Icy.Property
    Num totalAssets();

    @Icy.Property
    default Num riskRatio() {
        return Num.of(0.01);
    }

    default Num riskAsserts() {
        return totalAssets().multiply(riskRatio());
    }

    @Icy.Property
    default Num riskRweardRatio() {
        return Num.of(1.5);
    }

    default Num ruinProbability() {
        return null;
    }
}
