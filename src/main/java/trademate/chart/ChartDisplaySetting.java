/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import kiss.Variable;
import viewtify.model.ModelHelper;

/**
 * @version 2018/09/24 7:07:55
 */
public class ChartDisplaySetting extends ModelHelper<ChartDisplaySetting> {

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
