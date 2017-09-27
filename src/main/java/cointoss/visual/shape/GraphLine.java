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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.shape.Line;

import cointoss.visual.Axis;

/**
 * グラフ上に表示する線
 * 
 * @author nodamushi
 */
public class GraphLine extends AbstractGraphShape {

    private Line line;

    /**
     * {@inheritDoc}
     */
    @Override
    public Line getNode() {
        if (line == null) {
            line = new Line();
        }
        return line;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeProperty(Axis xaxis, Axis yaxis, double w, double h) {
        setValidate(true);
        double v = getValue();
        Line line = getNode();
        Orientation orientation = getOrientation();
        boolean isX = orientation == Orientation.VERTICAL;
        Axis axis = isX ? xaxis : yaxis;
        if (Double.isInfinite(v) || v != v || !isVisible()) {
            line.setVisible(false);
            return;
        }
        if (!isX) {
            // 横方向
            double y = axis.getPositionForValue(v);
            if (Double.isInfinite(y) || y != y || y < 0 || y > h) {
                line.setVisible(false);
                return;
            }
            line.setStartX(0);
            line.setEndX(w);
            line.setStartY(0);
            line.setEndY(0);
            line.setLayoutX(0);
            line.setLayoutY(y);
        } else {
            double x = axis.getPositionForValue(v);
            if (Double.isInfinite(x) || x != x || x < 0 || x > w) {
                line.setVisible(false);
                return;
            }
            line.setStartY(0);
            line.setEndY(h);
            line.setStartX(0);
            line.setEndX(0);
            line.setLayoutX(x);
            line.setLayoutY(0);
        }
        line.setVisible(true);

    }

    /**
     * グラフ上の値
     * 
     * @return
     */
    public final DoubleProperty valueProperty() {
        if (valueProperty == null) {
            valueProperty = new SimpleDoubleProperty(this, "value", 0);
            valueProperty.addListener(getInvalidateListener());
        }
        return valueProperty;
    }

    /**
     * Get Value.
     * 
     * @return
     */
    public final double getValue() {
        return valueProperty == null ? 0 : valueProperty.get();
    }

    /**
     * Set value.
     * 
     * @param value
     */
    public final void setValue(final double value) {
        valueProperty().set(value);
    }

    private DoubleProperty valueProperty;

    /**
     * 可視性
     * 
     * @return
     */
    public final BooleanProperty visibleProperty() {
        if (visibleProperty == null) {
            visibleProperty = new SimpleBooleanProperty(this, "visible", true);
            visibleProperty.addListener(getInvalidateListener());
        }
        return visibleProperty;
    }

    public final boolean isVisible() {
        return visibleProperty == null ? true : visibleProperty.get();
    }

    public final void setVisible(final boolean value) {
        visibleProperty().set(value);
    }

    private BooleanProperty visibleProperty;

    /**
     * 縦方向の線か、横方向の線か
     * 
     * @return
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientationProperty == null) {
            orientationProperty = new SimpleObjectProperty<>(this, "orientation", null);
            orientationProperty.addListener(getInvalidateListener());
        }
        return orientationProperty;
    }

    public final Orientation getOrientation() {
        return orientationProperty == null ? null : orientationProperty.get();
    }

    public final void setOrientation(final Orientation value) {
        orientationProperty().set(value);
    }

    private ObjectProperty<Orientation> orientationProperty;

}
