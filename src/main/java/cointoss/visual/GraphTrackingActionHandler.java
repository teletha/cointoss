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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;

/**
 * クリック点におけるLineChartのデータで処理を行うEventHandlerの基本実装。
 * bindを利用可能にするためにはgetTargetPlotAreaが唯一のGraphPlotAreaを返す様に実装する必要があります。
 * 
 * @author nodamushi
 */
public abstract class GraphTrackingActionHandler extends GraphEventHandler<MouseEvent> {

    private WeakReference<double[]> arrref;

    private double[] getARR(final int dsize) {
        double[] arr;
        if (arrref != null) {
            final double[] d = arrref.get();
            if (d == null || d.length != dsize) {
                arr = new double[dsize];
                arrref = new WeakReference<>(arr);
            } else {
                arr = d;
            }
        } else {
            arr = new double[dsize];
            arrref = new WeakReference<>(arr);
        }
        return arr;
    }

    /**
     * マウスイベントを処理するかどうか
     * 
     * @param e
     * @return 処理する場合はtrue,処理しない場合はfalse
     */
    public abstract boolean filter(MouseEvent e);

    /**
     * arrの値を設定する
     * 
     * @param e
     * @param v
     * @param arr
     * @return
     */
    private final boolean _handle(final MouseEvent e, final GraphPlotArea a, final double v, final double[] arr) {
        final ObservableList<CandleChartData> dataList = a.getLineDataList();
        final int dsize = arr.length;
        if (a.axisX.get() == null) {
            return false;
        }
        for (int i = 0; i < dsize; i++) {
            final CandleChartData d = dataList.get(i);
            final int ds = d.size();
            if (ds == 0) {
                arr[i] = Double.NaN;
                continue;
            }

            final int lowindex = d.searchXIndex(v, false);
            final double lowv = d.getX(lowindex);

            if (lowv == v) {
                arr[i] = d.getY(lowindex);
                continue;
            } else if (lowv > v || ds <= lowindex + 1) {
                arr[i] = Double.NaN;
                continue;
            }

            final double upv = d.getX(lowindex + 1);
            final double k = (v - lowv) / (upv - lowv);
            arr[i] = (1 - k) * d.getY(lowindex) + k * d.getY(lowindex + 1);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handle(final MouseEvent e) {
        if (!(e.getSource() instanceof GraphPlotArea) || !filter(e)) {
            return;
        }
        final GraphPlotArea a = (GraphPlotArea) e.getSource();
        final ObservableList<CandleChartData> dataList = a.getLineDataList();
        final int dsize = dataList == null ? 0 : dataList.size();
        final double[] arr = getARR(dsize);

        final double v;
        if (a.axisX == null) {
            return;
        }
        v = a.axisX.get().getValueForPosition(e.getX());

        _handle(e, a, v, arr);
        final boolean b = handle(e, a, v, arr);
        if (binds != null) {
            for (final A aa : binds) {
                if (aa.call) {
                    aa.a.bindHandle(e, v);
                }
            }
        }

        if (b && !e.isConsumed()) {
            e.consume();
        }

    }

    private void bindHandle(final MouseEvent e, final double v) {
        final GraphPlotArea a = getTargetPlotArea();
        if (a != null) {
            final ObservableList<CandleChartData> dataList = a.getLineDataList();
            final int dsize = dataList == null ? 0 : dataList.size();
            final double[] arr = getARR(dsize);
            if (!_handle(e, a, v, arr)) {
                return;
            }
            handle(e, a, v, arr);
        }
    }

    protected GraphPlotArea getTargetPlotArea() {
        return null;
    }

    /**
     * 実際の処理をします。
     * 
     * @param e マウスイベント
     * @param area 処理対象のGraphPlotArea
     * @param v eのクリックポイントをグラフ系における座標に変換した値。
     *            GraphPlotArea.getOrientationがHORIZONTALの時、この値はxを意味し、そうでないときはyを意味する
     * @param values vにおけるLineChartDataの値の配列。GraphPlotArea.getDataList()で得られるLineChartData
     *            の並びと対応する。vにおいて値を持たない場合はNaNが入っている。
     *            GraphPlotArea.getOrientationがHORIZONTALの時、この値はyを意味し、そうでないときはxを意味する
     * @return trueの時、eventの消費を行います。
     */
    protected abstract boolean handle(MouseEvent e, GraphPlotArea area, double v, double[] values);

    private static class A {
        GraphTrackingActionHandler a;

        boolean call;

        @Override
        public boolean equals(final Object obj) {
            return a == obj;
        }
    }

    private List<A> binds;

    /**
     * actionのhandleメソッドが呼ばれたときに、 このインスタンスのhandle(MouseEvent,GraphPlotArea,double,double[])メソッドも
     * 呼び出すようにします。
     * 
     * @param action
     */
    protected void bind(final GraphTrackingActionHandler action) {
        if (action == null) {
            return;
        }
        if (contains(action)) {
            final A a = get(action);
            a.call = false;
            action._bind(this);
            return;
        }
        add(action, false);
        action._bind(this);
    }

    private void _bind(final GraphTrackingActionHandler action) {
        if (contains(action)) {
            final A a = get(action);
            a.call = true;
            return;
        }
        add(action, true);
    }

    /**
     * handle(MouseEvent,GraphPlotArea,double,double[])メソッドを呼び出すときに、
     * 対象のhandleメソッドも相互に同じ引数で呼び出すようになります。
     * 
     * @param action
     */
    protected void bindBidical(final GraphTrackingActionHandler action) {
        if (action == null) {
            return;
        }
        if (contains(action)) {
            final A a = get(action);
            a.call = true;
            action._bindBidical(this);
            return;
        }

        add(action, true);
        action._bindBidical(this);
    }

    private void _bindBidical(final GraphTrackingActionHandler a) {
        if (contains(a)) {
            final A aa = get(a);
            aa.call = true;
            return;
        }
        add(a, true);
    }

    private A get(final GraphTrackingActionHandler a) {
        if (binds == null) {
            return null;
        }
        final int i = binds.indexOf(a);
        return i == -1 ? null : binds.get(i);
    }

    private boolean contains(final GraphTrackingActionHandler a) {
        if (binds == null) {
            return false;
        }
        return binds.contains(a);
    }

    private void add(final GraphTrackingActionHandler a, final boolean b) {
        if (binds == null) {
            binds = new ArrayList<>(2);
        }
        if (!binds.contains(a)) {
            final A aa = new A();
            aa.a = a;
            aa.call = b;
            binds.add(aa);
        }
    }

    private A remove(final GraphTrackingActionHandler a) {
        if (binds == null) {
            return null;
        }
        final int index = binds.indexOf(a);
        if (index == -1) {
            return null;
        }
        return binds.remove(index);
    }

    protected void unbind() {
        if (binds != null) {
            for (final A a : binds) {
                a.a.remove(this);
            }
            binds.clear();
        }
    }

    protected void unbind(final GraphTrackingActionHandler action) {
        final A a = remove(action);
        if (a != null) {
            a.a.remove(this);
        }
    }
}
