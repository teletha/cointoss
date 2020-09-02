/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.locust;

import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.canvas.EnhancedCanvas;

/**
 * 
 */
public class LocustView extends View {

    private EnhancedCanvas canvas = new EnhancedCanvas();

    class view extends ViewDSL {
        {
            $(() -> canvas);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {

    }
}
