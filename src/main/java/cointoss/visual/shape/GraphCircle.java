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

import javafx.scene.shape.Circle;

/**
 * @version 2017/09/26 1:04:55
 */
public class GraphCircle extends GraphPointShape {

    private Circle circle;

    /**
     * {@inheritDoc}
     */
    @Override
    public Circle getNode() {
        if (circle == null) {
            circle = new Circle(5);
        }
        return circle;
    }

    /**
     * @param r
     */
    public void setRadius(final double r) {
        getNode().setRadius(r);
    }

    /**
     * @return
     */
    public double getRadius() {
        return getNode().getRadius();
    }

}