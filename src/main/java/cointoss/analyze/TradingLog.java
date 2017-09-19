/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import static eu.verdelhan.ta4j.Decimal.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import cointoss.Execution;
import cointoss.Market;
import cointoss.Trading;
import cointoss.Trading.Entry;
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/11 18:31:10
 */
public class TradingLog {

    /** The duration format. */
    private static final DateTimeFormatter durationHM = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm");

    /** summary */
    public LongSummary orderTime = new LongSummary();

    /** summary */
    public LongSummary holdTime = new LongSummary();

    /** summary */
    public AmountSummary profit = new AmountSummary();

    /** summary */
    public AmountSummary loss = new AmountSummary();

    /** summary */
    public AmountSummary profitAndLoss = new AmountSummary();

    /** The max draw down. */
    public Decimal drawDown = Decimal.ZERO;

    /** The max draw down. */
    public Decimal drawDownRate = Decimal.ZERO;

    /** The max total profit and loss. */
    private Decimal maxTotalProfitAndLoss = Decimal.ZERO;

    /** A number of created positions. */
    public int total = 0;

    /** A number of active positions. */
    public int active = 0;

    /** A number of completed positions. */
    public int complete = 0;

    /** A number of canceled positions. */
    public int cancel = 0;

    /** The starting date. */
    public final ZonedDateTime start;

    /** The starting trading rate. */
    public final Decimal startPrice;

    /** The starting base currency. */
    public final Decimal startBaseCurrency;

    /** The starting target currency. */
    public final Decimal startTargetCurrency;

    /** The finishing date. */
    public final ZonedDateTime finish;

    /** The finishing trading rate. */
    public final Decimal finishPrice;

    /** The finishing base currency. */
    public final Decimal finishBaseCurrency;

    /** The finishing target currency. */
    public final Decimal finishTargetCurrency;

    /**
     * Analyze trading.
     */
    public TradingLog(Market market, List<Trading> tradings) {
        Execution init = market.getExecutionInit();
        Execution last = market.getExecutionLatest();
        this.start = init.exec_date;
        this.startPrice = init.price;
        this.startBaseCurrency = market.getBaseInit();
        this.startTargetCurrency = market.getTargetInit();
        this.finish = last.exec_date;
        this.finishPrice = last.price;
        this.finishBaseCurrency = market.getBase();
        this.finishTargetCurrency = market.getTarget();

        for (Trading trading : tradings) {
            for (Entry entry : trading.entries) {
                // skip not activated entry
                if (entry.isInitial()) {
                    if (entry.isCanceled()) {
                        cancel++;
                    }
                    continue;
                }

                total++;
                if (entry.isActive()) active++;
                if (entry.isCompleted()) complete++;

                // calculate order and hold time
                orderTime.add(entry.orderTime().time());
                holdTime.add(entry.holdTime().time());

                // calculate profit and loss
                Decimal profitOrLoss = entry.profit();
                profitAndLoss.add(profitOrLoss);
                if (profitOrLoss.isPositive()) profit.add(profitOrLoss);
                if (profitOrLoss.isNegative()) loss.add(profitOrLoss);
                maxTotalProfitAndLoss = maxTotalProfitAndLoss.max(profitAndLoss.total);
                drawDown = drawDown.max(maxTotalProfitAndLoss.minus(profitAndLoss.total));
                drawDownRate = maxTotalProfitAndLoss.isZero() ? drawDownRate
                        : drawDownRate.max(drawDown.dividedBy(maxTotalProfitAndLoss).multipliedBy(HUNDRED).scale(2));
            }
        }
    }

    /**
     * Calculate total assets.
     * 
     * @return
     */
    public Decimal asset() {
        return startBaseCurrency.plus(startTargetCurrency.multipliedBy(startPrice)).plus(profitAndLoss.total);
    }

    /**
     * Calculate profit and loss.
     * 
     * @return
     */
    public Decimal profit() {
        Decimal baseProfit = finishBaseCurrency.minus(startBaseCurrency);
        Decimal targetProfit = finishTargetCurrency.multipliedBy(finishPrice).minus(startTargetCurrency.multipliedBy(startPrice));
        return baseProfit.plus(targetProfit);
    }

    /**
     * Calculate winning rate.
     */
    public Decimal winningRate() {
        return Decimal.valueOf(profit.size).dividedBy(Decimal.valueOf(profitAndLoss.size)).multipliedBy(HUNDRED).scale(1);
    }

    /**
     * Calculate profit factor.
     */
    public Decimal profitFactor() {
        return profit.total.dividedBy(loss.total.isZero() ? Decimal.ONE : loss.total.abs()).scale(3);
    }

    /**
     * Format {@link Decimal}.
     * 
     * @param base
     * @return
     */
    private String format(ZonedDateTime time, Decimal price, Decimal base, Decimal target) {
        return durationHM.format(time) + " " + base.asJPY() + "\t" + target.asBTC() + "(" + price
                .asJPY(1) + ")\t総計" + base.plus(target.multipliedBy(price)).asJPY();
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
                .append(" ")
                .append(String.format("総%d 済%d 残%d 中止%d)%n", total, complete, active, cancel));
        builder.append("開始 ").append(format(start, startPrice, startBaseCurrency, startTargetCurrency)).append("\r\n");
        builder.append("終了 ")
                .append(format(finish, finishPrice, finishBaseCurrency, finishTargetCurrency))
                .append(" (損益 " + profit().asJPY(1))
                .append(")\r\n");

        return builder.toString();
    }
}
