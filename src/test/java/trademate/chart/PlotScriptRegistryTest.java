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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Ticker;
import kiss.Variable;

class PlotScriptRegistryTest {

    @RegisterExtension
    static CleanRoom room = new CleanRoom();

    @Test
    void register() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        Volume volume = registry.register(BitFlyer.BTC_JPY, Volume.class);
        assert volume != null;

        // same instance on same market
        assert volume == registry.register(BitFlyer.BTC_JPY, Volume.class);

        // diff instance on diff market
        assert volume != registry.register(BitFlyer.ETH_BTC, Volume.class);
    }

    @Test
    void store() {
        PlotScriptRegistry registry = new TestablePlotScriptRegistry();
        Volume volume = registry.register(BitFlyer.BTC_JPY, Volume.class);
        volume.buy.set(10);

        PlotScriptRegistry other = new TestablePlotScriptRegistry();
        other.restore();
        Volume otherVolume = other.register(BitFlyer.BTC_JPY, Volume.class);
        assert otherVolume.buy.is(10);
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
    private static class TestablePlotScriptRegistry extends PlotScriptRegistry {

        // /**
        // * {@inheritDoc}
        // */
        // @Override
        // public String locate() {
        // return room.locateAbsent("pref.json").toAbsolutePath().toString();
        // }
    }
}
