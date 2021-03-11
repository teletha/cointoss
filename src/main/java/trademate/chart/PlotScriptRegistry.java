/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import kiss.Variable;
import trademate.chart.builtin.OpenInterestIndicator;
import trademate.chart.builtin.SMAIndicator;
import trademate.chart.builtin.VolumeIndicator;
import trademate.chart.builtin.WaveTrendIndicator;

@Managed(value = Singleton.class)
class PlotScriptRegistry implements Storable<PlotScriptRegistry> {

    /** The managed scripts. */
    private Map<String, List<Class<? extends PlotScript>>> managedScripts = new HashMap();

    /** The state holder for each script. (Global) */
    private Map<Class<? extends PlotScript>, GlobalSetting> global = new HashMap();

    /**
     * 
     */
    protected PlotScriptRegistry() {
        restore().auto();
    }

    /**
     * Get the managedScripts property of this {@link PlotScriptRegistry}.
     * 
     * @return The managedScripts property.
     */
    @SuppressWarnings("unused")
    private Map<String, List<Class<? extends PlotScript>>> getManagedScripts() {
        return managedScripts;
    }

    /**
     * Set the managedScripts property of this {@link PlotScriptRegistry}.
     * 
     * @param managedScripts The managedScripts value to set.
     */
    @SuppressWarnings("unused")
    private void setManagedScripts(Map<String, List<Class<? extends PlotScript>>> managedScripts) {
        this.managedScripts = managedScripts;
    }

    /**
     * Get the global property of this {@link PlotScriptRegistry}.
     * 
     * @return The global property.
     */
    @SuppressWarnings("unused")
    private Map<Class<? extends PlotScript>, GlobalSetting> getGlobal() {
        return global;
    }

    /**
     * Set the global property of this {@link PlotScriptRegistry}.
     * 
     * @param global The global value to set.
     */
    @SuppressWarnings("unused")
    private void setGlobal(Map<Class<? extends PlotScript>, GlobalSetting> global) {
        this.global = global;
    }

    /**
     * Find all plotting scripts on the specified {@link Market} and {@link Ticker}.
     * 
     * @param market A target {@link Market}.
     * @param ticker A target {@link Ticker}.
     * @return A list of {@link PlotScript}.
     */
    final List<PlotScript> findPlottersBy(Market market, Ticker ticker) {
        return findPlottersBy(market.service, ticker.span);
    }

    /**
     * Find all plotting scripts on the specified {@link MarketService} and {@link Span}.
     * 
     * @param service A target {@link MarketService}.
     * @param span A target {@link Span}.
     * @return A list of {@link PlotScript}.
     */
    final List<PlotScript> findPlottersBy(MarketService service, Span span) {
        List<Class<? extends PlotScript>> classes = managedScripts.get(service.id);

        if (classes == null) {
            classes = defaults();
        }

        List<PlotScript> scripts = new ArrayList();
        for (Class<? extends PlotScript> clazz : classes) {
            scripts.add(I.make(clazz));
        }
        return scripts;
    }

    /**
     * Register {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param market A target market.
     * @param script A target script to add.
     * @return
     */
    final void register(Market market, Class<? extends PlotScript> type) {
        register(market.service, type);
    }

    /**
     * Register {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param service A target market.
     * @param script A target script to add.
     * @return
     */
    final void register(MarketService service, Class<? extends PlotScript> type) {
        List<Class<? extends PlotScript>> classes = managedScripts.computeIfAbsent(service.id, key -> new ArrayList());

        for (Class<? extends PlotScript> clazz : classes) {
            if (clazz == type) {
                return;
            }
        }
        classes.add(type);
    }

    /**
     * Unregister {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param market A target market.
     * @param script A target script to remove.
     */
    final void unregister(Market market, PlotScript script) {
        unregister(market, script.getClass());
    }

    /**
     * Unregister {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param market A target market.
     * @param script A target script to remove.
     */
    final void unregister(MarketService market, PlotScript script) {
        unregister(market, script.getClass());
    }

    /**
     * Unregister {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param market A target market.
     * @param script A target script to remove.
     */
    final void unregister(Market market, Class<? extends PlotScript> script) {
        unregister(market.service, script);
    }

    /**
     * Unregister {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param market A target market.
     * @param script A target script to remove.
     */
    final void unregister(MarketService market, Class<? extends PlotScript> script) {
        List<Class<? extends PlotScript>> classes = managedScripts.get(market.id);

        if (classes != null) {
            classes.remove(script);
        }
    }

    /**
     * Define default built-in indicators.
     * 
     * @return
     */
    protected List<Class<? extends PlotScript>> defaults() {
        return List.of(SMAIndicator.class, VolumeIndicator.class, WaveTrendIndicator.class, OpenInterestIndicator.class);
    }

    /**
     * Get {@link GlobalSetting} for each {@link PlotScript}.
     * 
     * @param script A target script.
     * @return A global setting.
     */
    final GlobalSetting globalSetting(PlotScript script) {
        return globalSetting(script.getClass());
    }

    /**
     * Get {@link GlobalSetting} for each {@link PlotScript}.
     * 
     * @param script A target script.
     * @return A global setting.
     */
    final GlobalSetting globalSetting(Class<? extends PlotScript> script) {
        GlobalSetting setting = global.get(script);

        if (setting == null) {
            global.put(script, setting = new GlobalSetting());
            store().auto();
        }
        return setting;
    }

    /**
     * Global script setting.
     */
    public static class GlobalSetting {

        /** The visibility. */
        public final Variable<Boolean> visible = Variable.of(true);

        /**
         * Toggle visibility.
         */
        public void toggleVisible() {
            visible.set(!visible.v);
        }
    }
}