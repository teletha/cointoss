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

import java.util.BitSet;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

/**
 * グラフを実際に描画するエリアです
 */
public class GraphPlotArea extends Region {

    /** The validator. */
    protected final InvalidationListener plotValidateListener = observable -> {
        if (isPlotValidate()) {
            setPlotValidate(false);
            setGraphShapeValidate(false);
            setNeedsLayout(true);
        }
    };

    private final Rectangle clip = new Rectangle();

    private final Group background = new LocalGroup();

    private final Group plotArea = new LocalGroup();

    private final Group foreground = new LocalGroup();

    private final Group userBackround = new LocalGroup();

    private final Group userForeground = new LocalGroup();

    private final Path verticalGridLines = new Path();

    private final Path horizontalGridLines = new Path();

    private final Path verticalMinorGridLines = new Path();

    private final Path horizontalMinorGridLines = new Path();

    private final Path verticalRowFill = new Path();

    private final Path horizontalRowFill = new Path();

    /**
     * 
     */
    public GraphPlotArea() {
        getStyleClass().setAll("chart-plot-background");
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
                .addAll(verticalRowFill, horizontalRowFill, verticalMinorGridLines, horizontalMinorGridLines, verticalGridLines, horizontalGridLines, background, userBackround, plotArea, foreground, userForeground);
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
        if (isAutoPlot() && !isPlotValidate()) {
            plotData();
        }
        if (!isGraphShapeValidate()) {
            drawGraphShapes();
        }
    }

    public void plotData() {
        final Axis xaxis = getXAxis();
        final Axis yaxis = getYAxis();

        if (xaxis == null || yaxis == null) {
            setPlotValidate(true);
            setGraphShapeValidate(true);
            return;
        }
        final double w = getWidth(), h = getHeight();
        drawGraphShapes();
        if (!isPlotValidate()) {
            drawBackGroundLine();
            plotLineChartDatas(w, h);
            setPlotValidate(true);
        }
    }

