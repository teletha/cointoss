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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;

/**
 * @version 2017/09/26 1:05:59
 */
public interface GraphShape {
    /**
     * 表示するノード。nullを可とする。
     * 
     * @return
     */
    public Node getNode();

    /**
     * 必要な値をaxisから座標に変換し、設定する。 このメソッドが呼ばれたときにisValidateの返す値がtrueになるようにすること。
     * 
     * @param xaxis
     * @param yaxis
     * @param w 表示領域の幅
     * @param h 表示領域の高さ
     */
    public void setNodeProperty(Axis xaxis, Axis yaxis, double w, double h);

    /**
     * 値が正当かどうか。この値がfalseに変わると、GraphPlotAreaはsetNodePropertyを呼び出す
     * 
     * @return
     */
    public ReadOnlyBooleanProperty validateProperty();

    /**
     * 値が正当かどうか。この値がfalseに変わると、GraphPlotAreaはsetNodePropertyを呼び出す
     * 
     * @return
     */
    public boolean isValidate();
}
