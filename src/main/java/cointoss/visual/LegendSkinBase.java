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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

public class LegendSkinBase extends TilePane implements Skin<Legend> {
    private Legend legend;

    protected String getDefaultColorStyleClass(final LineChartData data) {
        return data.defaultColor;
    }

    private static final double GAP = 5;

    private InvalidationListener lcdListener = new InvalidationListener() {
        @Override
        public void invalidated(final Observable observable) {
            requestLayout();
        }
    };

    private ChangeListener<ObservableList<LineChartData>> changelistener = new ChangeListener<ObservableList<LineChartData>>() {
        @Override
        public void changed(final ObservableValue<? extends ObservableList<LineChartData>> a, final ObservableList<LineChartData> o, final ObservableList<LineChartData> n) {
            if (o != null) {
                o.removeListener(listener);
                for (final LineChartData d : o) {
                    d.nameProperty.removeListener(lcdListener);
                }
            }
            if (n != null) {
                n.addListener(listener);
                for (final LineChartData d : n) {
                    d.nameProperty.addListener(lcdListener);
                }
            }

        }
    };

    private ListChangeListener<LineChartData> listener = new ListChangeListener<LineChartData>() {
        @Override
        public void onChanged(final Change<? extends LineChartData> c) {
            while (c.next()) {
                if (c.getRemovedSize() != 0) {
                    for (final LineChartData d : c.getRemoved()) {
                        d.nameProperty.removeListener(lcdListener);
                    }
                }
                if (c.getAddedSize() != 0) {
                    for (final LineChartData d : c.getAddedSubList()) {
                        d.nameProperty.addListener(lcdListener);
                    }
                }
            }
        }

    };

    public LegendSkinBase(final Legend l) {
        super(GAP, GAP);
        getStyleClass().add("chart-legend");
        legend = l;
        orientationProperty().bind(l.orientationProperty());
        l.dataListProperty().addListener(changelistener);
        setTileAlignment(Pos.CENTER_LEFT);
        makeChildren(l.getDataList(), getChildren());
    }

    @Override
    protected void layoutChildren() {
        makeChildren(getSkinnable().getDataList(), getChildren());
        super.layoutChildren();
    }

    @Override
    protected double computePrefHeight(final double forWidth) {
        final ObservableList<LineChartData> list = legend.getDataList();
        if (list == null || list.size() == 0) {
            return 0;
        }
        return super.computePrefHeight(forWidth);
    }

    @Override
    protected double computePrefWidth(final double forHeight) {
        final ObservableList<LineChartData> list = legend.getDataList();
        if (list == null || list.size() == 0) {
            return 0;
        }
        return super.computePrefWidth(forHeight);
    }

    /**
     * @param datas Legend.getDataList() nullable
     * @param children getChildren()
     */
    protected void makeChildren(final ObservableList<LineChartData> datas, final ObservableList<Node> children) {
        if (datas == null) {
            children.clear();
            return;
        }

        final int childsize = children.size();
        int index = 0;
        int i = 0;
        for (final LineChartData d : datas) {
            Label l = null;
            for (; index < childsize; index++) {
                final Node n = children.get(index);
                if (n instanceof Label) {
                    l = (Label) n;
                    index++;
                    break;
                }
            }
            final Label l2 = setProperty(d, l, i);
            if (l != l2) {
                if (l == null) {
                    children.add(l2);
                } else {
                    children.set(index - 1, l2);
                }
            }
            i++;
        }
    }

    /**
     * @param data
     * @param l if l is null,create new Label instance.
     * @return
     */
    protected Label setProperty(final LineChartData data, Label l, final int index) {
        if (l == null) {
            l = new Label();
            l.getStyleClass().add("chart-legend-item");
            final Node icon = createIcon();
            final String s = getDefaultColorStyleClass(data);
            if (s != null) {
                icon.getStyleClass().add(s);
            }
            icon.getStyleClass().addAll("series" + index, "chart-legend-item-symbol", "chart-line-symbol");
            l.setGraphic(icon);
        }
        l.setText(data.nameProperty.get());
        final Node icon = l.getGraphic();
        final ObservableList<String> sc = icon.getStyleClass();
        final String series = "series" + index;
        final int sindex = data.defaultColor == null ? 0 : 1;

        if (sc.size() > 1) {
            final String s = sc.get(0);
            if (s.startsWith("default-color")) {
                if (!s.equals(data.defaultColor)) {
                    if (data.defaultColor == null) {
                        sc.remove(0);
                    } else {
                        sc.set(0, data.defaultColor);
                    }
                }
            } else if (data.defaultColor != null) {
                sc.add(0, data.defaultColor);
            }
        } else if (data.defaultColor != null) {
            sc.add(0, data.defaultColor);
        }

        if (sc.size() > sindex) {
            final String s = sc.get(sindex);
            if (s.startsWith("series")) {
                if (!s.equalsIgnoreCase(series)) {
                    sc.set(sindex, series);
                }
            } else {
                sc.add(sindex, series);
            }
        } else {
            sc.add(sindex, series);
        }

        return l;
    }

    protected Node createIcon() {
        return new Region();
    }

    @Override
    public void dispose() {
        final Legend l = getSkinnable();
        l.dataListProperty().removeListener(changelistener);
        if (l.getDataList() != null) {
            l.getDataList().removeListener(listener);
            for (final LineChartData d : l.getDataList()) {
                d.nameProperty.removeListener(lcdListener);
            }
        }
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Legend getSkinnable() {
        return legend;
    }
}
