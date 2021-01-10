/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.setting;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import kiss.Variable;

@Managed(Singleton.class)
public class StaticConfig implements Storable<StaticConfig> {

    private static final StaticConfig SINGLETON = I.make(StaticConfig.class);

    public final Variable<Long> drawingThrottle = Variable.of(500L);

    /**
     * Hide constructor.
     */
    private StaticConfig() {
        restore().auto();
    }

    /**
     * Throttle time for drawing UI. (ms)
     * 
     * @return
     */
    public static long drawingThrottle() {
        return SINGLETON.drawingThrottle.exact();
    }
}
