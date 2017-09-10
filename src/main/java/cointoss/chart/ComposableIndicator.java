/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

/**
 * @version 2017/09/10 14:15:09
 */
public abstract class ComposableIndicator extends Indicator {

    protected final Indicator indicator;

    /**
     * @param chart
     * @param indicator
     */
    protected ComposableIndicator(Indicator indicator) {
        super(indicator.chart);

        this.indicator = indicator;
    }
}
