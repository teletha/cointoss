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
import java.util.stream.Collectors;

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
    public Map<String, List<PlotScriptPreference>> preferences = new HashMap();

    /**
     * 
     */
    protected PlotScriptRegistry() {
        restore();
    }

    /**
     * Retrieve all script on the specified {@link MarketService}.
     * 
     * @param market
     * @return
     */
    List<PlotScript> collectScriptOn(MarketService service) {
        List<PlotScriptPreference> list = preferences.get(service.marketName);

        if (list == null) {
            return List.of();
        }
        return list.stream().map(this::decode).collect(Collectors.toList());
    }

    /**
     * Register {@link PlotScript} on the specified {@link MarketService}.
     * 
     * @param <P>
     * @param market
     * @param scriptClass
     * @return
     */
    <P extends PlotScript> P register(MarketService market, Class<P> scriptClass) {
        List<PlotScriptPreference> list = preferences.get(market.marketName);

        if (list == null) {
            list = new ArrayList();
            preferences.put(market.marketName, list);
        }

        for (PlotScriptPreference p : list) {
            if (p.clazz == scriptClass) {
                return (P) decode(p);
            }
        }

        PlotScriptPreference p = new PlotScriptPreference();
        p.clazz = scriptClass;
        list.add(p);

        return (P) decode(p);
    }

    private PlotScript decode(PlotScriptPreference pref) {
        if (pref.cache != null) {
            return pref.cache;
        }
        PlotScript script = I.make(pref.clazz);
        Model<PlotScript> model = Model.of(script);
        for (Property p : model.properties()) {
            model.set(script, p, I.transform(preferences.get(p.name), p.model.type));
            model.observe(script, p).to(v -> {
                pref.preferences.put(p.name, I.transform(v, String.class));
                store();
            });
        }
        return pref.cache = script;
    }

    void unregister(MarketService market, PlotScript script) {

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
