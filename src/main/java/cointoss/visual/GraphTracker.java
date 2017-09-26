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

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;

import cointoss.visual.shape.GraphCandle;
import cointoss.visual.shape.GraphLine;
import cointoss.visual.shape.GraphPointShape;

/**
 * GraphTrackingActionHandleを用いた実装例的な
 * 
 * @author nodamushi
 */
public class GraphTracker {
    private H h = new H();

    public GraphTracker() {
    }

    public GraphTracker(final LineChart graph) {
        install(graph);
    }

    public GraphTracker(final GraphPlotArea area) {
        install(area);
    }

    public void bind(final GraphTracker t) {
        if (t == null) {
            return;
        }
        h.bind(t.h);
    }

    public void unbind(final GraphTracker t) {
        if (t == null) {
            return;
        }
        h.unbind(t.h);
    }

    public void bindBidical(final GraphTracker t) {
        if (t == null) {
            return;
        }
        h.bindBidical(t.h);
    }

    public void unbind() {
        h.unbind();
    }

    /**
     * 既存のインストール先からはアンインストールされます
     * 
     * @param graph nullの場合は何もしない
     */
    public void install(final LineChart graph) {
        if (graph == null) {
            return;
        }
        h.install(graph);
    }

    /**
     * 既存のインストール先からはアンインストールされます
     * 
     * @param area nullの場合は何もしない
     */
    public void install(final GraphPlotArea area) {
        if (area == null) {
            return;
        }
        h.install(area);
    }

    public void uninstall() {
        h.uninstall(h.area);
    }

    private GraphPointShape create() {
        final GraphPointShapeFactory factory = getFactory();
        GraphPointShape ret;
        if (factory == null) {
            ret = new GraphCandle();
        } else {
            ret = factory.create();
        }
        ret.setVisible(false);
        ret.getStyleClass().add("graph-tracking-point");
        return ret;
    }

    public static interface GraphPointShapeFactory {
        public GraphPointShape create();
    }

    /**
     * @return
     */
    public final ObjectProperty<GraphPointShapeFactory> factoryProperty() {
        if (factoryProperty == null) {
            factoryProperty = new SimpleObjectProperty<GraphPointShapeFactory>(this, "factory", null) {
                @Override
                protected void invalidated() {
                    h.remakeShapes();
                }
            };
        }
        return factoryProperty;
    }

    public final GraphPointShapeFactory getFactory() {
        return factoryProperty == null ? null : factoryProperty.get();
    }

    public final void setFactory(final GraphPointShapeFactory value) {
        factoryProperty().set(value);
    }

    private ObjectProperty<GraphPointShapeFactory> factoryProperty;

    public static interface PointsDataView {
        /**
         * 点の情報を表示する。表示方法は問わない
         * 
         * @param area 対象のGraphPlotArea
         * @param value oがHORIZONTALのとき、xを意味する。そうでないときはy
         * @param values oがHORIZONTALのとき、yを意味する。そうでないときはx
         * @param o
         */
        public void show(GraphPlotArea area, double value, double[] values, Orientation o);

        /**
         * 点の情報を非表示にする
         * 
         * @param area
         */
        public void hidden(GraphPlotArea area);

        /**
         * このPointsDataViewをareaで用いなくなった場合に呼ばれる。
         * 
         * @param area
         */
        public void dispose(GraphPlotArea area);
    }

    /**
     * @return
     */
    public final ObjectProperty<PointsDataView> viewProperty() {
        if (viewProperty == null) {
            viewProperty = new SimpleObjectProperty<>(this, "view", null);
            viewProperty.addListener(new ChangeListener<PointsDataView>() {
                @Override
                public void changed(final ObservableValue<? extends PointsDataView> c, final PointsDataView o, final PointsDataView n) {
                    if (o != null && h.area != null) {
                        o.dispose(h.area);
                    }
                }
            });
        }
        return viewProperty;
    }

    public final PointsDataView getView() {
        return viewProperty == null ? null : viewProperty.get();
    }

    public final void setView(final PointsDataView value) {
        viewProperty().set(value);
    }

    private ObjectProperty<PointsDataView> viewProperty;

    private void show(final double v, final double[] vv) {
        final PointsDataView view = getView();
        if (view != null) {
            view.show(h.area, v, vv, h.area.getOrientation());
        }
    }

    private void hidden() {
        final PointsDataView view = getView();
        if (view != null) {
            view.hidden(h.area);
        }
    }

    private void dispose() {
        final PointsDataView view = getView();
        if (view != null) {
            view.dispose(h.area);
        }
    }

    private class H extends GraphTrackingActionHandler {

