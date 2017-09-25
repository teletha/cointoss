/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * @version 2017/09/26 1:05:28
 */
public abstract class GraphPointShape extends AbstractGraphShape {

    /**
     * @return
     */
    public ObservableList<String> getStyleClass() {
        return getNode().getStyleClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeProperty(final Axis xaxis, final Axis yaxis, final double w, final double h) {
        setValidate(true);
        final double x = xaxis.getDisplayPosition(getX());
        final double y = yaxis.getDisplayPosition(getY());
        final Node c = getNode();
        if (!isVisible() || x != x || Double.isInfinite(x) || Double.isInfinite(y) || y != y) {
            c.setVisible(false);
            return;
        }
        c.setLayoutX(x);
        c.setLayoutY(y);
        c.setVisible(true);
    }

    /**
     * グラフ上でのxの値
     * 
     * @return
     */
    public final DoubleProperty xProperty() {
        if (xProperty == null) {
            xProperty = new SimpleDoubleProperty(this, "x", 0);
            xProperty.addListener(getInvalidateListener());
        }
        return xProperty;
    }

    public final double getX() {
        return xProperty == null ? 0 : xProperty.get();
    }

    public final void setX(final double value) {
        xProperty().set(value);
    }

    private DoubleProperty xProperty;

    /**
     * グラフ上でのyの値
     * 
     * @return
     */
    public final DoubleProperty yProperty() {
        if (yProperty == null) {
            yProperty = new SimpleDoubleProperty(this, "y", 0);
            yProperty.addListener(getInvalidateListener());
        }
        return yProperty;
    }

    public final double getY() {
        return yProperty == null ? 0 : yProperty.get();
    }

    public final void setY(final double value) {
        yProperty().set(value);
    }

    private DoubleProperty yProperty;

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

}
