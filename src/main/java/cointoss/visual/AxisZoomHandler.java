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

import static java.lang.Math.*;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;

/**
 * @version 2017/09/26 13:01:21
 */
public class AxisZoomHandler implements EventHandler<ScrollEvent> {

    /**
     * 無効かどうか
     * 
     * @return
     */
    public BooleanProperty disableProperty() {
        if (disableProperty == null) {
            disableProperty = new SimpleBooleanProperty(this, "disable", false);
        }
        return disableProperty;
    }

    public boolean isDisable() {
        return disableProperty == null ? false : disableProperty.get();
    }

    public void setDisable(final boolean value) {
        disableProperty().set(value);
    }

    private BooleanProperty disableProperty;

    /**
     * スクロールのスピード。デフォルトは0.01
     * 
     * @return
     */
    public DoubleProperty scrollSpeedProperty() {
        if (scrollSpeedProperty == null) {
            scrollSpeedProperty = new SimpleDoubleProperty(this, "scrollSpeed", 0.01);
        }
        return scrollSpeedProperty;
    }

    public double getScrollSpeed() {
        return scrollSpeedProperty == null ? -0.01 : scrollSpeedProperty.get();
    }

    public void setScrollSpeed(final double value) {
        scrollSpeedProperty().set(value);
    }

    private DoubleProperty scrollSpeedProperty;

    /**
     * 拡大率の最大値。デフォルトは20
     * 
     * @return
     */
    public DoubleProperty maxZoomProperty() {
        if (maxZoomProperty == null) {
            maxZoomProperty = new SimpleDoubleProperty(this, "maxZoom", 20) {
                @Override
                public void set(final double newValue) {
                    if (newValue < 1) {
                        return;
                    }
                    super.set(newValue);
                }
            };
        }
        return maxZoomProperty;
    }

    public double getMaxZoom() {
        return maxZoomProperty == null ? 20 : maxZoomProperty.get();
    }

    public void setMaxZoom(final double value) {
        maxZoomProperty().set(value);
    }

    private DoubleProperty maxZoomProperty;

    private Axis target;

    /**
     * どこからイベントが発生しても、捜査対象を引数のAxisにする。
     * 
     * @param a
     */
    public void setTargetAxis(final Axis a) {
        target = a;
    }

    public Axis getTargetAxis() {
        return target;
    }

    private static Axis getEventTargetAxis(final Event event) {
        final Object o = event.getSource();

        if (!(o instanceof Axis)) {
            return null;
        }
        return (Axis) o;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(ScrollEvent event) {
        if (isDisable()) {
            return;
        }
        Axis a = getTargetAxis();

        if (a == null) {
            a = getEventTargetAxis(event);
            if (a == null) {
                return;
            }
        }

        final double d = event.getDeltaY() * getScrollSpeed();
        if (d == 0d) {
            event.consume();
            return;
        }
        final double amount = a.getVisibleAmount();
        final double inva = min(1 / amount + d, getMaxZoom());
        final double newamount = max(min(1 / inva, 1), 0);
        a.visibleAmount(newamount);
        if (amount < newamount) {
            a.adjustLowerValue();
        }
        event.consume();
    }

    /**
     * GraphPlotAreaにインストールします
     * 
     * @param g
     */
    public final void install(final LineChart g) {
        install(g.graph);
    }

    public void install(final Node n) {
        n.addEventHandler(ScrollEvent.SCROLL, this);
    }

    public void uninstall(final Node n) {
        n.removeEventHandler(ScrollEvent.SCROLL, this);
    }
}