        private MouseEvent event;

        public void storeEvent(final MouseEvent event) {
            this.event = event;
        }

        public void rehandle() {
            if (event != null) {
                handle(event);
            }
        }

        private GraphPlotArea area;

        private GraphLine line;

        private List<GraphPointShape> points;

        @Override
        public boolean filter(final MouseEvent e) {
            final EventType<? extends MouseEvent> type = e.getEventType();
            return type == MouseEvent.MOUSE_DRAGGED || type == MouseEvent.MOUSE_PRESSED || type == MouseEvent.MOUSE_RELEASED;
        }

        public void remakeShapes() {
            final List<GraphPointShape> points = this.points;
            if (points == null || points.size() == 0) {
                return;
            }
            area.getForeGroundShapes().removeAll(points);
            for (int i = 0, e = points.size(); i < e; i++) {
                final GraphPointShape oldv = points.get(i);
                final GraphPointShape newv = create();
                newv.setVisible(oldv.isVisible());
                newv.setX(oldv.getX());
                newv.setY(oldv.getY());
                points.set(i, newv);
            }
            area.getForeGroundShapes().addAll(points);
        }

        @Override
        protected boolean handle(final MouseEvent e, final GraphPlotArea a, final double v, final double[] values) {
            final EventType<? extends MouseEvent> type = e.getEventType();
            if (type == MouseEvent.MOUSE_RELEASED || v != v) {
                storeEvent(null);
                hidden();
                if (line != null) {
                    line.setVisible(false);
                }
                if (points != null) {
                    for (final GraphPointShape s : points) {
                        s.setVisible(false);
                    }
                }
                return true;
            }
            storeEvent(e);
            final GraphPlotArea area = this.area;

            // type == drag or pressed
            if (line == null) {
                line = new GraphLine();
                line.getStyleClass().add("graph-tracking-line");
                area.getBackGroundShapes().add(line);
            }
            if (points == null) {
                points = new ArrayList<>(values.length);
            }

            if (points.size() < values.length) {
                final int s = points.size();
                for (int i = s; i < values.length; i++) {
                    points.add(create());
                }
                area.getForeGroundShapes().addAll(points.subList(s, values.length));
            }

            line.setValue(v);
            line.setVisible(true);
            if (area.getOrientation() == Orientation.HORIZONTAL) {
                // v = x,values = y
                line.setOrientation(Orientation.VERTICAL);
                for (int i = 0, ee = values.length; i < ee; i++) {
                    final double vv = values[i];
                    final GraphPointShape s = points.get(i);
                    if (vv != vv || Double.isInfinite(vv)) {
                        s.setVisible(false);
                    } else {
                        s.setVisible(true);
                        s.setX(v);
                        s.setY(vv);
                    }

                }
            } else {
                line.setOrientation(Orientation.HORIZONTAL);
                for (int i = 0, ee = values.length; i < ee; i++) {
                    final double vv = values[i];
                    final GraphPointShape s = points.get(i);
                    if (vv != vv || Double.isInfinite(vv)) {
                        s.setVisible(false);
                    } else {
                        s.setVisible(true);
                        s.setY(v);
                        s.setX(vv);
                    }
                }
            }

            show(v, values);
            return true;
        }

        InvalidationListener invalidatelistener = o -> {
            final ReadOnlyBooleanProperty b = (ReadOnlyBooleanProperty) o;
            if (!b.get()) {
                // TODO requestLayoutと違って無駄な計算が多すぎる
                // 呼び出しタイミングをコントロールできないかな？
                rehandle();
            }
        };

        @Override
        public void install(final GraphPlotArea g) {
            if (area != null) {
                uninstall(area);
            }
            area = g;
            g.addEventHandler(MouseEvent.ANY, this);

            if (line != null) {
                g.getBackGroundShapes().add(line);
            }

            if (points != null) {
                g.getForeGroundShapes().addAll(points);
            }

            for (final LineChartData d : g.getDataList()) {
                d.validateProperty().addListener(invalidatelistener);
            }

        }

        @Override
        public void uninstall(final GraphPlotArea g) {
            if (area != null && area == g) {
                g.removeEventHandler(MouseEvent.ANY, this);
                if (line != null) {
                    g.getBackGroundShapes().remove(line);
                }
                if (points != null && points.size() != 0) {
                    g.getForeGroundShapes().removeAll(points);
                }
                for (final LineChartData d : g.getDataList()) {
                    d.validateProperty().removeListener(invalidatelistener);
                }

                dispose();
                area = null;
            }
        }

        @Override
        protected GraphPlotArea getTargetPlotArea() {
            return area;
        }
    }
}
