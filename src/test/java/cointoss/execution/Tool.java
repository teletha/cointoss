/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.ZonedDateTime;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;
import kiss.WiseBiConsumer;
import kiss.WiseConsumer;

public class Tool {

    static {
        I.load(Market.class);
    }

    /**
     * Create new task.
     * 
     * @return
     */
    public static <X> Where defineTask() {
        return new Holder();
    }

    /**
     * Query market.
     */
    public interface Where {
        /**
         * Select markets.
         * 
         * @param service
         * @return
         */
        When on(Signal<MarketService> service);

        /**
         * Specify market directly.
         * 
         * @param service
         * @return
         */
        default When on(MarketService... service) {
            return on(I.signal(service));
        }

        /**
         * All defined markets.
         * 
         * @return
         */
        default When onAllServices() {
            return on(MarketServiceProvider.availableMarketServices());
        }

        /**
         * All markets on the specified {@link Exchange}.
         * 
         * @param exchange
         * @return
         */
        default When onAllService(Exchange exchange) {
            return on(MarketServiceProvider.availableMarketServices().take(x -> x.exchange == exchange));
        }
    }

    /**
     * Query date.
     */
    public interface When {
        /**
         * Select date.
         * 
         * @param date
         */
        ToolTask at(Signal<ZonedDateTime> date);

        /**
         * Select all date.
         * 
         */
        default ToolTask allDays() {
            return at((ZonedDateTime) null);
        }

        /**
         * Select date by year.
         * 
         * @param year
         */
        default ToolTask at(int year) {
            return at(Chrono.range(year));
        }

        /**
         * Select date by year and month.
         * 
         * @param year
         */
        default ToolTask at(int year, int month) {
            return at(Chrono.range(year, month));
        }

        /**
         * Select date by year, month and day.
         * 
         * @param year
         */
        default ToolTask at(int year, int month, int day) {
            return at(Chrono.utc(year, month, day));
        }

        /**
         * Select date by year, month and day.
         * 
         * @param date
         */
        default ToolTask at(ZonedDateTime... date) {
            return at(I.signal(date));
        }

        /**
         * Execute task.
         * 
         * @param task
         */
        void run(WiseConsumer<ExecutionLog> task);
    }

    public interface ToolTask {

        /**
         * Execute task.
         * 
         * @param task
         */
        void run(WiseBiConsumer<ExecutionLog, ZonedDateTime> task);
    }

    /**
     * Query date holder.
     */
    private static class Holder implements Where, When, ToolTask {

        private Signal<MarketService> where = I.signal();

        private Signal<ZonedDateTime> when = I.signal();

        /**
         * {@inheritDoc}
         */
        @Override
        public When on(Signal<MarketService> where) {
            this.where = where;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ToolTask at(Signal<ZonedDateTime> when) {
            this.when = when;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run(WiseConsumer<ExecutionLog> task) {
            where.map(ExecutionLog::new).to(task);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run(WiseBiConsumer<ExecutionLog, ZonedDateTime> task) {
            where.map(ExecutionLog::new).to(log -> {
                when.to(date -> {
                    task.accept(log, date);
                });
            });
        }
    }
}