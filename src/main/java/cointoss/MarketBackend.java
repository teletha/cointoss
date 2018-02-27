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

import java.util.List;

import cointoss.order.Order;
import cointoss.order.Order.State;
import cointoss.order.OrderBookChange;
import cointoss.util.Num;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/09/08 18:51:05
 */
public abstract class MarketBackend implements Disposable {

    /**
     * Backend name.
     * 
     * @return
     */
    public abstract String name();

    /**
     * <p>
     * Initialize and activate market backend.
     * </p>
     * 
     * @param market
     */
    public abstract void initialize(Market market, Signal<Execution> log);

    /**
     * <p>
     * Request order actually.
     * </p>
     * 
     * @param order
     * @return
     */
    public abstract Signal<String> request(Order order);

    /**
     * <p>
     * Request order canceling.
     * </p>
     * 
     * @param order
     */
    public abstract Signal<Order> cancel(Order order);

    /**
     * <p>
     * Request order by id.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> getOrderBy(String id);

    /**
     * Retrieve all active orders constantly.
     * 
     * @return
     */
    public abstract Signal<List<Order>> orders();

    /**
     * <p>
     * Check all orders.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> getOrders();

    /**
     * <p>
     * Check all orders.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> getOrdersBy(State state);

    /**
     * <p>
     * Check all positions.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Position> getPositions();

    /**
     * <p>
     * Check all executions.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Execution> getExecutions();

    /**
     * <p>
     * Get amount of the base currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Num> getBaseCurrency();

    /**
     * <p>
     * Get amount of the target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Num> getTargetCurrency();

    /**
     * <p>
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<OrderBookChange> getOrderBook();

    /**
     * Get the service status.
     * 
     * @return
     */
    public abstract Signal<Health> getHealth();

    /**
     * @version 2017/12/14 16:20:52
     */
    public enum Health {
        Normal("🌑"), Busy("🌘"), VeryBusy("🌗"), SuperBusy("🌖"), NoOrder("🌕"), Stop("💀");

        static {
            I.load(Codec.class, false);
        }

        /** The human-readable status. */
        public final String mark;

        /**
         * @param mark
         */
        private Health(String mark) {
            this.mark = mark;
        }

        /**
         * @version 2017/12/26 12:59:08
         */
        private static class Codec implements Decoder<MarketBackend.Health>, Encoder<MarketBackend.Health> {

            /**
             * {@inheritDoc}
             */
            @Override
            public String encode(MarketBackend.Health value) {
                return value.name();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public MarketBackend.Health decode(String value) {
                switch (value.toLowerCase().replaceAll("\\s", "")) {
                case "normal":
                    return MarketBackend.Health.Normal;

                case "busy":
                    return MarketBackend.Health.Busy;

                case "verybusy":
                    return MarketBackend.Health.VeryBusy;

                case "superbusy":
                    return MarketBackend.Health.SuperBusy;

                case "noorder":
                    return MarketBackend.Health.NoOrder;

                case "stop":
                    return MarketBackend.Health.Stop;
                }
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error();
            }
        }
    }
}