    public void drawGraphShapes() {
        if (isPlotValidate() && isGraphShapeValidate()) {
            return;
        }
        final Axis xaxis = getXAxis();
        final Axis yaxis = getYAxis();

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
        final Axis xaxis = getXAxis();
        final Axis yaxis = getYAxis();

        if (xaxis == null || yaxis == null) {
            setPlotValidate(true);
            return;
        }

        final double w = getWidth(), h = getHeight();
        // 背景の線を描画

        V: {
            final Axis axis = xaxis;
            final List<Double> vTicks = axis.getMajorTicks();
            final List<Boolean> vFill = axis.getMajorTicksFill();
            final ObservableList<PathElement> lele = verticalGridLines.getElements();
            final ObservableList<PathElement> fele = verticalRowFill.getElements();
            int lelesize = lele.size();
            final int felesize = fele.size();
            final boolean fill = isAlternativeColumnFillVisible();
            final boolean line = isVerticalGridLinesVisible();
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
            final List<Double> hTicks = axis.getMajorTicks();
            final List<Boolean> hFill = axis.getMajorTicksFill();
            final ObservableList<PathElement> lele = horizontalGridLines.getElements();
            final ObservableList<PathElement> fele = horizontalRowFill.getElements();
            int lelesize = lele.size();
            final int felesize = fele.size();
            final boolean fill = isAlternativeRowFillVisible();
            final boolean line = isHorizontalGridLinesVisible();
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

        if (isVerticalMinorGridLinesVisible()) {
            final Axis axis = xaxis;
            final List<Double> minorTicks = axis.getMinorTicks();

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

        if (isHorizontalMinorGridLinesVisible()) {
            final Axis axis = yaxis;
            final List<Double> minorTicks = axis.getMinorTicks();

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

    protected void plotLineChartDatas(final double width, final double height) {
        final Group g = plotArea;
        final ObservableList<Node> paths = g.getChildren();
        final List<LineChartData> datas = linechartData;
        if (datas == null) {
            paths.clear();
        } else {
            final int size = datas.size();
            int psize = paths.size();
            if (size < psize) {
                paths.remove(size, psize);
                psize = size;
            }

            for (int i = 0; i < size; i++) {
                final int defaultColorIndex = 2;
                final LineChartData d = datas.get(i);
                Path p;
                if (i < psize) {
                    p = (Path) paths.get(i);
                } else {
                    p = new Path();
                    p.setStrokeLineJoin(StrokeLineJoin.BEVEL);
                    // 順序とかあるのかね？
                    p.getStyleClass().setAll("chart-series-line", "series" + i, d.defaultColor);
                    paths.add(p);
                }
                final ObservableList<String> sc = p.getStyleClass();

                if (!sc.get(defaultColorIndex).equals(d.defaultColor)) {
                    sc.set(defaultColorIndex, d.defaultColor);
                }

                plotLineChartData(d, p, width, height);
            }
        }
    }

    private final double DISTANCE_THRESHOLD = 0.5;

    private PlotLine plotline = new PlotLine();

    protected void plotLineChartData(final LineChartData data, final Path path, final double width, final double height) {
        final ObservableList<PathElement> elements = path.getElements();
        if (data.size() == 0) {
            path.setVisible(false);
            return;
        } else {
            path.setVisible(true);
        }

        if (data.size() < 2000) {
            plotline.clearMemory();
        }

        final Orientation orientation = getOrientation();
        int start, end;
        if (orientation == Orientation.HORIZONTAL) {// x軸方向昇順
            final Axis axis = getXAxis();
            final double low = axis.getLowerValue();
            final double up = axis.getUpperValue();
            start = data.searchXIndex(low, false);
            end = data.searchXIndex(up, true);

        } else {

            final Axis axis = getYAxis();
            final double low = axis.getLowerValue();
            final double up = axis.getUpperValue();
            start = data.searchYIndex(low, false);
            end = data.searchYIndex(up, true);
        }
        start = max(0, start - 2);

        if (end - start < 2000) {
            plotLineChartData_min(data, path, width, height, start, end);
        } else {
            plotline.setOrientationX(getOrientation() == Orientation.HORIZONTAL);
            plotline.init();
            plotLineChartData_large(data, path, width, height, start, end);
            plotline.toElements(elements);
        }

    }

    private void plotLineChartData_min(final LineChartData data, final Path path, final double width, final double height, final int start, final int end) {
        final ObservableList<PathElement> elements = path.getElements();
        final int esize = elements.size();
        final Axis xaxis = getXAxis();
        final Axis yaxis = getYAxis();
        final Orientation orientation = getOrientation();

        if (orientation == Orientation.HORIZONTAL) {// x軸方向昇順
            boolean moveTo = true;
            boolean fromInfinit = false;
            boolean positivInf = false;
            double beforeX = 0, beforeY = 0;
            int elei = 0;
            for (int i = start; i <= end; i++) {
                double x = data.getX(i);
                double y = data.getY(i);

                // NaNの場合は線を途切れさせる
                if (y != y) {
                    moveTo = true;
                    fromInfinit = false;
                    continue;
                }
                // 無限の場合は垂直な線を引く
                if (Double.isInfinite(y)) {
                    // 線が途切れていたり、その前も無限の場合は何もしない
                    positivInf = y > 0;
                    if (!moveTo && !fromInfinit) {
                        beforeY = positivInf ? 0 : height;
                        if (elei < esize) {
                            final PathElement pathElement = elements.get(elei);
                            if (pathElement.getClass() == LineTo.class) {
                                final LineTo m = ((LineTo) pathElement);
                                m.setX(beforeX);
                                m.setY(beforeY);
                            } else {
                                final LineTo m = new LineTo(beforeX, beforeY);
                                elements.set(elei, m);
                            }
                            elei++;
                        } else {
                            final LineTo m = new LineTo(beforeX, beforeY);
                            elements.add(m);
                        }
                    }
                    // 無限フラグを立てる
                    fromInfinit = true;
                    moveTo = false;
                    // 次の処理へ
                    continue;
                }
                // 実数の処理

                // 座標変換
                x = xaxis.getDisplayPosition(x);
                y = yaxis.getDisplayPosition(y);

                // 前回が無限の時は垂直線を書く
                if (fromInfinit) {
                    beforeX = x;
                    beforeY = positivInf ? 0 : height;
                    if (elei < esize) {
                        final PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == MoveTo.class) {// 再利用
                            final MoveTo m = ((MoveTo) pathElement);
                            m.setX(x);
                            m.setY(beforeY);
                        } else {
                            final MoveTo m = new MoveTo(x, beforeY);
                            elements.set(elei, m);// 置換
                        }
                        elei++;
                    } else {
                        final MoveTo m = new MoveTo(x, beforeY);
                        elements.add(m);
                    }
                    moveTo = false;// moveToは不要になる
                }

                fromInfinit = false;

                if (moveTo) {// 線が途切れている場合
                    if (elei < esize) {
                        final PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == MoveTo.class) {// 再利用
                            final MoveTo m = ((MoveTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            final MoveTo m = new MoveTo(x, y);
                            elements.set(elei, m);// 置換
                        }
                        elei++;
                    } else {
                        final MoveTo m = new MoveTo(x, y);
                        elements.add(m);
                    }
                    moveTo = false;
                    beforeX = x;
                    beforeY = y;
                } else {// 線が続いている場合
                    final double l = hypot(x - beforeX, y - beforeY);
                    // 距離が小さすぎる場合は無視
                    if (l < DISTANCE_THRESHOLD) {
                        continue;
                    }
                    if (elei < esize) {
                        final PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == LineTo.class) {
                            final LineTo m = ((LineTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            final LineTo m = new LineTo(x, y);
                            elements.set(elei, m);
                        }
                        elei++;
                    } else {
                        final LineTo m = new LineTo(x, y);
                        elements.add(m);
                    }
                    beforeX = x;
                    beforeY = y;
                }
            } // end for

            if (elei < esize) {
                elements.remove(elei, esize);
            }
        } else {

            boolean moveTo = true;
            boolean fromInfinit = false;
            boolean positivInf = false;
            double beforeX = 0, beforeY = 0;
            int elei = 0;
            for (int i = start; i <= end; i++) {
                double x = data.getX(i);
                double y = data.getY(i);

                // NaNの場合は線を途切れさせる
                if (x != x) {
                    moveTo = true;
                    fromInfinit = false;
                    continue;
                }
                // 無限の場合は垂直な線を引く
                if (Double.isInfinite(x)) {
                    // 線が途切れていたり、その前も無限の場合は何もしない
                    positivInf = x > 0;
                    if (!moveTo && !fromInfinit) {
                        beforeX = positivInf ? width : 0;
                        if (elei < esize) {
                            final PathElement pathElement = elements.get(elei);
                            if (pathElement.getClass() == LineTo.class) {
                                final LineTo m = ((LineTo) pathElement);
                                m.setX(beforeX);
                                m.setY(beforeY);
                            } else {
                                final LineTo m = new LineTo(beforeX, beforeY);
                                elements.set(elei, m);
                            }
                            elei++;
                        } else {
                            final LineTo m = new LineTo(beforeX, beforeY);
                            elements.add(m);
                        }
                    }
                    // 無限フラグを立てる
                    fromInfinit = true;
                    moveTo = false;
                    // 次の処理へ
                    continue;
                }
                // 実数の処理

                // 座標変換
                x = xaxis.getDisplayPosition(x);
                y = yaxis.getDisplayPosition(y);

                // 前回が無限の時は水平線を書く
                if (fromInfinit) {
                    beforeY = y;
                    beforeX = positivInf ? width : 0;
                    if (elei < esize) {
                        final PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == MoveTo.class) {// 再利用
                            final MoveTo m = ((MoveTo) pathElement);
                            m.setX(beforeX);
                            m.setY(y);
                        } else {
                            final MoveTo m = new MoveTo(beforeX, y);
                            elements.set(elei, m);// 置換
                        }
                        elei++;
                    } else {
                        final MoveTo m = new MoveTo(beforeX, y);
                        elements.add(m);
                    }
                    moveTo = false;// moveToは不要になる
                }

                fromInfinit = false;

                if (moveTo) {// 線が途切れている場合
                    if (elei < esize) {
                        final PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == MoveTo.class) {// 再利用
                            final MoveTo m = ((MoveTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            final MoveTo m = new MoveTo(x, y);
                            elements.set(elei, m);// 置換
                        }
                        elei++;
                    } else {
                        final MoveTo m = new MoveTo(x, y);
                        elements.add(m);
                    }
                    moveTo = false;
                    beforeY = y;
                    beforeX = x;
                } else {// 線が続いている場合
                    final double l = hypot(x - beforeX, y - beforeY);
                    // 距離が小さすぎる場合は無視
                    if (l < DISTANCE_THRESHOLD) {
                        continue;
                    }
                    if (elei < esize) {
                        final PathElement pathElement = elements.get(elei);
                        if (pathElement.getClass() == LineTo.class) {
                            final LineTo m = ((LineTo) pathElement);
                            m.setX(x);
                            m.setY(y);
                        } else {
                            final LineTo m = new LineTo(x, y);
                            elements.set(elei, m);
                        }
                        elei++;
                    } else {
                        final LineTo m = new LineTo(x, y);
                        elements.add(m);
                    }
                    beforeY = y;
                    beforeX = x;
                }
            } // end for

            if (elei < esize) {
                elements.remove(elei, esize);
            }

        }

    }

    private void plotLineChartData_large(final LineChartData data, final Path path, final double width, final double height, final int start, final int end) {
        // final ObservableList<PathElement> elements = path.getElements();
        // final int esize = elements.size();
        final Axis xaxis = getXAxis();
        final Axis yaxis = getYAxis();
        final Orientation orientation = getOrientation();

        final PlotLine line = plotline;

        if (orientation == Orientation.HORIZONTAL) {// x軸方向昇順
            boolean moveTo = true;
            boolean fromInfinit = false;
            boolean positivInf = false;
            double beforeX = 0, beforeY = 0;
            for (int i = start; i <= end; i++) {
                double x = data.getX(i);
                double y = data.getY(i);

                // NaNの場合は線を途切れさせる
                if (y != y) {
                    moveTo = true;
                    fromInfinit = false;
                    continue;
                }
                // 無限の場合は垂直な線を引く
                if (Double.isInfinite(y)) {
                    // 線が途切れていたり、その前も無限の場合は何もしない
                    positivInf = y > 0;
                    if (!moveTo && !fromInfinit) {
                        beforeY = positivInf ? 0 : height;
                        line.add(1, beforeX, beforeY);
                    }
                    // 無限フラグを立てる
                    fromInfinit = true;
                    moveTo = false;
                    // 次の処理へ
                    continue;
                }
                // 実数の処理

                // 座標変換
                x = xaxis.getDisplayPosition(x);
                y = yaxis.getDisplayPosition(y);

                // 前回が無限の時は垂直線を書く
                if (fromInfinit) {
                    beforeX = x;
                    beforeY = positivInf ? 0 : height;
                    line.add(0, x, beforeY);
                    moveTo = false;// moveToは不要になる
                }

                fromInfinit = false;

                if (moveTo) {// 線が途切れている場合
                    line.add(0, x, y);
                    moveTo = false;
                    beforeX = x;
                    beforeY = y;
                } else {// 線が続いている場合
                    final double l = hypot(x - beforeX, y - beforeY);
                    // 距離が小さすぎる場合は無視
                    if (l < DISTANCE_THRESHOLD) {
                        continue;
                    }
                    line.add(1, x, y);
                    beforeX = x;
                    beforeY = y;
                }
            } // end for
        } else {

            boolean moveTo = true;
            boolean fromInfinit = false;
            boolean positivInf = false;
            double beforeX = 0, beforeY = 0;
            for (int i = start; i <= end; i++) {
                double x = data.getX(i);
                double y = data.getY(i);

                // NaNの場合は線を途切れさせる
                if (x != x) {
                    moveTo = true;
                    fromInfinit = false;
                    continue;
                }
                // 無限の場合は垂直な線を引く
                if (Double.isInfinite(x)) {
                    // 線が途切れていたり、その前も無限の場合は何もしない
                    positivInf = x > 0;
                    if (!moveTo && !fromInfinit) {
                        beforeX = positivInf ? width : 0;
                        line.add(0, beforeX, beforeY);
                    }
                    // 無限フラグを立てる
                    fromInfinit = true;
                    moveTo = false;
                    // 次の処理へ
                    continue;
                }
                // 実数の処理

                // 座標変換
                x = xaxis.getDisplayPosition(x);
                y = yaxis.getDisplayPosition(y);

                // 前回が無限の時は垂直線を書く
                if (fromInfinit) {
                    beforeY = y;
                    beforeX = positivInf ? width : 0;
                    line.add(0, beforeX, y);
                    moveTo = false;// moveToは不要になる
                }

                fromInfinit = false;

                if (moveTo) {// 線が途切れている場合
                    line.add(0, x, y);
                    moveTo = false;
                    beforeY = y;
                    beforeX = x;
                } else {// 線が続いている場合
                    final double l = hypot(x - beforeX, y - beforeY);
                    // 距離が小さすぎる場合は無視
                    if (l < DISTANCE_THRESHOLD) {
                        continue;
                    }
                    line.add(1, x, y);
                    beforeY = y;
                    beforeX = x;
                }
            } // end for
        }

    }

    // ----------------------------------------------------------------

    /**
     * 横方向のグリッド線を表示するかどうかのプロパティ
     * 
     * @return
     */
    public final BooleanProperty horizontalGridLinesVisibleProperty() {
        if (horizontalGridLinesVisibleProperty == null) {
            horizontalGridLinesVisibleProperty = new SimpleBooleanProperty(this, "horizontalGridLinesVisible", true);
        }
        return horizontalGridLinesVisibleProperty;
    }

    public final boolean isHorizontalGridLinesVisible() {
        return horizontalGridLinesVisibleProperty == null ? true : horizontalGridLinesVisibleProperty.get();
    }

    public final void setHorizontalGridLinesVisible(final boolean value) {
        horizontalGridLinesVisibleProperty().set(value);
    }

    private BooleanProperty horizontalGridLinesVisibleProperty;

    /**
     * 縦方向のグリッド線を表示するかどうかのプロパティ
     * 
     * @return
     */
    public final BooleanProperty verticalGridLinesVisibleProperty() {
        if (verticalGridLinesVisibleProperty == null) {
            verticalGridLinesVisibleProperty = new SimpleBooleanProperty(this, "verticalGridLinesVisible", true);
        }
        return verticalGridLinesVisibleProperty;
    }

    public final boolean isVerticalGridLinesVisible() {
        return verticalGridLinesVisibleProperty == null ? true : verticalGridLinesVisibleProperty.get();
    }

    public final void setVerticalGridLinesVisible(final boolean value) {
        verticalGridLinesVisibleProperty().set(value);
    }

    private BooleanProperty verticalGridLinesVisibleProperty;

    /**
     * 横方向minor tickの線の可視性
     * 
     * @return
     */
    public final BooleanProperty horizontalMinorGridLinesVisibleProperty() {
        if (horizontalMinorGridLinesVisibleProperty == null) {
            horizontalMinorGridLinesVisibleProperty = new SimpleBooleanProperty(this, "horizontalMinorGridLinesVisible", false);
            horizontalMinorGridLines.visibleProperty().bind(horizontalMinorGridLinesVisibleProperty);
        }
        return horizontalMinorGridLinesVisibleProperty;
    }

    public final boolean isHorizontalMinorGridLinesVisible() {
        return horizontalMinorGridLinesVisibleProperty == null ? false : horizontalMinorGridLinesVisibleProperty.get();
    }

    public final void setHorizontalMinorGridLinesVisible(final boolean value) {
        horizontalMinorGridLinesVisibleProperty().set(value);
    }

    private BooleanProperty horizontalMinorGridLinesVisibleProperty;

    /**
     * 縦方向minor tickの線の可視性
     * 
     * @return
     */
    public final BooleanProperty verticalMinorGridLinesVisibleProperty() {
        if (verticalMinorGridLinesVisibleProperty == null) {
            verticalMinorGridLinesVisibleProperty = new SimpleBooleanProperty(this, "verticalMinorGridLinesVisible", false);
            verticalMinorGridLines.visibleProperty().bind(verticalMinorGridLinesVisibleProperty);
        }
        return verticalMinorGridLinesVisibleProperty;
    }

    public final boolean isVerticalMinorGridLinesVisible() {
        return verticalMinorGridLinesVisibleProperty == null ? false : verticalMinorGridLinesVisibleProperty.get();
    }

    public final void setVerticalMinorGridLinesVisible(final boolean value) {
        verticalMinorGridLinesVisibleProperty().set(value);
    }

    private BooleanProperty verticalMinorGridLinesVisibleProperty;

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

    private ChangeListener<Axis> axisListener = (observable, oldValue, newValue) -> {
        if (oldValue != null) {
            oldValue.lowerValueProperty().removeListener(plotValidateListener);
            oldValue.visibleAmountProperty().removeListener(plotValidateListener);
        }

        if (newValue != null) {
            newValue.lowerValueProperty().addListener(plotValidateListener);
            newValue.visibleAmountProperty().addListener(plotValidateListener);
        }
        if (isPlotValidate()) {
            setPlotValidate(false);
            requestLayout();
        }
    };

    /**
     * x-axis
     * 
     * @return
     */
    public final ObjectProperty<Axis> xAxisProperty() {
        if (xAxisProperty == null) {
            xAxisProperty = new SimpleObjectProperty<>(this, "xAxis", null);
            xAxisProperty.addListener(axisListener);
        }
        return xAxisProperty;
    }

    public final Axis getXAxis() {
        return xAxisProperty == null ? null : xAxisProperty.get();
    }

    public final void setXAxis(final Axis value) {
        xAxisProperty().set(value);
    }

    private ObjectProperty<Axis> xAxisProperty;

    /**
     * y-axis
     * 
     * @return
     */
    public final ObjectProperty<Axis> yAxisProperty() {
        if (yAxisProperty == null) {
            yAxisProperty = new SimpleObjectProperty<>(this, "yAxis", null);
            yAxisProperty.addListener(axisListener);
        }
        return yAxisProperty;
    }

    public Axis getYAxis() {
        return yAxisProperty == null ? null : yAxisProperty.get();
    }

    public final void setYAxis(final Axis value) {
        yAxisProperty().set(value);
    }

    private ObjectProperty<Axis> yAxisProperty;

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
                if (isPlotValidate()) {
                    setGraphShapeValidate(false);
                    setPlotValidate(false);
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
                if (isPlotValidate()) {
                    setGraphShapeValidate(false);
                    requestLayout();
                }
            };
            foreGroundShapes.addListener(l);
        }
        return foreGroundShapes;
    }

    private ObservableList<LineChartData> linechartData;

    private ListChangeListener<LineChartData> dataListListener;

    private InvalidationListener dataListener;

    private BitSet colorIndex = new BitSet(8);

    protected final InvalidationListener getDataListener() {
        if (dataListener == null) {
            dataListener = o -> {
                final ReadOnlyBooleanProperty b = (ReadOnlyBooleanProperty) o;
                if (!b.get() && isPlotValidate()) {
                    setPlotValidate(false);
                    setNeedsLayout(true);
                }
            };
        }
        return dataListener;
    }

    public final void setLineChartDataList(final ObservableList<LineChartData> datalist) {
        if (dataListListener == null) {
            dataListListener = (c) -> {
                c.next();
                final InvalidationListener dataListener = getDataListener();
                for (final LineChartData d : c.getRemoved()) {
                    colorIndex.clear(d.defaultColorIndex);
                    d.validateProperty().removeListener(dataListener);
                }
                for (final LineChartData d : c.getAddedSubList()) {
                    d.defaultColorIndex = colorIndex.nextClearBit(0);
                    colorIndex.set(d.defaultColorIndex, true);
                    d.defaultColor = "default-color" + (d.defaultColorIndex % 8);
                    d.validateProperty().addListener(dataListener);
                }
            };
        }

        final ObservableList<LineChartData> old = linechartData;
        final InvalidationListener dataListener = getDataListener();
        if (old != null) {
            old.removeListener(dataListListener);
            for (final LineChartData d : old) {
                d.validateProperty().removeListener(dataListener);
            }
            colorIndex.clear();
        }

        if (datalist != null) {
            datalist.addListener(dataListListener);
            for (final LineChartData d : datalist) {
                d.defaultColorIndex = colorIndex.nextClearBit(0);
                colorIndex.set(d.defaultColorIndex, true);
                d.defaultColor = "default-color" + (d.defaultColorIndex % 8);
                d.validateProperty().addListener(dataListener);
            }
        }

        linechartData = datalist;
        if (isPlotValidate()) {
            setPlotValidate(false);
            setNeedsLayout(true);
        }
    }

    /**
     * layoutChildrenを実行時に自動的にグラフエリアの描画も行うかどうか。 falseにした場合は必要なときに自分でplotDataを呼び出す必要がある。 デフォルトはtrue
     * 
     * @return
     */
    public final BooleanProperty autoPlotProperty() {
        if (autoPlotProperty == null) {
            autoPlotProperty = new SimpleBooleanProperty(this, "autoPlot", true);
        }
        return autoPlotProperty;
    }

    public final boolean isAutoPlot() {
        return autoPlotProperty == null ? true : autoPlotProperty.get();
    }

    public final void setAutoPlot(final boolean value) {
        autoPlotProperty().set(value);
    }

    private BooleanProperty autoPlotProperty;

    protected final boolean isPlotValidate() {
        return plotValidate;
    }

    protected final void setPlotValidate(final boolean bool) {
        plotValidate = bool;
    }

    /** 状態の正当性を示すプロパティ */
    private boolean plotValidate = false;

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

    private GraphLine verticalZeroLine, horizontalZeroLine;

    public final GraphLine getVerticalZeroLine() {
        if (verticalZeroLine == null) {
            verticalZeroLine = new GraphLine();
            verticalZeroLine.setOrientation(Orientation.VERTICAL);
            verticalZeroLine.getStyleClass().setAll("chart-vertical-zero-line");
        }
        return verticalZeroLine;
    }

    public final GraphLine getHorizontalZeroLine() {
        if (horizontalZeroLine == null) {
            horizontalZeroLine = new GraphLine();
            horizontalZeroLine.setOrientation(Orientation.HORIZONTAL);
            horizontalZeroLine.getStyleClass().setAll("chart-horizontal-zero-line");
        }
        return horizontalZeroLine;
    }

    public void showVerticalZeroLine() {
        final ObservableList<GraphShape> backGroundLine = getBackGroundShapes();
        final GraphLine l = getVerticalZeroLine();
        l.setVisible(true);
        if (!backGroundLine.contains(l)) {
            backGroundLine.add(l);
        }
    }

    public void showHorizontalZeroLine() {
        final ObservableList<GraphShape> backGroundLine = getBackGroundShapes();
        final GraphLine l = getHorizontalZeroLine();
        l.setVisible(true);
        if (!backGroundLine.contains(l)) {
            backGroundLine.add(l);
        }
    }

    public final ObservableList<LineChartData> getDataList() {
        return linechartData;
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
