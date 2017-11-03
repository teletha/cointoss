/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.util.concurrent.atomic.AtomicReference;

import cointoss.Market;
import cointoss.Trading;
import cointoss.chart.Tick;
import cointoss.util.Num;

/**
 * @version 2017/09/08 18:40:12
 */
public class BitFlyerMonitor extends Trading {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // market.observeExecutionBySize(20).to(exe -> {
        // System.out.println("大口 " + exe.exec_date.withNano(0).toLocalDateTime().plusHours(9) + " "
        // + exe.side
        // .mark() + exe.cumulativeSize + " @" + exe.price);
        // });

        AtomicReference<Tick> latest = new AtomicReference();

        market.second5.to(tick -> {
            Tick prev1 = latest.getAndSet(tick);

            if (prev1 == null) {
                return;
            }

            Num priceDiff = tick.closePrice.minus(prev1.closePrice);
            Num volumeDiff = tick.longVolume.minus(tick.shortVolume).scale(1);
            Num volatilityDiff = tick.longPriceIncrese.minus(tick.shortPriceDecrease);
            Num longVolumeRatio = tick.longVolume.divide(prev1.longVolume).scale(1);
            Num shortVolumeRatio = tick.shortVolume.divide(prev1.shortVolume).scale(1);

            StringBuilder builder = new StringBuilder();
            builder.append(tick.closePrice).append("(P").append(priceDiff).append(" V").append(volumeDiff).append(")\t");
            builder.append("L")
                    .append(tick.longVolume.scale(1))
                    .append("(")
                    .append(longVolumeRatio)
                    .append(")")
                    .append(" S")
                    .append(tick.shortVolume.scale(1))
                    .append("(")
                    .append(shortVolumeRatio)
                    .append(")")
                    .append("\t");
            builder.append("Volatility").append(volatilityDiff).append("(").append(tick.priceVolatility().scale(1)).append(")\t");

            // まず下げている最中か上げている最中かを判断できないといけない

            if (priceDiff.isNegative() && volumeDiff.isPositive()) {
                builder.append("転換気配");
            }

            if (priceDiff.isPositive() && volumeDiff.isNegative()) {
                builder.append("転換気配");
            }
            System.out.println(builder);
        });
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Market market = new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().fromLast(3), new BitFlyerMonitor());
    }
}
