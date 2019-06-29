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

import java.util.List;
import java.util.function.Function;

import cointoss.Market;
import cointoss.analyze.Statistics;
import cointoss.trade.Trader.Entry;
import cointoss.util.Chrono;
import cointoss.util.Num;

public class TradingLog {

    /** summary */
    public final Statistics holdTime = new Statistics().formatter(Chrono::formatAsDuration);

    /** summary */
    public final Statistics profit;

    /** summary */
    public final Statistics profitRange;

    /** summary */
    public final Statistics loss;

    /** summary */
    public final Statistics lossRange;

    /** summary */
    public final Statistics profitAndLoss;

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

    /** The all entries. */
    private final List<Entry> entries;

    /**
     * Analyze trading.
     */
    public TradingLog(Market market, FundManager funds, List<Entry> entries) {
        Function<Num, String> format = v -> v.scale(market.service.setting.baseCurrencyScaleSize).toString();
        this.profit = new Statistics().formatter(format);
        this.profitRange = new Statistics().formatter(format);
        this.loss = new Statistics().formatter(format).negative();
        this.lossRange = new Statistics().formatter(format).negative();
        this.profitAndLoss = new Statistics().formatter(format);
        this.entries = entries;

        for (Entry entry : entries) {
            total++;
            if (entry.isActive()) active++;
            if (entry.isTerminated()) terminated++;

            // calculate order and hold time
            holdTime.add(entry.holdTime().toMillis());

            // calculate profit and loss
            Num pol = entry.profit(market.latestPrice());
            Num pips = pol.divide(entry.entryExecutedSize);

            profitAndLoss.add(pol);
            if (pol.isPositiveOrZero()) {
                profit.add(pol);
                profitRange.add(pips);
            } else {
                loss.add(pol);
                lossRange.add(pips);
            }
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String EOL = "\r\n";

        StringBuilder builder = new StringBuilder();

        for (Entry entry : entries) {
            builder.append(entry);
        }
        builder.append("保持 ").append(holdTime).append(EOL);
        builder.append("利益 ").append(profit).append(EOL);
        builder.append("利幅 ").append(profitRange).append(EOL);
        builder.append("損失 ").append(loss).append(EOL);
        builder.append("損幅 ").append(lossRange).append(EOL);
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
