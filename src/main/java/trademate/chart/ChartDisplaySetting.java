/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;
import kiss.Variable;

/**
 * @version 2018/09/24 7:07:55
 */
@Manageable(lifestyle = Singleton.class)
public class ChartDisplaySetting implements Storable<ChartDisplaySetting> {

    /** The chart configuration. */
    public final Variable<Boolean> showLatestPrice = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showMouseTrack = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showOrderSupport = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showPositionSupport = Variable.of(true);

    /**
     * Hide constructor.
     */
    private ChartDisplaySetting() {
        restore().auto();
    }
}
