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

import java.util.BitSet;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;

import org.eclipse.collections.api.list.primitive.BooleanList;
import org.eclipse.collections.api.list.primitive.DoubleList;

import cointoss.chart.Tick;
import cointoss.util.Num;
import cointoss.visual.shape.Candle;
import cointoss.visual.shape.GraphShape;

/**
 * @version 2017/09/27 21:49:33
 */
public class GraphPlotArea extends Region {

    /** The visibility of horizontal grid line. */
    public final BooleanProperty horizontalGridLineVisibility = new SimpleBooleanProperty(this, "horizontalGridLinesVisible", true);

    /** The visibility of vertical grid line. */
    public final BooleanProperty verticalGridLineVisibility = new SimpleBooleanProperty(this, "verticalGridLineVisibility", true);

    /** The horizontal margin of plot area. */
    public final DoubleProperty horizontalMargin = new SimpleDoubleProperty(this, "horizontalGridLinesVisible", 1);

    /** The vertical margin of plot area. */
    public final DoubleProperty verticalMargin = new SimpleDoubleProperty(this, "verticalGridLineVisibility", 1);

    /** The horizontal axis. */
    final ObjectProperty<Axis> axisX = new SimpleObjectProperty<>(this, "axisX", null);

    /** The vertical axis. */
    final ObjectProperty<Axis> axisY = new SimpleObjectProperty<>(this, "axisY", null);

    /** The validity flag of plotting. */
    private boolean plotValidate = false;

    /** The validator. */
    private final InvalidationListener plotValidateListener = observable -> {
        if (plotValidate) {
            plotValidate = false;
            setGraphShapeValidate(false);
            setNeedsLayout(true);
        }
    };

    /** The validator. */
    private final ChangeListener<Axis> axisListener = (observable, oldValue, newValue) -> {
        if (oldValue != null) {
            oldValue.visualMinValue.removeListener(plotValidateListener);
            oldValue.visibleRange.removeListener(plotValidateListener);
        }

        if (newValue != null) {
            newValue.visualMinValue.addListener(plotValidateListener);
            newValue.visibleRange.addListener(plotValidateListener);
        }
        if (plotValidate) {
            plotValidate = false;
            requestLayout();
        }
    };

    private final Rectangle clip = new Rectangle();

    private final Group background = new LocalGroup();

    /** The line chart manager */
    private final Group lines = new LocalGroup();

    /** The candle chart manager */
    private final Group candles = new LocalGroup();

    private final Group foreground = new LocalGroup();

    private final Group userBackround = new LocalGroup();

    private final Group userForeground = new LocalGroup();

    private final Path verticalGridLines = new Path();

    private final Path horizontalGridLines = new Path();

    private final Path verticalMinorGridLines = new Path();

    private final Path horizontalMinorGridLines = new Path();

    private final Path verticalRowFill = new Path();

    private final Path horizontalRowFill = new Path();

    /** The line chart color manager. */
    private final BitSet lineColorManager = new BitSet(8);

    /** The line chart data list. */
    private ObservableList<Tick> candleChartData;

    /** The line chart data list. */
    private ObservableList<CandleChartData> lineChartData;

