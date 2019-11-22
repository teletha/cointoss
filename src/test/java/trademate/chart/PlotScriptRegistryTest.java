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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.CleanRoom;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Ticker;
import kiss.Variable;

@Execution(ExecutionMode.SAME_THREAD)
class PlotScriptRegistryTest {

    @RegisterExtension
    static CleanRoom room = new CleanRoom();

    @Test
    void findAllScriptsOnMarket() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        Volume volume = registry.register(BitFlyer.BTC_JPY, Volume.class);
        SMA sma = registry.register(BitFlyer.BTC_JPY, SMA.class);

        List<PlotScript> scripts = registry.findScriptsOn(BitFlyer.BTC_JPY);
        assert scripts.size() == 2;
        assert scripts.get(0) == volume;
        assert scripts.get(1) == sma;
    }

    @Test
    void unregisterScript() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        Volume volume = registry.register(BitFlyer.BTC_JPY, Volume.class);
        SMA sma = registry.register(BitFlyer.BTC_JPY, SMA.class);

        assert registry.findScriptsOn(BitFlyer.BTC_JPY).size() == 2;
        registry.unregister(BitFlyer.BTC_JPY, volume);
        assert registry.findScriptsOn(BitFlyer.BTC_JPY).size() == 1;
        registry.unregister(BitFlyer.BTC_JPY, sma);
        assert registry.findScriptsOn(BitFlyer.BTC_JPY).size() == 0;
    }

    @Test
    void plotScriptIsSingletonPerMarket() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        Volume volume = registry.register(BitFlyer.BTC_JPY, Volume.class);
        assert volume != null;

        // same instance on same market
        assert volume == registry.register(BitFlyer.BTC_JPY, Volume.class);

        // diff instance on diff market
        assert volume != registry.register(BitFlyer.ETH_BTC, Volume.class);
    }

    @Test
    void storePropertyAutomatically() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        Volume volume = registry.register(BitFlyer.BTC_JPY, Volume.class);
        volume.buy.set(12);

        PlotScriptRegistry other = new TestablePlotScriptRegistry();
        Volume otherVolume = other.register(BitFlyer.BTC_JPY, Volume.class);
        assert otherVolume.buy.is(12);
        assert otherVolume.sell.is(0);
        otherVolume.sell.set(15);

        PlotScriptRegistry another = new TestablePlotScriptRegistry();
        Volume anotherVolume = another.register(BitFlyer.BTC_JPY, Volume.class);
        assert anotherVolume.buy.is(12);
        assert anotherVolume.sell.is(15);
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
        protected List<PlotScript> defaults(String market) {
            return new ArrayList();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return room.locate("pref.json").toAbsolutePath().toString();
        }
    }
}
