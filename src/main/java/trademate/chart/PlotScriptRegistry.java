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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cointoss.Market;
import cointoss.MarketService;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;
import kiss.model.Model;
import kiss.model.Property;
import trademate.chart.builtin.SMAIndicator;
import trademate.chart.builtin.VolumeIndicator;
import trademate.chart.builtin.WaveTrendIndicator;

@Manageable(lifestyle = Singleton.class)
class PlotScriptRegistry implements Storable {

    /** The managed scripts. */
    private Map<String, List<PlotScript>> managedScripts = new HashMap();

    /**
     * 
     */
    protected PlotScriptRegistry() {
        restore();

        I.signal(managedScripts.values()).flatIterable(v -> v).to(this::autoSave);
    }

    /**
     * Get the managedScripts property of this {@link PlotScriptRegistry}.
     * 
     * @return The managedScripts property.
     */
    @SuppressWarnings("unused")
    private Map<String, List<PlotScript>> getScripts() {
        return managedScripts;
    }

    /**
     * Set the managedScripts property of this {@link PlotScriptRegistry}.
     * 
     * @param managedScripts The managedScripts value to set.
     */
    @SuppressWarnings("unused")
    private void setScripts(Map<String, List<PlotScript>> managedScripts) {
        this.managedScripts = managedScripts;
    }

    /**
     * Retrieve all script on the specified {@link MarketService}.
     * 
     * @param market
     * @return
     */
    List<PlotScript> collectScriptOn(MarketService service) {
        return managedScripts.computeIfAbsent(service.marketName, k -> new ArrayList());
    }

    /**
     * Register {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param <S>
     * @param market A target market.
     * @param script A target script to add.
     * @return
     */
    <S extends PlotScript> S register(MarketService market, Class<S> type) {
        List<PlotScript> scripts = managedScripts.computeIfAbsent(market.marketName, k -> new ArrayList());
        for (PlotScript script : scripts) {
            if (script.getClass() == type) {
                return (S) script;
            }
        }

        S script = I.make(type);
        scripts.add(script);
        autoSave(script);
        return script;
    }

    /**
     * Observe properties to save automatically.
     * 
     * @param script
     */
    private void autoSave(PlotScript script) {
        Model<PlotScript> model = Model.of(script);
        for (Property p : model.properties()) {
            model.observe(script, p).to(v -> store());
        }
    }

    /**
     * Unregister {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param market A target market.
     * @param script A target script to remove.
     */
    void unregister(MarketService market, PlotScript script) {
        List<PlotScript> scripts = managedScripts.get(market.marketName);

        if (scripts != null) {
            scripts.remove(script);
        }
    }

    /**
     * Retrieve all script on the specified {@link Market}.
     * 
     * @param market
     * @return
     */
    public List<PlotScript> findScriptsOn(Market market) {
        return List.of(new WaveTrendIndicator(), new VolumeIndicator(), new SMAIndicator());
    }
}
