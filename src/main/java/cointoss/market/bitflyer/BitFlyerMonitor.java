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

import java.time.ZonedDateTime;

import cointoss.Execution;
import cointoss.Market;
import cointoss.Trading;

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

        // AtomicReference<Tick> latest = new AtomicReference();
        //
        // market.second5.to(tick -> {
        // Tick prev1 = latest.getAndSet(tick);
        //
        // if (prev1 == null) {
        // return;
        // }
        //
        // Num priceDiff = tick.closePrice.minus(prev1.closePrice);
        // Num volumeDiff = tick.longVolume.minus(tick.shortVolume).scale(1);
        // Num longVolumeDiff = tick.longVolume.minus(prev1.longVolume).scale(1);
        // Num longVolumeRatio = tick.longVolume.divide(prev1.longVolume).scale(1);
        // Num shortVolumeDiff = tick.shortVolume.minus(prev1.shortVolume).scale(1);
        // Num shortVolumeRatio = tick.shortVolume.divide(prev1.shortVolume).scale(1);
        //
        // Num power = tick.upRatio().minus(tick.downRatio()).scale(3);
        //
        // StringBuilder builder = new StringBuilder();
        // builder.append(tick.closePrice).append("(P").append(priceDiff).append("
        // V").append(volumeDiff).append(")\t");
        // builder.append("L")
        // .append("(")
        // .append(longVolumeRatio)
        // .append(" ")
        // .append(longVolumeDiff)
        // .append(" ")
        // .append(tick.upRatio().scale(1))
        // .append(")")
        // .append(" S")
        // .append("(")
        // .append(shortVolumeRatio)
        // .append(" ")
        // .append(shortVolumeDiff)
        // .append(" ")
        // .append(tick.downRatio().scale(1))
        // .append(")")
        // .append("\t");
        // builder.append("Volatility").append("(").append(power).append("
        // ").append(priceDiff.divide(power).scale(5)).append(")\t");
        //
        // Tick minute = market.minute5.ticks.latest();
        // builder.append("MINUTE L").append(minute.longVolume.scale(1)).append("
        // S").append(minute.shortVolume.scale(1)).append("\t");
        //
        // if (priceDiff.isZero()) {
        // builder.append("均衡");
        // } else if (priceDiff.isNegative()) {
        // if (volumeDiff.isPositive()) {
        // builder.append("上げ止まり");
        // } else {
        // builder.append("下げ");
        // }
        // } else {
        // if (volumeDiff.isPositive()) {
        // builder.append("上げ");
        // } else {
        // builder.append("下げ止まり");
        // }
        // }
        // System.out.println(builder);
        // });

        // market.flow.to(flow -> {
        // System.out.println(flow.history.latest().volume + " (" + flow.history.latest().id + ") "
        // + flow.history
        // .latest(1).volume + "(" + flow.history.latest(1).id + ")");
        // });

        market.timeline.take(e -> e.cumulativeSize.isGreaterThan(5)).to(e -> {
            System.out.println(e + "   " + e.cumulativeSize);
        });
        // market.executions(500, (prev, flow) -> {
        // Num longDiff = flow.longVolume.minus(prev.longVolume);
        // Num shortDiff = flow.shortVolume.minus(prev.shortVolume);
        //
        // Num longPriceDiff = flow.longPriceIncrese.minus(prev.longPriceIncrese);
        // Num shortPriceDiff = flow.shortPriceDecrease.minus(prev.shortPriceDecrease);
        //
        // StringBuilder builder = new StringBuilder();
        // builder.append(flow.price).append(" ");
        // builder.append(flow.volume().format(2)).append(" \t");
        // builder.append(longDiff.minus(shortDiff).format(2)).append("\t");
        // builder.append(longPriceDiff.minus(shortPriceDiff).format(0)).append("\t");
        // builder.append(flow.longVolume.scale(2)).append("(").append(longDiff.format(2)).append(")\t
        // ");
        // builder.append(flow.shortVolume.scale(2)).append("(").append(shortDiff.format(2)).append(")\t");
        // builder.append(flow.power().format(0)).append(" ");
        //
        // System.out.println(builder);
        // });
    }

    private ZonedDateTime last = ZonedDateTime.of(1971, 1, 1, 0, 0, 0, 0, Execution.UTC);

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Market market = new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().fromLast(3), new BitFlyerMonitor());
    }
}
