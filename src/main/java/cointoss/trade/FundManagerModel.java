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
interface FundManagerModel {
    /**
     * Your total assets.
     * 
     * @return
     */
    @Icy.Property
    Num totalAssets();

    @Icy.Overload("totalAssets")
    private Num totalAssets(long value) {
        return Num.of(value);
    }

    @Icy.Overload("totalAssets")
    private Num totalAssets(double value) {
        return Num.of(value);
    }

    /**
     * Config the acceptable risk asset ratio.
     * 
     * @return
     */
    @Icy.Property
    default double riskAssetsRatio() {
        return 0.01;
    }

    /**
     * Risk assets ratio can accepts between 0.001 and 0.5.
     * 
     * @param ratio
     * @return
     */
    @Icy.Intercept("riskAssetsRatio")
    private double validateRiskAssetsRatio(double ratio) {
        if (ratio < 0.001) {
            ratio = 0.001;
        }

        if (0.05 < ratio) {
            ratio = 0.05;
        }
        return ratio;
    }

    /**
     * Compute risk assets.
     * 
     * @return
     */
    default Num riskAssets() {
        return totalAssets().multiply(riskAssetsRatio());
    }

    /**
     * Config losscut range.
     * 
     * @return
     */
    @Icy.Property(mutable = true, copiable = true)
    default Num losscutRange() {
        return Num.of(-2000);
    }

    @Icy.Intercept("losscutRange")
    private Num validateLosscutRange(Num value) {
        if (value.isPositive()) {
            value = Num.ZERO;
        }
        return value;
    }

    /**
     * Config losscut range.
     * 
     * @return
     */
    default Num profitRange() {
        return losscutRange().multiply(riskRewardRatio()).abs();
    }

    /**
     * Config losscut range.
     * 
     * @return
     */
    @Icy.Property(mutable = true, copiable = true)
    default double riskRewardRatio() {
        return 1.5;
    }

    @Icy.Intercept("riskRewardRatio")
    private double validateRiskRewardRatio(double value) {
        if (value < 0.1) {
            value = 0.1;
        }
        return value;
    }

    /**
     * Market histrical volatility.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    default Num historicalVolatility() {
        return Num.ZERO;
    }

    /**
     * Market liquidity.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    default Num liquidity() {
        return Num.ZERO;
    }
}
