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

    /** A number of created positions. */
    public int total = 0;

    /** A number of active positions. */
    public int active = 0;

    /** A number of completed positions. */
    public int complete = 0;

    /** A number of canceled positions. */
    public int cancel = 0;

    /**
     * Analyze trading.
     */
    public TradingLog(Market market, List<Trading> tradings) {
        ZonedDateTime latestTime = market.getExecutionLatest().exec_date;

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

                // calculate order time
                ZonedDateTime start = entry.order.child_order_date;
                ZonedDateTime finish = entry.order.executions.isEmpty() ? latestTime : entry.order.executions.getLast().exec_date;
                if (start.isBefore(finish)) orderTime.add(Duration.between(start, finish).getSeconds());

                // calculate hold time and profit
                finish = start;

                for (Order exit : entry.exit) {
                    if (exit.isCompleted()) {
                        finish = max(finish, exit.executions.getLast().exec_date);
                    } else if (!exit.isCanceled()) {
                        finish = market.getExecutionLatest().exec_date;
                    }
                }

                // calculate profit and loss
                Decimal profitOrLoss = entry.profit();
                profitAndLoss.add(profitOrLoss);
                if (profitOrLoss.isPositive()) profit.add(profitOrLoss);
                if (profitOrLoss.isNegative()) loss.add(profitOrLoss);
                if (start != finish) holdTime.add(Duration.between(start, finish).getSeconds());

                // show bad orders
                if (profitOrLoss.isNegative() || true) {
                    System.out.println(new StringBuilder() //
                            .append("注文 ")
                            .append(durationHMS.format(start))
                            .append("～")
                            .append(start == finish ? "\t\t" : durationHMS.format(finish))
                            .append("\t 損益")
                            .append(profitOrLoss.asJPY(4))
                            .append("\t")
                            .append(entry.exitSize())
                            .append("/")
                            .append(entry.entrySize())
                            .append("@")
                            .append(entry.side().mark())
                            .append(entry.order.average_price.asJPY(1))
                            .append(" → ")
                            .append(entry.exitPrice().asJPY(1))
                            .append("\t")
                            .append(entry.order.description() == null ? "" : entry.order.description())
                            .toString());

                    for (String log : entry.logs) {
                        System.out.println("   " + log);
                    }
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
                .append(String.format("総%d 済%d 残%d 中止%d)%n", total, complete, active, cancel));
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
    public static class AmountSummary {

        /** MAX value. */
        public Decimal min = Decimal.MAX;

        /** MIN value. */
        public Decimal max = Decimal.ZERO;

        /** Total value. */
        public Decimal total = Decimal.ZERO;

        /** Number of values. */
        public int size = 0;

        /** Number of positive values. */
        private int positive = 0;

        /**
         * Calculate mean.
         * 
         * @return
         */
        public Decimal mean() {
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
