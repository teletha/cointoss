/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.shape;

import javafx.scene.shape.Rectangle;

/**
 * @version 2017/09/26 1:04:55
 */
public class GraphCandle extends GraphPointShape {

    private Rectangle circle;

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle getNode() {
        if (circle == null) {
            circle = new Rectangle(5, 30);
        }
        return circle;
    }
}