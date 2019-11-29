/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import static cointoss.util.Num.HUNDRED;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.eclipse.collections.impl.list.mutable.FastList;

import cointoss.FundManager;
import cointoss.Market;
import cointoss.Scenario;
import cointoss.Trader;
import cointoss.util.Chrono;
import cointoss.util.Num;

public class TradingStatistics {

    /** The associated trader. */
    public final String name;

    /** The start date-time. */
    public final ZonedDateTime startDate;

    /** The end date-time. */
    public final ZonedDateTime endDate;

    /** summary */
    public final Statistics holdTime = new Statistics().formatter(Chrono::formatAsDuration);

    /** summary */
    public final Statistics holdTimeOnProfitTrade = new Statistics().formatter(Chrono::formatAsDuration);

    /** summary */
    public final Statistics holdTimeOnLossTrade = new Statistics().formatter(Chrono::formatAsDuration);

    /** The base currency scale. */
    private int baseCurrencyScale;

    /** The base currency format. */
    private final Function<Num, String> baseCurrencyFormatter = num -> num.scale(baseCurrencyScale)
            .format(NumberFormat.getNumberInstance());

    /** The target currency scale. */
    private int targetCurrencyScale;

    /** The target currency format. */
    private final Function<Num, String> targetCurrencyFormatter = num -> num.scale(targetCurrencyScale)
            .format(NumberFormat.getNumberInstance());

    /** summary. */
    public final Num holdMaxSize;

    /** summary. */
    public final Num holdCurrentSize;

    /** summary */
    public final Statistics profit = new Statistics().formatter(baseCurrencyFormatter);

    /** summary */
    public final Statistics profitRange = new Statistics().formatter(baseCurrencyFormatter);

    /** summary */
    public final Statistics realizedProfit = new Statistics().formatter(baseCurrencyFormatter);

    /** summary */
    public final Statistics realizedProfitRange = new Statistics().formatter(baseCurrencyFormatter);

    /** summary */
    public final Statistics unrealizedProfit = new Statistics().formatter(baseCurrencyFormatter);

    /** summary */
    public final Statistics unrealizedProfitRange = new Statistics().formatter(baseCurrencyFormatter);

    /** summary */
    public final Statistics loss = new Statistics().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final Statistics lossRange = new Statistics().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final Statistics realizedLoss = new Statistics().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final Statistics realizedLossRange = new Statistics().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final Statistics unrealizedLoss = new Statistics().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final Statistics unrealizedLossRange = new Statistics().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final Statistics profitAndLoss = new Statistics().formatter(baseCurrencyFormatter);

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

    /** The exected duration. */
    public Duration duration = Duration.ZERO;

    FastList<Scenario> entries;

    /**
     * Analyze trading.
     */
    public TradingStatistics(Market market, FundManager funds, FastList<Scenario> entries, Trader trader) {
        this.name = trader.name();
        this.startDate = entries.getFirstOptional().map(Scenario::holdStartTime).orElseGet(market.service::now);
        this.endDate = entries.getLastOptional().map(Scenario::holdEndTime).orElseGet(market.service::now);
        this.baseCurrencyScale = market.service.setting.baseCurrencyScaleSize;
        this.targetCurrencyScale = market.service.setting.targetCurrencyScaleSize;
        this.holdMaxSize = trader.holdMaxSize;
        this.holdCurrentSize = trader.holdSize;
        this.entries = entries;

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
                holdTimeOnProfitTrade.add(hold);
            } else if (pol.isNegative()) {
                loss.add(pol);
                lossRange.add(pips);
                holdTimeOnLossTrade.add(hold);
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

        if (detail) {
            for (Scenario scenario : entries) {
                System.out.println(scenario);
            }
        }

        builder.append("実行時間 ").append(Chrono.formatAsDuration(duration.toMillis())).append(EOL);
        builder.append("枚数 現在").append(holdCurrentSize).append(" 最大").append(holdMaxSize).append(EOL);
        builder.append("時間 ").append(holdTime).append(EOL);
        builder.append("利幅 ").append(profitRange).append(EOL);
        builder.append("含利幅 ").append(unrealizedProfitRange).append(EOL);
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