/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import java.util.List;

import cointoss.trade.TradingLog;
import viewtify.ActivationPolicy;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UILabel;
import viewtify.ui.View;

public class UIVisualizer implements Visualizer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void visualize(List<TradingLog> logs) {
        Viewtify.application().size(500, 500).use(ActivationPolicy.Latest).activate(new Viewer(logs));
    }

    /**
     * 
     */
    private static class Viewer extends View {

        private UILabel label;

        /**
         * @param logs
         */
        private Viewer(List<TradingLog> logs) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected UI declareUI() {
            return new UI() {
                {
                    $(label);
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            label.text("OK");
        }
    }
}
