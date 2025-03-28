/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.part;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;

import kiss.Signal;
import trademate.chart.ChartCanvas;
import trademate.setting.PerformanceSetting;
import viewtify.Viewtify;
import viewtify.preference.Preferences;
import viewtify.ui.canvas.EnhancedCanvas;
import viewtify.ui.helper.LayoutAssistant;

public abstract class ChartPart {

    /** The parent chart. */
    public final ChartCanvas parent;

    /** The layout manage. */
    public final LayoutAssistant layout;

    /** The actual canvas. */
    public final EnhancedCanvas canvas;

    /** The all managed canvases. */
    public final List<EnhancedCanvas> managed = new ArrayList();

    /**
     * Set up part of the chart.
     * 
     * @param parent
     */
    protected ChartPart(ChartCanvas parent) {
        this.parent = parent;
        this.layout = new LayoutAssistant(parent);
        this.canvas = createCanvas();
    }

    /**
     * Create new canvas.
     * 
     * @return
     */
    protected final EnhancedCanvas createCanvas() {
        EnhancedCanvas canvas = new EnhancedCanvas().visibleWhen(layout.canLayout);

        // manage the created new canvas automatically
        managed.add(canvas);

        parent.chart.showRealtimeUpdate.observing().to(show -> {
            if (show) {
                configurePreferedCanvasSize(canvas);
            } else {
                // clear all contents and minimize them to reduce memory usage
                canvas.clear().size(0, 0);
            }
        });

        return canvas;
    }

    /**
     * Configure your prefered canvas size.
     * 
     * @param canvas
     */
    protected void configurePreferedCanvasSize(EnhancedCanvas canvas) {
        canvas.bindSizeTo(parent);
    }

    public abstract void draw();

    protected final Observable[] chartAxisModification() {
        return new DoubleProperty[] {parent.axisX.scroll.valueProperty(), parent.axisX.scroll.visibleAmountProperty()};
    }

    protected final Signal userInterfaceModification() {
        PerformanceSetting performance = Preferences.of(PerformanceSetting.class);

        return Viewtify.observe(parent.widthProperty())
                .merge(Viewtify.observe(parent.heightProperty()))
                .debounce(performance.refreshRate, TimeUnit.MILLISECONDS, false);
    }
}