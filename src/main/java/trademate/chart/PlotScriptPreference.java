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

import java.util.HashMap;
import java.util.Map;

class PlotScriptPreference {

    /** The script name. */
    public Class<? extends PlotScript> clazz;

    /** The script settings. */
    public Map<String, String> preferences = new HashMap();

    /** The cached associated script. */
    PlotScript cache;

}
