/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.CleanRoom;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import kiss.Variable;

@Execution(ExecutionMode.SAME_THREAD)
class PlotScriptRegistryTest {

    @RegisterExtension
    static CleanRoom room = new CleanRoom();

    @Test
    void register() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        registry.register(BitFlyer.BTC_JPY, Volume.class);
        registry.register(BitFlyer.BTC_JPY, SMA.class);

        List<PlotScript> plotters = registry.findPlottersBy(BitFlyer.BTC_JPY, Span.Hour1);
        assert plotters.size() == 2;
        assert plotters.get(0) instanceof Volume;
        assert plotters.get(1) instanceof SMA;

    }

    @Test
    void unregister() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        registry.register(BitFlyer.BTC_JPY, Volume.class);
        registry.register(BitFlyer.BTC_JPY, SMA.class);

        assert registry.findPlottersBy(BitFlyer.BTC_JPY, Span.Hour1).size() == 2;
        registry.unregister(BitFlyer.BTC_JPY, Volume.class);
        assert registry.findPlottersBy(BitFlyer.BTC_JPY, Span.Hour1).size() == 1;
        registry.unregister(BitFlyer.BTC_JPY, SMA.class);
        assert registry.findPlottersBy(BitFlyer.BTC_JPY, Span.Hour1).size() == 0;
    }

    /**
     * 
     */
    private static class Volume extends PlotScript {

        public Variable<Integer> buy = Variable.of(0);

        public Variable<Integer> sell = Variable.of(0);

        /**
         * {@inheritDoc}
         */
        @Override
        protected void declare(Market market, Ticker ticker) {
        }
    }

    /**
     * 
     */
    private static class SMA extends PlotScript {

        public Variable<Integer> length = Variable.of(21);

        /**
         * {@inheritDoc}
         */
        @Override
        protected void declare(Market market, Ticker ticker) {
        }
    }

    /**
     * 
     */
    private static class TestablePlotScriptRegistry extends PlotScriptRegistry {

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return room.locate("pref.json").toAbsolutePath().toString();
        }
    }
}
