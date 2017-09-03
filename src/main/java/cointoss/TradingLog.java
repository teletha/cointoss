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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * @version 2017/08/27 18:19:06
 */
public class TradingLog {

    /** The duration format. */
    private static final DateTimeFormatter durationHM = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm");

    /** The duration format. */
    private static final DateTimeFormatter durationHMS = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm:ss");

    /** All trading log. */
    private final List<Order> orders = new ArrayList(2048);

    /** The market. */
    private final Market market;

    /** summary */
    public int active;

    /** summary */
    public int completed;

    /** summary */
    public int canceled;

    /** summary */
    public int expired;

    /** summary */
    public int rejected;

    /** summary */
    public LongSummary orderTime;

    /** summary */
    public LongSummary holdTime;

    /** summary */
    public AmountSummary profit;

    /**
     * @param market
     */
    TradingLog(Market market) {
        this.market = market;
    }

    /**
     * Analyze current trading.
     */
    public void analyze() {
        // initialize
        active = 0;
        completed = 0;
        canceled = 0;
        orderTime = new LongSummary();
        holdTime = new LongSummary();
        profit = new AmountSummary();

        for (Order entry : orders) {
            // exclude exit order
            if (entry.exits.isEmpty()) {
                if (entry.child_order_state == OrderState.CANCELED) {
                    canceled++;
                }
                continue;
            }

            // calculate order time
            LocalDateTime start = entry.child_order_date;
            LocalDateTime finish = entry.executions.get(entry.executions.size() - 1).exec_date;
            orderTime.add(Duration.between(start, finish).getSeconds());

            // calculate hold time and profit
            start = entry.executions.get(0).exec_date;
            finish = start;
            Amount totalExecutedSize = Amount.ZERO;
            Amount totalProfit = Amount.ZERO;
            Amount totalPrice = Amount.ZERO;
            int active = 0;

            for (Order exit : entry.exits) {
                switch (exit.child_order_state) {
                case ACTIVE:
                    active++;
                    totalExecutedSize = totalExecutedSize.plus(exit.executed_size);
                    totalProfit = totalProfit.plus(calculateTradeProfit(entry, exit));
                    totalPrice = totalPrice.plus(exit.average_price.multiply(exit.executed_size));
                    break;

                case COMPLETED:
                    finish = max(finish, exit.executions.get(exit.executions.size() - 1).exec_date);
                    totalExecutedSize = totalExecutedSize.plus(exit.executed_size);
                    totalProfit = totalProfit.plus(calculateTradeProfit(entry, exit));
                    totalPrice = totalPrice.plus(exit.average_price.multiply(exit.executed_size));
                    break;

                case CANCELED:
                    totalExecutedSize = totalExecutedSize.plus(exit.executed_size);
                    totalProfit = totalProfit.plus(calculateTradeProfit(entry, exit));
                    totalPrice = totalPrice.plus(exit.average_price.multiply(exit.executed_size));
                    break;

                case EXPIRED:
                    break;

                case REJECTED:
                    break;
                }
            }
            holdTime.add(Duration.between(start, finish).getSeconds());
            profit.add(totalProfit);
            if (0 < active) {
                this.active++;
            } else {
                this.completed++;
            }

            // show bad orders
            if (totalProfit.isNegative()) {
                System.out.println(new StringBuilder() //
                        .append("注文 ")
                        .append(durationHMS.format(start))
                        .append("～")
                        .append(start == finish ? "\t\t" : durationHMS.format(finish))
                        .append("\t 損益")
                        .append(totalProfit.asJPY(4))
                        .append("\t")
                        .append(totalExecutedSize)
                        .append("/")
                        .append(entry.executed_size)
                        .append("@")
                        .append(entry.side().mark())
                        .append(entry.average_price.asJPY(1))
                        .append(" → ")
                        .append(totalExecutedSize.isZero() ? "" : totalPrice.divide(totalExecutedSize).asJPY(1))
                        .append("\t")
                        .append(entry.description())
                        .toString());
            }
        }

        int order = active + completed + canceled + expired + rejected;

        StringBuilder builder = new StringBuilder();
        builder.append("発注 ").append(orderTime);
        builder.append("保持 ").append(holdTime);
        builder.append("取引 最小")
                .append(profit.min.asJPY(7))
                .append("\t最大")
                .append(profit.max.asJPY(7))
                .append("\t平均")
                .append(profit.mean().asJPY(7))
                .append("\t合計")
                .append(profit.total.asJPY(12))
                .append(" (勝率")
                .append((profit.positive * 100 / Math.max(profit.size, 1)))
                .append("% ")
                .append(String.format("総%d 済%d 残%d 中止%d 失効%d 棄却%d)%n", order, completed, active, canceled, expired, rejected));
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
    private LocalDateTime max(LocalDateTime one, LocalDateTime other) {
        return one.isBefore(other) ? other : one;
    }

    /**
     * Calculate profit and loss.
     * 
     * @return
     */
    private Amount calculateTradeProfit(Order entry, Order exit) {
        if (entry.isBuy()) {
            return exit.isBuy() ? Amount.ZERO : exit.average_price.minus(entry.average_price).multiply(exit.executed_size);
        } else {
            return exit.isBuy() ? entry.average_price.minus(exit.average_price).multiply(exit.executed_size) : Amount.ZERO;
        }
    }

    /**
     * Logging {@link Order}.
     * 
     * @param order
     */
    public Order log(Order order) {
        orders.add(order);

        return order;
    }

    /**
     * Format {@link Amount}.
     * 
     * @param base
     * @return
     */
    private String format(Execution e, Amount base, Amount target) {
        return durationHM.format(e.exec_date) + " " + base.asJPY() + "\t" + target.asBTC() + "(" + e.price
                .asJPY(1) + ")\t総計" + base.plus(target.multiply(e.price)).asJPY();
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
    private static class AmountSummary {

        /** MAX value. */
        private Amount min = Amount.MAX;

        /** MIN value. */
        private Amount max = Amount.ZERO;

        /** Total value. */
        private Amount total = Amount.ZERO;

        /** Number of values. */
        private int size = 0;

        /** Number of positive values. */
        private int positive = 0;

        /**
         * Calculate mean.
         * 
         * @return
         */
        private Amount mean() {
            return total.divide(Math.max(size, 1));
        }

        /**
         * Add new value to summarize.
         * 
         * @param value
         */
        private void add(Amount value) {
            min = Amount.min(min, value);
            max = Amount.max(max, value);
            total = total.plus(value);
            size++;
            if (value.isPositive()) positive++;
        }
    }
}
