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
    public Num drawDownRate = Num.ZERO;

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

    /** The starting date. */
    public final ZonedDateTime start;

    /** The starting trading rate. */
    public final Num startPrice;

    /** The finishing date. */
    public final ZonedDateTime finish;

    /** The finishing trading rate. */
    public final Num finishPrice;

    /**
     * Analyze trading.
     */
    public TradingLog(List<Entry> entries) {
        for (Entry entry : entries) {
            total++;
            if (entry.isActive()) active++;
            if (entry.isTerminated()) terminated++;

            // calculate order and hold time
            orderTime.add(entry.orderTime());
            holdTime.add(entry.holdTime());

            // calculate profit and loss
            Num profitOrLoss = entry.profit();
            profitAndLoss.add(profitOrLoss);
            if (profitOrLoss.isPositive()) profit.add(profitOrLoss);
            if (profitOrLoss.isNegative()) loss.add(profitOrLoss);
            maxTotalProfitAndLoss = Num.max(maxTotalProfitAndLoss, profitAndLoss.total());
            drawDown = Num.max(drawDown, maxTotalProfitAndLoss.minus(profitAndLoss.total()));
            drawDownRate = Num.max(drawDownRate, drawDown.divide(assetInitial().plus(maxTotalProfitAndLoss)).multiply(HUNDRED).scale(1));
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
        StringBuilder builder = new StringBuilder();
        builder.append("発注 ").append(orderTime);
        builder.append("保持 ").append(holdTime);
        builder.append("損失 ").append(loss).append("\r\n");
        builder.append("利益 ").append(profit).append("\r\n");
        builder.append("取引 ")
                .append(profitAndLoss) //
                .append(" (勝率")
                .append(winningRate())
                .append("% ")
                .append(" PF")
                .append(profitFactor())
                .append(" DD")
                .append(drawDownRate)
                .append("% ")
                .append(String.format("総%d 済%d 残%d 中止%d)%n", total, terminated, active, cancel));
        builder.append("開始 ").append(format(start, startPrice, startBaseCurrency, startTargetCurrency)).append("\r\n");
        builder.append("終了 ")
                .append(format(finish, finishPrice, finishBaseCurrency, finishTargetCurrency))
                .append(" (損益 " + profit().asJPY(1))
                .append(")\r\n");

        return builder.toString();
    }
}
