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

enum PlotArea {
    Top(true, 0, 35, 10), High(true, 70, 35, 15), Low(false, 70, 35, 10), LowNarrow(false, 70, 25, 9), Bottom(false, 0, 35, 10), Main(true,
            0, -1, -1);

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
