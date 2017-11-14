/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import javafx.scene.paint.Paint;

import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2017/11/14 22:16:38
 */
@Manageable(lifestyle = Singleton.class)
public class Theme implements Extensible {

    /** Theme color. */
    private final Paint buy = Paint.valueOf("rgb(251, 189, 42)");

    /** Theme color. */
    private final Paint sell = Paint.valueOf("rgb(247, 105, 77)");

    /**
     * @return
     */
    public Paint buy() {
        return buy;
    }

    /**
     * @return
     */
    public Paint sell() {
        return sell;
    }

    /**
     * Select the current theme.
     */
    public static Theme now() {
        return I.make(Theme.class);
    }
}
