/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.CleanRoom;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import kiss.Disposable;
import kiss.Variable;
import trademate.chart.PlotScriptRegistry.GlobalSetting;

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

    @Test
    void visibility() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        GlobalSetting setting = registry.globalSetting(Volume.class);
        assert setting.visible.is(true);

        setting.visible.set(false);
        assert setting.visible.is(false);

        setting.visible.set(true);
        assert setting.visible.is(true);

        setting.toggleVisible();
        assert setting.visible.is(false);

        // save automatically
        PlotScriptRegistry otherRegistry = new TestablePlotScriptRegistry();
        GlobalSetting otherSetting = otherRegistry.globalSetting(Volume.class);
        assert otherSetting.visible.is(false);
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
        public Disposable auto() {
            return auto(Function.identity());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Path locate() {
            return room.locate("pref.json").toAbsolutePath();
        }
    }
}