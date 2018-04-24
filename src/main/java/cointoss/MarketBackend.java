/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import cointoss.order.Order;
import cointoss.order.OrderBookListChange;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2018/02/28 16:28:45
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
     * Get the server health (status).
     * 
     * @return
     */
    public abstract Signal<Health> health();

    /**
     * <p>
     * Request order actually.
     * </p>
     * 
     * @param order A order to request.
     * @return A requested order.
     */
    public abstract Signal<String> request(Order order);

    /**
     * <p>
     * Request order canceling.
     * </p>
     * 
     * @param order A order to cancel.
     */
    public abstract Signal<Order> cancel(Order order);

    /**
     * <p>
     * Request all orders.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> orders();

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
    public abstract Signal<OrderBookListChange> getOrderBook();

    /**
     * @version 2018/02/28 16:28:41
     */
    public static enum Health {
        Normal("🌑"), Busy("🌘"), VeryBusy("🌗"), SuperBusy("🌖"), NoOrder("🌕"), Stop("💀");

        /** The human-readable status. */
        public final String mark;

        /**
         * @param mark
         */
        private Health(String mark) {
            this.mark = mark;
        }
    }
}
