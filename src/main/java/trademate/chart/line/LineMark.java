/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.line;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import hypatia.Num;
import hypatia.Primitives;
import javafx.collections.ObservableList;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import kiss.Variable;
import stylist.Style;
import trademate.chart.Axis;
import trademate.chart.Axis.TickLable;
import trademate.chart.ChartCanvas;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.StyleHelper;

public class LineMark extends Path {

    /** The associated canvas. */
    protected final ChartCanvas canvas;

    /** The styles. */
    private final Style[] styles;

    /** The model. */
    private final List<TickLable> labels;

    /** The associated axis. */
    private final Axis axis;

    /** The layout manager. */
    public final LayoutAssistant layoutLine;

    /**
     * Create line with label.
     * 
     * @param canvas
     * @param axis
     * @param styles
     */
    public LineMark(ChartCanvas canvas, Axis axis, Style... styles) {
        this(canvas, new CopyOnWriteArrayList(), axis, styles);
    }

    /**
     * Create line with label.
     * 
     * @param canvas
     * @param axis
     * @param styles
     */
    public LineMark(ChartCanvas canvas, List<TickLable> labels, Axis axis, Style... styles) {
        this.canvas = canvas;
        this.styles = styles;
        this.labels = labels;
        this.axis = axis;
        this.layoutLine = canvas.layoutCandle.sub();

        StyleHelper.of(this).style(styles);
    }

    /**
     * Create new mark.
     * 
     * @return
     */
    public final TickLable createLabel() {
        return createLabel(null, null);
    }

    /**
     * Create new mark.
     * 
     * @return
     */
    public final TickLable createLabel(String description) {
        return createLabel(null, description);
    }

    /**
     * Create new mark.
     * 
     * @return
     */
    public final TickLable createLabel(Num price) {
        return createLabel(price, null);
    }

    /**
     * Create new mark.
     * 
     * @return
     */
    public final TickLable createLabel(Num price, String description) {
        TickLable label = axis.createLabel(description, styles);
        if (price != null) label.value.set(price.doubleValue());
        labels.add(label);

        layoutLine.requestLayout();

        return label;
    }

    /**
     * Remove the registered label.
     * 
     * @param label
     */
    public final void removeLabel(TickLable label) {
        if (label != null && labels.contains(label)) {
            label.dispose();
            labels.remove(label);
            layoutLine.requestLayout();
        }
    }

    /**
     * Remove all registered labels.
     */
    public final void removeAllLabels() {
        labels.forEach(TickLable::dispose);
        labels.clear();
        layoutLine.requestLayout();
    }

    /**
     * Draw mark.
     */
    public void draw() {
        layoutLine.layout(() -> {
            ObservableList<PathElement> paths = getElements();
            int pathSize = paths.size();
            int labelSize = labels.size();

            if (pathSize > labelSize * 2) {
                paths.remove(labelSize * 2, pathSize);
                pathSize = labelSize * 2;
            }

            for (int i = 0; i < labelSize; i++) {
                MoveTo move;
                LineTo line;

                if (i * 2 < pathSize) {
                    move = (MoveTo) paths.get(i * 2);
                    line = (LineTo) paths.get(i * 2 + 1);
                } else {
                    move = new MoveTo();
                    line = new LineTo();
                    paths.addAll(move, line);
                }

                double value = labels.get(i).position();

                if (axis.isHorizontal()) {
                    if (Primitives.within(0, value, canvas.getWidth())) {
                        move.setX(value);
                        move.setY(0);
                        line.setX(value);
                        line.setY(canvas.getHeight());
                    } else {
                        move.setX(0);
                        move.setY(0);
                        line.setX(0);
                        line.setY(0);
                    }
                } else {
                    if (Primitives.within(0, value, canvas.getHeight())) {
                        move.setX(0);
                        move.setY(value);
                        line.setX(canvas.getWidth());
                        line.setY(value);
                    } else {
                        move.setX(0);
                        move.setY(0);
                        line.setX(0);
                        line.setY(0);
                    }
                }
            }
        });
    }

    /***
     * Find the label near the given position.
     * 
     * @param position
     * @return
     */
    protected final Variable<TickLable> findLabelByPosition(double position) {
        return findLabelByPosition(position, 5);
    }

    /***
     * Find the label near the given position.
     * 
     * @param position
     * @return
     */
    protected final Variable<TickLable> findLabelByPosition(double position, double threshold) {
        for (TickLable mark : labels) {
            double markedPosition = canvas.axisY.getPositionForValue(mark.value.get());

            if (Math.abs(markedPosition - position) < threshold) {
                return Variable.of(mark);
            }
        }
        return Variable.empty();
    }
}