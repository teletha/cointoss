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

import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;

@Manageable(lifestyle = Singleton.class)
public class PlotScriptRegistry implements Storable {

    /** The managed scripts. */
    public List<PlotScript> scripts = new ArrayList();

    /**
     * 
     */
    protected PlotScriptRegistry() {
        restore();
    }
}
