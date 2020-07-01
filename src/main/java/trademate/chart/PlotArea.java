/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

public enum PlotArea {
    Top(true, 0, 35, 15), TopNarrow(true, 0, 25, 10), High(true, 70, 35, 15), HighNarrow(true, 70, 25, 10), Low(false, 70, 35,
            10), LowNarrow(false, 70, 25, 10), Bottom(false, 0, 35, 15), BottomNarrow(false, 0, 25, 10), Main(true, 0, -1, -1);

    /** The positioning. */
    final boolean direction;

    /** The offset. */
    final int offset;

    /** The area height. */
    final int maxHeight;

    /** The area height. */
    final int minHeight;

    /**
     * Initializer.
     */
    private PlotArea(boolean direction, int offset, int maxHeight, int minHeight) {
        this.direction = direction;
        this.offset = offset;
        this.maxHeight = maxHeight;
        this.minHeight = minHeight;
    }
}