    /** The line chart data observer. */
    private final InvalidationListener lineDataObserver = o -> {
        ReadOnlyBooleanProperty b = (ReadOnlyBooleanProperty) o;
        if (!b.get() && plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    };

    /** The line chart data list observer. */
    private final ListChangeListener<CandleChartData> lineDataListObserver = change -> {
        change.next();
        for (CandleChartData d : change.getRemoved()) {
            lineColorManager.clear(d.defaultColorIndex);
            d.validateProperty().removeListener(lineDataObserver);
        }
        for (CandleChartData d : change.getAddedSubList()) {
            d.defaultColorIndex = lineColorManager.nextClearBit(0);
            lineColorManager.set(d.defaultColorIndex, true);
            d.defaultColor = "default-color" + (d.defaultColorIndex % 8);
            d.validateProperty().addListener(lineDataObserver);
        }
    };

    /**
     * 
     */
    public GraphPlotArea() {
        getStyleClass().setAll("chart-plot-background");
        axisX.addListener(axisListener);
        widthProperty().addListener(plotValidateListener);
        heightProperty().addListener(plotValidateListener);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        verticalMinorGridLines.setVisible(false);
        horizontalMinorGridLines.setVisible(false);
        verticalRowFill.getStyleClass().setAll("chart-alternative-column-fill");
        horizontalRowFill.getStyleClass().setAll("chart-alternative-row-fill");
        verticalGridLines.getStyleClass().setAll("chart-vertical-grid-lines");
        horizontalGridLines.getStyleClass().setAll("chart-horizontal-grid-lines");
        verticalMinorGridLines.getStyleClass().setAll("chart-vertical-grid-lines", "chart-vertical-minor-grid-lines");
        horizontalMinorGridLines.getStyleClass().setAll("chart-horizontal-grid-lines", "chart-horizontal-minor-grid-lines");
        getChildren()
                .addAll(verticalRowFill, horizontalRowFill, verticalMinorGridLines, horizontalMinorGridLines, verticalGridLines, horizontalGridLines, background, userBackround, candles, lines, foreground, userForeground);
    }

    /**
     * ユーザが任意に使える背景領域
     * 
     * @return
     */
    public final ObservableList<Node> getBackgroundChildren() {
        return userBackround.getChildren();
    }

    /**
     * ユーザが任意に使える前景領域
     * 
     * @return
     */
    public final ObservableList<Node> getForegroundChildren() {
        return userForeground.getChildren();
    }

    @Override
    protected void layoutChildren() {
        if (!plotValidate) {
            plotData();
        }
        if (!isGraphShapeValidate()) {
            drawGraphShapes();
        }
    }

    /**
     * Draw plot data.
     */
    public void plotData() {
        final Axis xaxis = axisX.get();
        final Axis yaxis = axisY.get();

        if (xaxis == null || yaxis == null) {
            plotValidate = true;
            setGraphShapeValidate(true);
            return;
        }
        drawGraphShapes();

        if (!plotValidate) {
            drawBackGroundLine();
            plotLineChartDatas();
            plotCandleChartDatas();
            plotValidate = true;
        }
    }

    public void drawGraphShapes() {
        if (plotValidate && isGraphShapeValidate()) {
            return;
        }
        final Axis xaxis = axisX.get();
        final Axis yaxis = axisY.get();

        if (xaxis == null || yaxis == null) {
            setGraphShapeValidate(true);
            return;
        }

        final double w = getWidth(), h = getHeight();
        if (w != xaxis.getWidth() || h != yaxis.getHeight()) {
            return;
        }
        List<GraphShape> lines = backGroundShapes;
        if (lines != null) {

            for (final GraphShape gl : lines) {
                gl.setNodeProperty(xaxis, yaxis, w, h);
            }
        }
        lines = foreGroundShapes;
        if (lines != null) {
            for (final GraphShape gl : lines) {
                gl.setNodeProperty(xaxis, yaxis, w, h);
            }
        }
        setGraphShapeValidate(true);
    }

    public void drawBackGroundLine() {
        final Axis xaxis = axisX.get();
        final Axis yaxis = axisY.get();

        if (xaxis == null || yaxis == null) {
            plotValidate = true;
            return;
        }

        final double w = getWidth(), h = getHeight();
        // 背景の線を描画

        V: {
            final Axis axis = xaxis;
            DoubleList vTicks = axis.majors;
            BooleanList vFill = axis.majorsFill;
            final ObservableList<PathElement> lele = verticalGridLines.getElements();
            final ObservableList<PathElement> fele = verticalRowFill.getElements();
            int lelesize = lele.size();
            final int felesize = fele.size();
            final boolean fill = isAlternativeColumnFillVisible();
            final boolean line = verticalGridLineVisibility.get();
            verticalGridLines.setVisible(line);
            verticalRowFill.setVisible(fill);
            final int e = vTicks.size();
            if (!line) {
                lele.clear();
            } else if (lelesize > e * 2) {
                lele.remove(e * 2, lelesize);
                lelesize = e * 2;
            }
            if (!fill) {
                fele.clear();
            }
            int findex = 0;

            if (!line && !fill) {
                break V;
            }
            for (int i = 0; i < e; i++) {
                final double d = vTicks.get(i);
                if (line) {
                    MoveTo mt;
                    LineTo lt;
                    if (i * 2 < lelesize) {
                        mt = (MoveTo) lele.get(i * 2);
                        lt = (LineTo) lele.get(i * 2 + 1);
                    } else {
                        mt = new MoveTo();
                        lt = new LineTo();
                        lele.addAll(mt, lt);
                    }
                    mt.setX(d);
                    mt.setY(0);
                    lt.setX(d);
                    lt.setY(h);
                }
                if (fill) {
                    final boolean f = vFill.get(i);
                    MoveTo m;
                    LineTo l1, l2, l3;

                    if (f || i == 0) {
                        if (findex < felesize) {
                            m = (MoveTo) fele.get(findex);
                            l1 = (LineTo) fele.get(findex + 1);
                            l2 = (LineTo) fele.get(findex + 2);
                            l3 = (LineTo) fele.get(findex + 3);
                            findex += 5;
                        } else {
                            m = new MoveTo();
                            l1 = new LineTo();
                            l2 = new LineTo();
                            l3 = new LineTo();
                            fele.addAll(m, l1, l2, l3, new ClosePath());
                        }
                    } else {
                        continue;
                    }
                    double x0, x1;
                    if (!f) {
                        x0 = 0;
                        x1 = d;
                    } else if (i == e - 1) {
                        x0 = d;
                        x1 = w;
                    } else {
                        x0 = d;
                        x1 = vTicks.get(i + 1);
                    }
                    m.setX(x0);
                    m.setY(0);
                    l1.setX(x0);
                    l1.setY(h);
                    l2.setX(x1);
                    l2.setY(h);
                    l3.setX(x1);
                    l3.setY(0);
                } // end fill
            } // end for
            if (findex < felesize) {
                fele.remove(findex, felesize);
            }
        } // end V

        H: {
            final Axis axis = yaxis;
            DoubleList hTicks = axis.majors;
            BooleanList hFill = axis.majorsFill;
            final ObservableList<PathElement> lele = horizontalGridLines.getElements();
            final ObservableList<PathElement> fele = horizontalRowFill.getElements();
            int lelesize = lele.size();
            final int felesize = fele.size();
            final boolean fill = isAlternativeRowFillVisible();
            final boolean line = horizontalGridLineVisibility.get();
            horizontalGridLines.setVisible(line);
            horizontalRowFill.setVisible(fill);
            final int e = hTicks.size();
            if (!line) {
                lele.clear();
            } else if (lelesize > e * 2) {
                lele.remove(e * 2, lelesize);
                lelesize = e * 2;
            }
            if (!fill) {
                fele.clear();
            }
            int findex = 0;
            if (!line && !fill) {
                break H;
            }
            for (int i = 0; i < e; i++) {
                final double d = hTicks.get(i);
                if (line) {
                    MoveTo mt;
                    LineTo lt;
                    if (i * 2 < lelesize) {
                        mt = (MoveTo) lele.get(i * 2);
                        lt = (LineTo) lele.get(i * 2 + 1);
                    } else {
                        mt = new MoveTo();
                        lt = new LineTo();
                        lele.addAll(mt, lt);
                    }
                    mt.setX(0);
                    mt.setY(d);
                    lt.setX(w);
                    lt.setY(d);
                }
                if (fill) {
                    final boolean f = hFill.get(i);
                    MoveTo m;
                    LineTo l1, l2, l3;
                    if (f || i == 0) {
                        if (findex < felesize) {
                            m = (MoveTo) fele.get(findex);
                            l1 = (LineTo) fele.get(findex + 1);
                            l2 = (LineTo) fele.get(findex + 2);
                            l3 = (LineTo) fele.get(findex + 3);
                            findex += 5;
                        } else {
                            m = new MoveTo();
                            l1 = new LineTo();
                            l2 = new LineTo();
                            l3 = new LineTo();
                            fele.addAll(m, l1, l2, l3, new ClosePath());
                        }
                    } else {
                        continue;
                    }
                    double y0, y1;
                    if (!f) {
                        y0 = h;
                        y1 = d;
                    } else if (i == e - 1) {
                        y0 = d;
                        y1 = 0;
                    } else {
                        y0 = d;
                        y1 = hTicks.get(i + 1);
                    }
                    m.setX(0);
                    m.setY(y0);
                    l1.setX(w);
                    l1.setY(y0);
                    l2.setX(w);
                    l2.setY(y1);
                    l3.setX(0);
                    l3.setY(y1);
                } // end fill
            } // end for
            if (findex < felesize) {
                fele.remove(findex, felesize);
            }
        } // end H

        if (yaxis.minorTickVisibility.get()) {
            final Axis axis = xaxis;
            DoubleList minorTicks = axis.majors;

            final ObservableList<PathElement> ele = verticalMinorGridLines.getElements();
            final int elesize = ele.size();
            final int e = minorTicks.size();
            if (elesize > e * 2) {
                ele.remove(e * 2, elesize);
            }
            for (int i = 0; i < e; i++) {
                final double d = minorTicks.get(i);
                MoveTo mt;
                LineTo lt;
                if (i * 2 < elesize) {
                    mt = (MoveTo) ele.get(i * 2);
                    lt = (LineTo) ele.get(i * 2 + 1);
                } else {
                    mt = new MoveTo();
                    lt = new LineTo();
                    ele.addAll(mt, lt);
                }
                mt.setX(d);
                mt.setY(0);
                lt.setX(d);
                lt.setY(h);
            }
        }

        if (xaxis.minorTickVisibility.get()) {
            final Axis axis = yaxis;
            DoubleList minorTicks = axis.minors;

            final ObservableList<PathElement> ele = horizontalMinorGridLines.getElements();
            final int elesize = ele.size();
            final int e = minorTicks.size();
            if (elesize > e * 2) {
                ele.remove(e * 2, elesize);
            }
            for (int i = 0; i < e; i++) {
                final double d = minorTicks.get(i);
                MoveTo mt;
                LineTo lt;
                if (i * 2 < elesize) {
                    mt = (MoveTo) ele.get(i * 2);
                    lt = (LineTo) ele.get(i * 2 + 1);
                } else {
                    mt = new MoveTo();
                    lt = new LineTo();
                    ele.addAll(mt, lt);
                }
                mt.setX(0);
                mt.setY(d);
                lt.setX(w);
                lt.setY(d);
            }
        }
    }

    /**
     * Draw line chart.
     */
    protected void plotLineChartDatas() {
        ObservableList<Node> paths = lines.getChildren();
        List<CandleChartData> datas = lineChartData;

        if (datas == null) {
            paths.clear();
        } else {
            int sizeData = datas.size();
            int sizePath = paths.size();

            if (sizeData < sizePath) {
                paths.remove(sizeData, sizePath);
                sizePath = sizeData;
            }

            for (int i = 0; i < sizeData; i++) {
                int defaultColorIndex = 2;
                CandleChartData data = datas.get(i);

                Path path;

                if (i < sizePath) {
                    path = (Path) paths.get(i);
                } else {
                    path = new Path();
                    path.setStrokeLineJoin(StrokeLineJoin.BEVEL);
                    path.setStrokeWidth(0.6);
                    path.getStyleClass().setAll("chart-series-line", "series" + i, data.defaultColor);
                    paths.add(path);
                }

                ObservableList<String> className = path.getStyleClass();

                if (!className.get(defaultColorIndex).equals(data.defaultColor)) {
                    className.set(defaultColorIndex, data.defaultColor);
                }
                plotLineChartData(data, path);
            }
        }
    }

    /**
     * Draw line chart.
     * 
     * @param width
     * @param height
     */
    protected void plotCandleChartDatas() {
        ObservableList<Node> nodes = candles.getChildren();
        List<Tick> datas = candleChartData;

        if (datas == null) {
            nodes.clear();
        } else {
            int sizeData = datas.size();
            int sizePath = nodes.size();

            if (sizeData < sizePath) {
                nodes.remove(sizeData, sizePath);
                sizePath = sizeData;
            }

            Num min = Num.MAX;
            Axis xAxis = axisX.get();
            long start = (long) xAxis.visualMinValue.get();
            long end = (long) xAxis.visualMaxValue.get();

            for (int i = 0; i < sizeData; i++) {
                Tick data = datas.get(i);
                long time = data.start.toInstant().toEpochMilli();

                if (start <= time && time <= end) {
                    min = Num.min(min, data.minPrice);
                }

                Candle candle;

                if (i < sizePath) {
                    candle = (Candle) nodes.get(i);
                } else {
                    candle = new Candle("series" + i, "data" + i);
                    nodes.add(candle);
                }
                plotCandleChartData(data.start.toInstant().toEpochMilli(), data, candle);
            }
            axisY.get().visualMinValue.set(min.multiply("0.995").toDouble());
        }
    }

    private final double DISTANCE_THRESHOLD = 0.5;

    private PlotLine plotline = new PlotLine();

    /**
     * Draw chart data.
     * 
     * @param data
     * @param path
     */
    protected void plotLineChartData(CandleChartData data, Path path) {
        if (data.size() == 0) {
            path.setVisible(false);
            return;
        } else {
            path.setVisible(true);
        }

        if (data.size() < 2000) {
            plotline.clearMemory();
        }

        Orientation orientation = getOrientation();
        int start, end;
        if (orientation == Orientation.HORIZONTAL) {// x軸方向昇順
            Axis axis = axisX.get();
            double low = axis.visualMinValue.get();
            double up = axis.visualMaxValue.get();
            start = data.searchXIndex(low, false);
            end = data.searchXIndex(up, true);

        } else {
            Axis axis = axisY.get();
            double low = axis.visualMinValue.get();
            double up = axis.visualMaxValue.get();
            start = data.searchYIndex(low, false);
            end = data.searchYIndex(up, true);
        }
        start = Math.max(0, start - 2);

        plotLineChartData(data, path.getElements(), start, end);
    }

    /**
     * Draw chart data.
     * 
     * @param data
     * @param elements
     * @param start
     * @param end
     */
    private void plotLineChartData(CandleChartData data, ObservableList<PathElement> elements, int start, int end) {
        int elementSize = elements.size();
        Axis xaxis = axisX.get();
        Axis yaxis = axisY.get();

        if (getOrientation() == Orientation.HORIZONTAL) {// x軸方向昇順
            boolean moveTo = true;
            double beforeX = 0, beforeY = 0;
            int elementIndex = 0;
            for (int i = start; i <= end; i++) {
                double x = data.getX(i);
                double y = data.getY(i);

                // 座標変換
                x = xaxis.getPositionForValue(x);
                y = yaxis.getPositionForValue(y);

                if (moveTo) {// 線が途切れている場合
                    if (elementIndex < elementSize) {
                        PathElement pathElement = elements.get(elementIndex);
                        if (pathElement.getClass() == MoveTo.class) {// 再利用
                            MoveTo m = ((MoveTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            MoveTo m = new MoveTo(x, y);
                            elements.set(elementIndex, m);// 置換
                        }
                        elementIndex++;
                    } else {
                        MoveTo m = new MoveTo(x, y);
                        elements.add(m);
                    }
                    moveTo = false;
                    beforeX = x;
                    beforeY = y;
                } else {// 線が続いている場合
                    double l = Math.hypot(x - beforeX, y - beforeY);
                    // 距離が小さすぎる場合は無視
                    if (l < DISTANCE_THRESHOLD) {
                        continue;
                    }
                    if (elementIndex < elementSize) {
                        final PathElement pathElement = elements.get(elementIndex);
                        if (pathElement.getClass() == LineTo.class) {
                            LineTo m = ((LineTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            LineTo m = new LineTo(x, y);
                            elements.set(elementIndex, m);
                        }
                        elementIndex++;
                    } else {
                        LineTo m = new LineTo(x, y);
                        elements.add(m);
                    }
                    beforeX = x;
                    beforeY = y;
                }
            } // end for

            if (elementIndex < elementSize) {
                elements.remove(elementIndex, elementSize);
            }
        } else {
            boolean moveTo = true;
            double beforeX = 0, beforeY = 0;
            int elei = 0;
            for (int i = start; i <= end; i++) {
                double x = data.getX(i);
                double y = data.getY(i);

                // 座標変換
                x = xaxis.getPositionForValue(x);
                y = yaxis.getPositionForValue(y);

                if (moveTo) {// 線が途切れている場合
                    if (elei < elementSize) {
                        PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == MoveTo.class) {// 再利用
                            MoveTo m = ((MoveTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            MoveTo m = new MoveTo(x, y);
                            elements.set(elei, m);// 置換
                        }
                        elei++;
                    } else {
                        MoveTo m = new MoveTo(x, y);
                        elements.add(m);
                    }
                    moveTo = false;
                    beforeY = y;
                    beforeX = x;
                } else {// 線が続いている場合
                    double l = Math.hypot(x - beforeX, y - beforeY);
                    // 距離が小さすぎる場合は無視
                    if (l < DISTANCE_THRESHOLD) {
                        continue;
                    }
                    if (elei < elementSize) {
                        PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == LineTo.class) {
                            LineTo m = ((LineTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            LineTo m = new LineTo(x, y);
                            elements.set(elei, m);
                        }
                        elei++;
                    } else {
                        LineTo m = new LineTo(x, y);
                        elements.add(m);
                    }
                    beforeY = y;
                    beforeX = x;
                }
            } // end for

            if (elei < elementSize) {
                elements.remove(elei, elementSize);
            }
        }

    }

    /**
     * Draw chart data.
     * 
     * @param data
     * @param candle
     * @param width
     * @param height
     */
    protected void plotCandleChartData(long index, Tick data, Candle candle) {
        double x = axisX.get().getPositionForValue(index);
        double open = axisY.get().getPositionForValue(data.openPrice.toDouble());
        double close = axisY.get().getPositionForValue(data.closePrice.toDouble());
        double high = axisY.get().getPositionForValue(data.maxPrice.toDouble());
        double low = axisY.get().getPositionForValue(data.minPrice.toDouble());

        // calculate candle width
        double candleWidth = 5;

        // update candle
        candle.update(close - open, high - open, low - open, candleWidth);
        // candle.updateTooltip(item.getYValue().doubleValue(), extra.getClose(), extra.getHigh(),
        // extra.getLow());

        // position the candle
        candle.setLayoutX(x);
        candle.setLayoutY(open);
    }

    /**
     * 縦方向に交互に背景を塗りつぶすかどうか
     * 
     * @return
     */
    public final BooleanProperty alternativeColumnFillVisibleProperty() {
        if (alternativeColumnFillVisibleProperty == null) {
            alternativeColumnFillVisibleProperty = new SimpleBooleanProperty(this, "alternativeColumnFillVisible", true);
        }
        return alternativeColumnFillVisibleProperty;
    }

    public final boolean isAlternativeColumnFillVisible() {
        return alternativeColumnFillVisibleProperty == null ? true : alternativeColumnFillVisibleProperty.get();
    }

    public final void setAlternativeColumnFillVisible(final boolean value) {
        alternativeColumnFillVisibleProperty().set(value);
    }

    private BooleanProperty alternativeColumnFillVisibleProperty;

    /**
     * 横方向に交互に背景を塗りつぶす
     * 
     * @return
     */
    public final BooleanProperty alternativeRowFillVisibleProperty() {
        if (alternativeRowFillVisibleProperty == null) {
            alternativeRowFillVisibleProperty = new SimpleBooleanProperty(this, "alternativeRowFillVisible", true);
        }
        return alternativeRowFillVisibleProperty;
    }

    public final boolean isAlternativeRowFillVisible() {
        return alternativeRowFillVisibleProperty == null ? true : alternativeRowFillVisibleProperty.get();
    }

    public final void setAlternativeRowFillVisible(final boolean value) {
        alternativeRowFillVisibleProperty().set(value);
    }

    private BooleanProperty alternativeRowFillVisibleProperty;

    private boolean graphshapeValidate = true;

    protected final void setGraphShapeValidate(final boolean b) {
        graphshapeValidate = b;
    }

    public final boolean isGraphShapeValidate() {
        return graphshapeValidate;
    }

    private InvalidationListener getGraphShapeValidateListener() {
        if (graphShapeValidateListener == null) {
            graphShapeValidateListener = o -> {
                final ReadOnlyBooleanProperty p = (ReadOnlyBooleanProperty) o;
                final boolean b = p.get();
                if (!b && isGraphShapeValidate()) {
                    setGraphShapeValidate(false);
                    setNeedsLayout(true);

                }
            };
        }
        return graphShapeValidateListener;
    }

    private InvalidationListener graphShapeValidateListener;

    private ObservableList<GraphShape> backGroundShapes, foreGroundShapes;

    public final ObservableList<GraphShape> getBackGroundShapes() {
        if (backGroundShapes == null) {
            backGroundShapes = FXCollections.observableArrayList();
            final ListChangeListener<GraphShape> l = (c) -> {
                c.next();
                final Group g = background;
                final ObservableList<Node> ch = g.getChildren();
                final InvalidationListener listener = getGraphShapeValidateListener();
                for (final GraphShape gl : c.getRemoved()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.remove(node);
                    }
                    gl.validateProperty().removeListener(listener);
                }
                for (final GraphShape gl : c.getAddedSubList()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.add(gl.getNode());
                    }
                    gl.validateProperty().addListener(listener);
                }
                if (plotValidate) {
                    setGraphShapeValidate(false);
                    plotValidate = false;
                    setNeedsLayout(true);
                }
            };
            backGroundShapes.addListener(l);
        }
        return backGroundShapes;
    }

    public final ObservableList<GraphShape> getForeGroundShapes() {
        if (foreGroundShapes == null) {
            foreGroundShapes = FXCollections.observableArrayList();
            final ListChangeListener<GraphShape> l = c -> {
                c.next();
                final Group g = foreground;
                final ObservableList<Node> ch = g.getChildren();
                final InvalidationListener listener = getGraphShapeValidateListener();
                for (final GraphShape gl : c.getRemoved()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.remove(node);
                    }
                    gl.validateProperty().removeListener(listener);
                }
                for (final GraphShape gl : c.getAddedSubList()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.add(gl.getNode());
                    }
                    gl.validateProperty().addListener(listener);
                }
                if (plotValidate) {
                    setGraphShapeValidate(false);
                    requestLayout();
                }
            };
            foreGroundShapes.addListener(l);
        }
        return foreGroundShapes;
    }

    /**
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setLineChartDataList(ObservableList<CandleChartData> datalist) {
        // clear old list configuration
        if (lineChartData != null) {
            lineChartData.removeListener(lineDataListObserver);
            for (CandleChartData data : lineChartData) {
                data.validateProperty().removeListener(lineDataObserver);
            }
            lineColorManager.clear();
        }

        // add new list configuration
        if (datalist != null) {
            datalist.addListener(lineDataListObserver);
            for (CandleChartData data : datalist) {
                data.defaultColorIndex = lineColorManager.nextClearBit(0);
                lineColorManager.set(data.defaultColorIndex, true);
                data.defaultColor = "default-color" + (data.defaultColorIndex % 8);
                data.validateProperty().addListener(lineDataObserver);
            }
        }

        // update
        lineChartData = datalist;

        if (plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    }

    /**
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setCandleChartDataList(ObservableList<Tick> datalist) {
        // clear old list configuration
        if (candleChartData != null) {
            // lineChartData.removeListener(lineDataListObserver);
            // for (LineChartData data : lineChartData) {
            // data.validateProperty().removeListener(lineDataObserver);
            // }
            // lineColorManager.clear();
        }

        // add new list configuration
        if (datalist != null) {
            // datalist.addListener(lineDataListObserver);
            // for (LineChartData data : datalist) {
            // data.defaultColorIndex = lineColorManager.nextClearBit(0);
            // lineColorManager.set(data.defaultColorIndex, true);
            // data.defaultColor = "default-color" + (data.defaultColorIndex % 8);
            // data.validateProperty().addListener(lineDataObserver);
            // }
        }

        // update
        candleChartData = datalist;

        if (plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    }

    /**
     * x軸方向に連続なデータか、y軸方向に連続なデータかを指定するプロパティ
     * 
     * @return
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientationProperty == null) {
            orientationProperty = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);
        }
        return orientationProperty;
    }

    public final Orientation getOrientation() {
        return orientationProperty == null ? Orientation.HORIZONTAL : orientationProperty.get();
    }

    public final void setOrientation(final Orientation value) {
        orientationProperty().set(value);
    }

    private ObjectProperty<Orientation> orientationProperty;

    /**
     * Get data for line chart.
     * 
     * @return
     */
    public final ObservableList<CandleChartData> getLineDataList() {
        return lineChartData;
    }

    /**
     * Get data for candle chart.
     * 
     * @return
     */
    public final ObservableList<Tick> getCandleDataList() {
        return candleChartData;
    }

    /**
     * @version 2017/09/26 1:20:10
     */
    private final class LocalGroup extends Group {

        /**
         * 
         */
        private LocalGroup() {
            setAutoSizeChildren(false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void requestLayout() {
        }
    }

}
