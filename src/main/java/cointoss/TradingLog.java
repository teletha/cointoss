/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import cointoss.Trading.Entry;
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/11 18:31:10
 */
public class TradingLog {

    /** The duration format. */
    private static final DateTimeFormatter durationHM = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm");

    /** The duration format. */
    private static final DateTimeFormatter durationHMS = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm:ss");

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

    /**
     * Analyze trading.
     */
    public TradingLog(Market market, List<Trading> tradings) {
        int total = 0;
        int active = 0;
        int cancel = 0;

        for (Trading trading : tradings) {
            for (Entry entry : trading.entries) {
                total++;

                if (entry.entry.isCanceled()) {
                    cancel++;
                    continue;
                }

                // calculate entry cost
                Decimal entrySize = entry.entry.executed_size;
                Decimal entryCost = entry.entry.average_price.multipliedBy(entrySize);

                // calculate order time
                ZonedDateTime start = entry.entry.child_order_date;
                ZonedDateTime finish = entry.entry.executions.get(entry.entry.executions.size() - 1).exec_date;
                orderTime.add(Duration.between(start, finish).getSeconds());

                // calculate hold time and profit
                start = entry.entry.executions.get(0).exec_date;
                finish = start;
                Decimal exitSize = Decimal.ZERO;
                Decimal exitCost = Decimal.ZERO;
                boolean hasActiveExit = false;

                for (Order exit : entry.exit) {
                    exitSize = exitSize.plus(exit.executed_size);
                    exitCost = exitCost.plus(exit.average_price.multipliedBy(exit.executed_size));

                    if (exit.isAllCompleted()) {
                        finish = max(finish, exit.executions.get(exit.executions.size() - 1).exec_date);
                    } else {
                        hasActiveExit = true;
                        finish = market.getExecutionLatest().exec_date;
                    }
                }

                if (entry.exit.isEmpty()) {
                    exitCost = market.getLatestPrice();
                }

                if (entry.exit.isEmpty() || hasActiveExit) {
                    active++;
                }

                // calculate profit and loss
                Decimal entryProfit;

                if (entry.isBuy()) {
                    entryProfit = exitCost.minus(entryCost);
                } else {
                    entryProfit = entryCost.minus(exitCost);
                }

                holdTime.add(Duration.between(start, finish).getSeconds());
                profitAndLoss.add(entryProfit);

                if (entryProfit.isPositive()) {
                    profit.add(entryProfit);
                } else {
                    loss.add(entryProfit);
                }

                // show bad orders
                System.out.println(new StringBuilder() //
                        .append("注文 ")
                        .append(durationHMS.format(start))
                        .append("～")
                        .append(start == finish ? "\t\t" : durationHMS.format(finish))
                        .append("\t 損益")
                        .append(entryProfit.asJPY(4))
                        .append("\t")
                        .append(exitSize)
                        .append("/")
                        .append(entrySize)
                        .append("@")
                        .append(entry.side().mark())
                        .append(entry.entry.average_price.asJPY(1))
                        .append(" → ")
                        .append(exitSize.isZero() ? "" : exitCost.dividedBy(exitSize).asJPY(1))
                        .append("\t")
                        .append(entry.entry.description() == null ? "" : entry.entry.description())
                        .toString());

                for (String log : entry.logs) {
                    System.out.println("   " + log);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("発注 ").append(orderTime);
        builder.append("保持 ").append(holdTime);
        builder.append("損失 ").append(loss).append("\r\n");
        builder.append("利益 ").append(profit).append("\r\n");
        builder.append("取引 最小")
                .append(profitAndLoss.min.asJPY(7))
                .append("\t最大")
                .append(profitAndLoss.max.asJPY(7))
                .append("\t平均")
                .append(profitAndLoss.mean().asJPY(7))
                .append("\t合計")
                .append(profitAndLoss.total.asJPY(12))
                .append(" (勝率")
                .append((profitAndLoss.positive * 100 / Math.max(profitAndLoss.size, 1)))
                .append("% ")
                .append(String.format("総%d 済%d 残%d 中止%d)%n", total, total - active - cancel, active, cancel));
        builder.append("開始 ").append(format(market.getExecutionInit(), market.getBaseInit(), market.getTargetInit())).append("\r\n");
        builder.append("終了 ")
                .append(format(market.getExecutionLatest(), market.getBase(), market.getTarget()))
                .append(" (損益 " + market.calculateProfit().asJPY(1))
                .append(")\r\n");

        System.out.println(builder);
    }

    /**
     * Helper to detect latest date.
     * 
     * @param one
     * @param other
     * @return
     */
    private ZonedDateTime max(ZonedDateTime one, ZonedDateTime other) {
        return one.isBefore(other) ? other : one;
    }

    /**
     * Calculate profit and loss.
     * 
     * @return
     */
    private Decimal calculateTradeProfit(Order entry, Order exit) {
        if (entry.isBuy()) {
            return exit.isBuy() ? Decimal.ZERO : exit.average_price.minus(entry.average_price).multipliedBy(exit.executed_size);
        } else {
            return exit.isBuy() ? entry.average_price.minus(exit.average_price).multipliedBy(exit.executed_size) : Decimal.ZERO;
        }
    }

    /**
     * Format {@link Decimal}.
     * 
     * @param base
     * @return
     */
    private String format(Execution e, Decimal base, Decimal target) {
        return durationHM.format(e.exec_date) + " " + base.asJPY() + "\t" + target.asBTC() + "(" + e.price
                .asJPY(1) + ")\t総計" + base.plus(target.multipliedBy(e.price)).asJPY();
    }

    /**
     * @version 2017/08/30 20:45:02
     */
    private static class LongSummary {

        /** MAX value. */
        private long min = Long.MAX_VALUE;

        /** MIN value. */
        private long max = Long.MIN_VALUE;

        /** Total value. */
        private long total = 0;

        /** Number of values. */
        private int size = 0;

        /**
         * Add new value to summarize.
         * 
         * @param value
         */
        private void add(long value) {
            min = Math.min(min, value);
            max = Math.max(max, value);
            total += value;
            size++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "最小" + duration(min) + "\t最大" + duration(max) + "\t平均" + duration(total / Math.max(1, size)) + "\r\n";
        }

        /**
         * Format duration.
         * 
         * @param time seconds
         * @return
         */
        private String duration(long time) {
            if (time == Long.MAX_VALUE) {
                return "";
            }
            return DurationFormatUtils.formatDuration(time * 1000, "HH:mm:ss");
        }
    }

    /**
     * @version 2017/08/30 20:45:02
     */
    /**
     * @version 2017/09/04 14:13:21
     */
    private static class AmountSummary {

        /** MAX value. */
        private Decimal min = Decimal.MAX;

        /** MIN value. */
        private Decimal max = Decimal.ZERO;

        /** Total value. */
        private Decimal total = Decimal.ZERO;

        /** Number of values. */
        private int size = 0;

        /** Number of positive values. */
        private int positive = 0;

        /**
         * Calculate mean.
         * 
         * @return
         */
        private Decimal mean() {
            return total.dividedBy(Math.max(size, 1));
        }

        /**
         * Add new value to summarize.
         * 
         * @param value
         */
        private void add(Decimal value) {
            min = min.min(value);
            max = max.max(value);
            total = total.plus(value);
            size++;
            if (value.isPositive()) positive++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return new StringBuilder().append("最小")
                    .append(min.asJPY(7))
                    .append("\t最大")
                    .append(max.asJPY(7))
                    .append("\t平均")
                    .append(mean().asJPY(7))
                    .append("\t合計")
                    .append(total.asJPY(12))
                    .toString();
        }
    }
}
