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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;

/**
 * グラフのレジェンドの表示をする。<br/>
 * 表示される名前はLiceChartDataのnamePropertyを利用する。
 * 
 * @author nodamushi
 */
public class Legend extends Control {

    private static final String BASE_SKIN = "-fx-skin:'" + LegendSkinBase.class.getName() + "';";

    public Legend() {
        setStyle(BASE_SKIN);
    }

    /**
     * 配置の方向
     * 
     * @return
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientationProperty == null) {
            orientationProperty = new SimpleObjectProperty<Orientation>(this, "orientation", Orientation.HORIZONTAL) {
                @Override
                public void set(final Orientation v) {
                    if (v == null) {
                        return;
                    }
                    super.set(v);
                }

                @Override
                protected void invalidated() {
                    requestLayout();
                    super.invalidated();
                }
            };
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
     * @return
     */
    public final ObjectProperty<ObservableList<CandleChartData>> dataListProperty() {
        if (dataListProperty == null) {
            dataListProperty = new SimpleObjectProperty<>(this, "dataList", null);
            dataListProperty.addListener(new ChangeListener<ObservableList<CandleChartData>>() {
                InvalidationListener lis = new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable arg0) {
                        requestLayout();
                    }
                };

                @Override
                public void changed(final ObservableValue<? extends ObservableList<CandleChartData>> c, final ObservableList<CandleChartData> old, final ObservableList<CandleChartData> newv) {
                    if (old != null) {
                        old.removeListener(lis);
                    }
                    if (newv != null) {
                        newv.addListener(lis);
                    }
                    requestLayout();
                }
            });
        }
        return dataListProperty;
    }

    public final ObservableList<CandleChartData> getDataList() {
        return dataListProperty == null ? null : dataListProperty.get();
    }

    public final void setDataList(final ObservableList<CandleChartData> value) {
        dataListProperty().set(value);
    }

    private ObjectProperty<ObservableList<CandleChartData>> dataListProperty;

}
