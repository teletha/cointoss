/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.orderbook;

import java.util.List;

import cointoss.Direction;
import hypatia.Num;
import kiss.JSON;

public abstract class OrderBookChanges {

    /** The update mode. (diff or full-update) */
    private boolean diff = true;

    /**
     * Get the diff property of this {@link OrderBookChanges}.
     * 
     * @return The diff property.
     */
    public final boolean isDiff() {
        return diff;
    }

    /**
     * Get the diff property of this {@link OrderBookChanges}.
     * 
     * @return The diff property.
     */
    public final boolean isFull() {
        return !diff;
    }

    /**
     * Set the diff property of this {@link OrderBookChanges}.
     */
    public final OrderBookChanges diff() {
        this.diff = true;
        return this;
    }

    /**
     * Set the diff property of this {@link OrderBookChanges}.
     */
    public final OrderBookChanges full() {
        this.diff = false;
        return this;
    }

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
    public abstract void each(Direction side, Listener process);

    /**
     * Add the change of orderbook.
     * 
     * @param bid
     * @param price
     * @param size
     */
    public abstract void add(boolean bid, double price, float size);

    /**
     * Helper method to create {@link OrderBookChanges} by size.
     * 
     * @param size
     * @return
     */
    public static OrderBookChanges byHint(int size) {
        return size == 1 ? new Single() : new Multi(size);
    }

    /**
     * Helper method to build the optimized {@link OrderBookChanges} from simple JSON data set.
     * 
     * @param bids A bid data.
     * @param asks An ask data.
     * @param priceKey The price key on json.
     * @param sizeKey The size key on json.
     * @return
     */
    public static OrderBookChanges byJSON(List<JSON> bids, List<JSON> asks, String priceKey, String sizeKey) {
        return byJSON(bids, asks, priceKey, sizeKey, -1);
    }

    /**
     * Helper method to build the optimized {@link OrderBookChanges} from simple JSON data set.
     * 
     * @param bids A bid data.
     * @param asks An ask data.
     * @param priceKey The price key on json.
     * @param sizeKey The size key on json.
     * @return
     */
    public static OrderBookChanges byJSON(List<JSON> bids, List<JSON> asks, String priceKey, String sizeKey, int scale) {
        int bidSize = bids.size();
        int askSize = asks.size();
        int sum = bidSize + askSize;
        OrderBookChanges changes = sum == 1 ? new Single() : new Multi(sum);

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
     * Build the optimized {@link OrderBookChanges} for bid.
     * 
     * @param price A requested price.
     * @param size A requested size.
     * @return
     */
    public static OrderBookChanges singleBid(double price, float size) {
        Single change = new Single();
        change.add(true, price, size);
        return change;
    }

    /**
     * Build the optimized {@link OrderBookChanges} for ask.
     * 
     * @param price A requested price.
     * @param size A requested size.
     * @return
     */
    public static OrderBookChanges singleAsk(double price, float size) {
        Single change = new Single();
        change.add(false, price, size);
        return change;
    }

    /**
     * Special listener for the modification of orderbook pages.
     */
    public interface Listener {

        /**
         * Invoke when orderbook is changed.
         * 
         * @param price
         * @param size
         */
        void change(double price, float size);
    }

    /**
     * Optimized single orderbook change.
     */
    private static class Single extends OrderBookChanges {

        /** Bid or Ask */
        private boolean bid;

        /** Change of orderbook. */
        private double price;

        /** Change of orderbook. */
        private float size;

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestAsk() {
            return bid ? -1 : price;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestBid() {
            return bid ? price : -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void each(Direction side, Listener process) {
            if ((side == Direction.BUY) == bid) {
                process.change(price, size);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(boolean bid, double price, float size) {
            this.bid = bid;
            this.price = price;
            this.size = size;
        }
    }

    /**
     * Multiple change container.
     */
    private static class Multi extends OrderBookChanges {

        /** Size of bids. */
        private int bidSize;

        /** Size of asks. */
        private int askSize;

        /** Price container */
        private final double[] prices;

        /** Size container */
        private final float[] sizes;

        /**
         * Initialization.
         */
        private Multi(int size) {
            prices = new double[size];
            sizes = new float[size];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestAsk() {
            return askSize == 0 ? -1 : prices[prices.length - 1];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double bestBid() {
            return bidSize == 0 ? -1 : prices[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void each(Direction side, Listener process) {
            if (side == Direction.BUY) {
                for (int i = 0; i < bidSize; i++) {
                    process.change(prices[i], sizes[i]);
                }
            } else {
                for (int i = 0; i < askSize; i++) {
                    process.change(prices[bidSize + i], sizes[bidSize + i]);
                }
            }
        }

        @Override
        public void add(boolean bid, double price, float size) {
            if (bid) {
                prices[bidSize] = price;
                sizes[bidSize++] = size;
            } else {
                prices[prices.length - 1 - askSize] = price;
                sizes[sizes.length - 1 - askSize++] = size;
            }
        }
    }
}