/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.util.arithmetic.Num;
import kiss.JSON;

public abstract class OrderBookPageChanges {

    /**
     * Compute the best ask price.
     * 
     * @return
     */
    public abstract double bestAsk();

    /**
     * Compute the best bid price.
     * 
     * @return
     */
    public abstract double bestBid();

    /**
     * Iterate all {@link OrderBookPage} for the specified {@link Direction}.
     * 
     * @param side
     * @param process
     */
    public abstract void each(Direction side, Consumer<OrderBookPage> process);

    /**
     * Add the change of orderbook.
     * 
     * @param bid
     * @param price
     * @param size
     */
    public abstract void add(boolean bid, double price, float size);

    /**
     * Helper method to create {@link OrderBookPageChanges} by size.
     * 
     * @param size
     * @return
     */
    public static OrderBookPageChanges byHint(int size) {
        return size == 1 ? new Single() : new Multi(size);
    }

    /**
     * Helper method to build the optimized {@link OrderBookPageChanges} from simple JSON data set.
     * 
     * @param bids A bid data.
     * @param asks An ask data.
     * @param priceKey The price key on json.
     * @param sizeKey The size key on json.
     * @return
     */
    public static OrderBookPageChanges byJSON(List<JSON> bids, List<JSON> asks, String priceKey, String sizeKey) {
        return byJSON(bids, asks, priceKey, sizeKey, -1);
    }

    /**
     * Helper method to build the optimized {@link OrderBookPageChanges} from simple JSON data set.
     * 
     * @param bids A bid data.
     * @param asks An ask data.
     * @param priceKey The price key on json.
     * @param sizeKey The size key on json.
     * @return
     */
    public static OrderBookPageChanges byJSON(List<JSON> bids, List<JSON> asks, String priceKey, String sizeKey, int scale) {
        int bidSize = bids.size();
        int askSize = asks.size();
        int sum = bidSize + askSize;
        OrderBookPageChanges changes = sum == 1 ? new Single() : new Multi(sum);

        if (scale == -1) {
            for (int i = 0; i < bidSize; i++) {
                JSON e = bids.get(i);
                changes.add(true, Double.parseDouble(e.text(priceKey)), Float.parseFloat(e.text(sizeKey)));
            }
            for (int i = 0; i < askSize; i++) {
                JSON e = asks.get(i);
                changes.add(false, Double.parseDouble(e.text(priceKey)), Float.parseFloat(e.text(sizeKey)));
            }
        } else {
            for (int i = 0; i < bidSize; i++) {
                JSON e = bids.get(i);
                double price = Double.parseDouble(e.text(priceKey));
                Num size = e.get(Num.class, sizeKey).divide(price).scale(scale);
                changes.add(true, price, size.floatValue());
            }
            for (int i = 0; i < askSize; i++) {
                JSON e = asks.get(i);
                double price = Double.parseDouble(e.text(priceKey));
                Num size = e.get(Num.class, sizeKey).divide(price).scale(scale);
                changes.add(false, price, size.floatValue());
            }
        }

        return changes;
    }

    /**
     * Build the optimized {@link OrderBookPageChanges} for bid.
     * 
     * @param price A requested price.
     * @param size A requested size.
     * @return
     */
    public static OrderBookPageChanges singleBid(double price, float size) {
        Single change = new Single();
        change.add(true, price, size);
        return change;
    }

    /**
     * Build the optimized {@link OrderBookPageChanges} for ask.
     * 
     * @param price A requested price.
     * @param size A requested size.
     * @return
     */
    public static OrderBookPageChanges singleAsk(double price, float size) {
        Single change = new Single();
        change.add(false, price, size);
        return change;
    }

    /**
     * Optimized single orderbook change.
     */
    private static class Single extends OrderBookPageChanges {

        /** Bid or Ask */
        private boolean bid;

        /** Change of orderbook. */
        private OrderBookPage page;

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestAsk() {
            return bid ? -1 : page.price;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestBid() {
            return bid ? page.price : -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void each(Direction side, Consumer<OrderBookPage> process) {
            if ((side == Direction.BUY) == bid) {
                process.accept(page);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(boolean bid, double price, float size) {
            this.bid = bid;
            this.page = new OrderBookPage(price, size);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "OrderBookChange [" + page + "]";
        }
    }

    /**
     * Multiple change container.
     */
    private static class Multi extends OrderBookPageChanges {

        /** Size of bids. */
        private int bidSize;

        /** Size of asks. */
        private int askSize;

        /** Actual container. */
        private final OrderBookPage[] pages;

        /**
         * Initialization.
         */
        private Multi(int size) {
            pages = (OrderBookPage[]) Array.newInstance(OrderBookPage.class, size);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestAsk() {
            return askSize == 0 ? -1 : pages[pages.length - 1].price;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestBid() {
            return bidSize == 0 ? -1 : pages[0].price;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void each(Direction side, Consumer<OrderBookPage> process) {
            if (side == Direction.BUY) {
                for (int i = 0; i < bidSize; i++) {
                    process.accept(pages[i]);
                }
            } else {
                for (int i = 0; i < askSize; i++) {
                    process.accept(pages[bidSize + i]);
                }
            }
        }

        @Override
        public void add(boolean bid, double price, float size) {
            OrderBookPage page = new OrderBookPage(price, size);

            if (bid) {
                pages[bidSize++] = page;
            } else {
                pages[pages.length - 1 - askSize++] = page;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "OrderBookChange " + Arrays.toString(pages);
        }
    }
}