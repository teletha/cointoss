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

import static cointoss.util.Num.HUNDRED;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import cointoss.analyze.Statistics;
import cointoss.trade.Trader.Entry;
import cointoss.util.Num;

public class TradingLog {

    /** The duration format. */
    private static final DateTimeFormatter durationHM = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm");

    /** summary */
    public Statistics orderTime = new Statistics();

    /** summary */
    public Statistics holdTime = new Statistics();

    /** summary */
    public Statistics profit = new Statistics();

    /** summary */
    public Statistics loss = new Statistics();

    /** summary */
    public Statistics profitAndLoss = new Statistics();

    /** The max draw down. */
    public Num drawDown = Num.ZERO;

    /** The max draw down. */
    public Num drawDownRatio = Num.ZERO;

    /** The max total() profit and loss. */
    private Num maxTotalProfitAndLoss = Num.ZERO;

    /** A number of total entries. */
    public int total = 0;

    /** A number of active entries. */
    public int active = 0;

    /** A number of terminated entries. */
    public int terminated = 0;

    /** A number of canceled entries. */
    public int cancel = 0;

    /**
     * Analyze trading.
     */
    public TradingLog(FundManager funds, List<Entry> entries) {
        for (Entry entry : entries) {
            total++;
            if (entry.isActive()) active++;
            if (entry.isTerminated()) terminated++;

            // calculate order and hold time
            holdTime.add(entry.holdTime().toMillis());

            // calculate profit and loss
            profitAndLoss.add(entry.profit);
            if (entry.profit.isPositive()) profit.add(entry.profit);
            if (entry.profit.isNegative()) loss.add(entry.profit);
            maxTotalProfitAndLoss = Num.max(maxTotalProfitAndLoss, profitAndLoss.total());
            drawDown = Num.max(drawDown, maxTotalProfitAndLoss.minus(profitAndLoss.total()));
            drawDownRatio = Num.max(drawDownRatio, drawDown.divide(funds.totalAssets.plus(maxTotalProfitAndLoss)).scale(3));
        }
    }

    /**
     * Calculate winning rate.
     */
    public Num winningRate() {
        return profitAndLoss.size() == 0 ? Num.ZERO : Num.of(profit.size()).divide(Num.of(profitAndLoss.size())).multiply(HUNDRED).scale(1);
    }

    /**
     * Calculate profit factor.
     */
    public Num profitFactor() {
        return profit.total().divide(loss.total().isZero() ? Num.ONE : loss.total().abs()).scale(3);
    }

    /**
     * Format {@link Num}.
     * 
     * @param base
     * @return
     */
    private String format(ZonedDateTime time, Num price, Num base, Num target) {
        return durationHM.format(time) + " " + base.asJPY() + "\t" + target.asBTC() + "(" + price
                .asJPY(1) + ")\t総計" + base.plus(target.multiply(price)).asJPY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String EOL = "\r\n";

        StringBuilder builder = new StringBuilder();
        builder.append("発注 ").append(orderTime).append(EOL);
        builder.append("保持 ").append(holdTime).append(EOL);
        builder.append("損失 ").append(loss).append(EOL);
        builder.append("利益 ").append(profit).append(EOL);
        builder.append("取引 ")
                .append(profitAndLoss) //
                .append(" (勝率")
                .append(winningRate())
                .append("% ")
                .append(" PF")
                .append(profitFactor())
                .append(" DD")
                .append(drawDownRatio)
                .append("% ")
                .append("総")
                .append(total)
                .append(" 済")
                .append(terminated)
                .append(" 残")
                .append(active)
                .append(" 中止")
                .append(cancel)
                .append(")")
                .append(EOL);

        return builder.toString();
    }
}
