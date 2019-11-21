/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import static cointoss.util.Num.*;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import cointoss.analyze.Statistics;
import cointoss.util.Chrono;
import cointoss.util.Num;

public class TradingLog {

    /** summary */
    public final Statistics holdTime = new Statistics().formatter(Chrono::formatAsDuration);

    /** summary */
    public final Statistics holdTimeProfit = new Statistics().formatter(Chrono::formatAsDuration);

    /** summary */
    public final Statistics holdTimeLoss = new Statistics().formatter(Chrono::formatAsDuration);

    /** summary */
    public final Statistics profit;

    /** summary */
    public final Statistics profitRange;

    /** summary */
    public final Statistics realizedProfit;

    /** summary */
    public final Statistics realizedProfitRange;

    /** summary */
    public final Statistics unrealizedProfit;

    /** summary */
    public final Statistics unrealizedProfitRange;

    /** summary */
    public final Statistics loss;

    /** summary */
    public final Statistics lossRange;

    /** summary */
    public final Statistics realizedLoss;

    /** summary */
    public final Statistics realizedLossRange;

    /** summary */
    public final Statistics unrealizedLoss;

    /** summary */
    public final Statistics unrealizedLossRange;

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

    /** The all entries. */
    private final List<Scenario> scenarios;

    /** The exected duration. */
    public Duration duration = Duration.ZERO;

    /**
     * Analyze trading.
     */
    public TradingLog(Market market, FundManager funds, List<Scenario> entries) {
        Function<Num, String> format = v -> v.scale(market.service.setting.baseCurrencyScaleSize).format(NumberFormat.getNumberInstance());
        this.profit = new Statistics().formatter(format);
        this.profitRange = new Statistics().formatter(format);
        this.realizedProfit = new Statistics().formatter(format);
        this.realizedProfitRange = new Statistics().formatter(format);
        this.unrealizedProfit = new Statistics().formatter(format);
        this.unrealizedProfitRange = new Statistics().formatter(format);
        this.loss = new Statistics().formatter(format).negative();
        this.lossRange = new Statistics().formatter(format).negative();
        this.realizedLoss = new Statistics().formatter(format).negative();
        this.realizedLossRange = new Statistics().formatter(format).negative();
        this.unrealizedLoss = new Statistics().formatter(format).negative();
        this.unrealizedLossRange = new Statistics().formatter(format).negative();
        this.profitAndLoss = new Statistics().formatter(format);
        this.scenarios = entries;

        for (Scenario entry : entries) {
            if (entry.isCanceled()) {
                continue;
            }

            total++;
            if (entry.isActive()) active++;
            if (entry.isTerminated()) terminated++;

            // calculate order and hold time
            long hold = entry.holdTime().toMillis();
            holdTime.add(hold);

            // calculate profit and loss
            Num realized = entry.realizedProfit;
            Num unrealized = entry.unrealizedProfit(market.tickers.latestPrice.v);
            Num pol = realized.plus(unrealized);
            Num pips = entry.entryExecutedSize.isZero() ? Num.ZERO : realized.divide(entry.entryExecutedSize);
            Num realizedPips = entry.entryExecutedSize.isZero() ? Num.ZERO : realized.divide(entry.entryExecutedSize);
            Num unrealizedPips = entry.entryExecutedSize.isZero() ? Num.ZERO : unrealized.divide(entry.entryExecutedSize);

            profitAndLoss.add(pol);
            if (pol.isPositive()) {
                profit.add(pol);
                profitRange.add(pips);
                holdTimeProfit.add(hold);
            } else if (pol.isNegative()) {
                loss.add(pol);
                lossRange.add(pips);
                holdTimeLoss.add(hold);
            }
            if (realized.isPositive()) {
                realizedProfit.add(realized);
                realizedProfitRange.add(realizedPips);
            } else if (realized.isNegative()) {
                realizedLoss.add(realized);
                realizedLossRange.add(realizedPips);
            }
            if (unrealized.isPositive()) {
                unrealizedProfit.add(unrealized);
                unrealizedProfitRange.add(unrealizedPips);
            } else if (unrealized.isNegative()) {
                unrealizedLoss.add(unrealized);
                unrealizedLossRange.add(unrealizedPips);
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
        return profitAndLoss.size() == 0 ? Num.ZERO : Num.of(profit.size()).divide(total).multiply(HUNDRED).scale(1);
    }

    /**
     * Calculate profit factor.
     */
    public Num profitFactor() {
        return profit.total().divide(loss.total().isZero() ? Num.ONE : loss.total().abs()).scale(3);
    }

    /**
     * Output result by text format.
     * 
     * @param detail
     * @return
     */
    public final String showByText(boolean detail) {
        String EOL = "\r\n";

        StringBuilder builder = new StringBuilder();

        for (Scenario scenario : scenarios) {
            if ((scenario.isActive()) || detail) {
                builder.append(scenario);
            }
        }

        builder.append("時間 ").append(holdTime).append("\t実行").append(Chrono.formatAsDuration(duration.toMillis())).append(EOL);
        // builder.append("利益 ").append(profit).append(EOL);
        builder.append("利幅 ").append(profitRange).append(EOL);
        builder.append("含利幅 ").append(unrealizedProfitRange).append(EOL);
        // builder.append("損失 ").append(loss).append(EOL);
        builder.append("損幅 ").append(lossRange).append(EOL);
        builder.append("含損幅 ").append(unrealizedLossRange).append(EOL);
        builder.append("総合 ")
                .append(profitAndLoss)
                .append("\t勝率")
                .append(winningRate())
                .append("% ")
                .append(" PF")
                .append(profitFactor())
                .append(" DD")
                .append(drawDownRatio.multiply(100))
                .append("% ")
                .append("総")
                .append(total)
                .append(" 済")
                .append(terminated)
                .append(" 残")
                .append(active)
                .append(EOL);

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return showByText(false);
    }
}
