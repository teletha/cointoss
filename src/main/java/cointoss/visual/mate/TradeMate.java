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

import viewtify.ActivationPolicy;
import viewtify.Viewtify;

/**
 * @version 2017/11/13 16:58:58
 */
public class TradeMate {

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Viewtify.activate(MainView.class, ActivationPolicy.Latest);
    }
}
