/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import static cointoss.util.arithmetic.Num.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Iterables;

import cointoss.Market;
import cointoss.order.OrderState;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Variable;
import kiss.WiseConsumer;

public class TradingStats {

    /** The associated trader. */
    public final String name;

    /** The trader's properties. */
    public final Map<String, Object> properties = new HashMap();

    /** The start date-time. */
    public final ZonedDateTime startDate;

    /** The end date-time. */
    public final ZonedDateTime endDate;

    /** summary */
    public final NumStats holdTime = new NumStats().formatter(Chrono::formatAsDuration);

    /** summary */
    public final NumStats holdTimeOnProfitTrade = new NumStats().formatter(Chrono::formatAsDuration);

    /** summary */
    public final NumStats holdTimeOnLossTrade = new NumStats().formatter(Chrono::formatAsDuration);

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
    public final NumStats profit = new NumStats().formatter(baseCurrencyFormatter);

    /** summary */
    public final NumStats profitRange = new NumStats().formatter(baseCurrencyFormatter);

    /** summary */
    public final NumStats realizedProfit = new NumStats().formatter(baseCurrencyFormatter);

    /** summary */
    public final NumStats realizedProfitRange = new NumStats().formatter(baseCurrencyFormatter);

    /** summary */
    public final NumStats unrealizedProfit = new NumStats().formatter(baseCurrencyFormatter);

    /** summary */
    public final NumStats unrealizedProfitRange = new NumStats().formatter(baseCurrencyFormatter);

    /** summary */
    public final NumStats loss = new NumStats().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final NumStats lossRange = new NumStats().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final NumStats realizedLoss = new NumStats().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final NumStats realizedLossRange = new NumStats().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final NumStats unrealizedLoss = new NumStats().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final NumStats unrealizedLossRange = new NumStats().formatter(baseCurrencyFormatter).negative();

    /** summary */
    public final NumStats profitAndLoss = new NumStats().formatter(baseCurrencyFormatter);

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

    /** All scenario. */
    private final List<Scenario> scenarios;

    /**
     * Analyze trading.
     */
    public TradingStats(Market market, Funds funds, List<Scenario> scenarios, Trader trader) {
        this.name = trader.name();
        this.startDate = Variable.of(Iterables.getFirst(scenarios, null)).map(Scenario::holdStartTime).or(market.service::now);
        this.endDate = Variable.of(Iterables.getLast(scenarios, null)).map(Scenario::holdEndTime).or(market.service::now);
        this.baseCurrencyScale = market.service.setting.base.scale;
        this.targetCurrencyScale = market.service.setting.target.scale;
        this.holdMaxSize = trader.holdMaxSize;
        this.holdCurrentSize = trader.holdSize;
        this.scenarios = scenarios;

        // extract trader's properties
        I.signal(trader)
                .flatArray(t -> t.getClass().getDeclaredFields())
                .take(f -> Modifier.isPublic(f.getModifiers()))
                .to((WiseConsumer<Field>) f -> {
                    properties.put(f.getName(), f.get(trader));
                });

        for (Scenario scenario : scenarios) {
            if (scenario.state.is(OrderState.CANCELED)) {
                continue;
            }

            total++;
            if (scenario.state.is(OrderState.ACTIVE)) active++;
            if (scenario.state.is(OrderState.COMPLETED)) terminated++;

            // calculate order and hold time
            long hold = scenario.holdTime().toMillis();
            holdTime.add(hold);

            // calculate profit and loss
            Num realized = scenario.realizedProfit;
            Num unrealized = scenario.unrealizedProfit(market.tickers.latest.v.price);
            Num pol = realized.plus(unrealized);
            Num pips = scenario.entryExecutedSize.isZero() ? Num.ZERO : realized.divide(scenario.entryExecutedSize);
            Num realizedPips = scenario.entryExecutedSize.isZero() ? Num.ZERO : realized.divide(scenario.entryExecutedSize);
            Num unrealizedPips = scenario.entryExecutedSize.isZero() ? Num.ZERO : unrealized.divide(scenario.entryExecutedSize);

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
            drawDownRatio = Num.max(drawDownRatio, drawDown.divide(Num.max(Num.ONE, funds.assets.v.plus(maxTotalProfitAndLoss))).scale(3));
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
     * @return
     */
    public final String showByText(boolean detail) {
        StringBuilder builder = new StringBuilder();
        showByText(builder, detail);
        return builder.toString();
    }

    /**
     * Output result by text format.
     */
    public final void showByText(Appendable builder, boolean detail) {
        try {
            String EOL = "\r\n";

            if (detail) {
                for (Scenario scenario : scenarios) {
                    builder.append(scenario.toString()).append(EOL);
                }
            }

            String time = duration.truncatedTo(ChronoUnit.MILLIS).toString().substring(2).toLowerCase();
            builder.append("期間　\t")
                    .append(Chrono.Date.format(startDate))
                    .append(" ～ ")
                    .append(Chrono.Date.format(endDate))
                    .append("\t実行時間 ")
                    .append(time)
                    .append(EOL);
            builder.append("枚数  \t現在").append(holdCurrentSize.toString()).append("  \t最大").append(holdMaxSize.toString()).append(EOL);
            builder.append("時間  \t").append(holdTime.toString()).append(EOL).append(EOL);
            builder.append("利幅  \t").append(profitRange.toString()).append(EOL);
            builder.append("含利幅 \t").append(unrealizedProfitRange.toString()).append(EOL);
            builder.append("損幅  \t").append(lossRange.toString()).append(EOL);
            builder.append("含損幅 \t").append(unrealizedLossRange.toString()).append(EOL).append(EOL);
            builder.append("総合  \t").append(profitAndLoss.toString()).append(EOL);
            builder.append("勝率 ")
                    .append(winningRate().toString())
                    .append("%  PF")
                    .append(profitFactor().toString())
                    .append("  DD")
                    .append(drawDownRatio.multiply(100).toString())
                    .append("%  総数")
                    .append(String.valueOf(total))
                    .append("  決済")
                    .append(String.valueOf(terminated))
                    .append("  未決済")
                    .append(String.valueOf(active))
                    .append(EOL);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return showByText(false);
    }
}