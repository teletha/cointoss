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

import cointoss.Market;
import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;
import trademate.chart.builtin.VolumeIndicator;
import trademate.chart.builtin.WaveTrendIndicator;

@Manageable(lifestyle = Singleton.class)
public class PlotScriptRegistry implements Storable {

    /** The managed scripts. */
    private List<PlotScript> scripts = new ArrayList();

    /**
     * 
     */
    protected PlotScriptRegistry() {
        restore();
    }

    /**
     * Get the scripts property of this {@link PlotScriptRegistry}.
     * 
     * @return The scripts property.
     */
    List<PlotScript> getScripts() {
        return scripts;
    }

    /**
     * Set the scripts property of this {@link PlotScriptRegistry}.
     * 
     * @param scripts The scripts value to set.
     */
    void setScripts(List<PlotScript> scripts) {
        this.scripts = scripts;
    }

    /**
     * Retrieve all script on the specified {@link Market}.
     * 
     * @param market
     * @return
     */
    public List<PlotScript> findScriptsOn(Market market) {
        return List.of(new WaveTrendIndicator(), new VolumeIndicator());
    }
}
