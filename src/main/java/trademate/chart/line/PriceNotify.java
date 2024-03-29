/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart.line;

import javafx.scene.input.MouseEvent;

import cointoss.MarketService;
import hypatia.Num;
import kiss.I;
import kiss.Variable;
import trademate.chart.Axis.TickLable;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartStyles;
import trademate.setting.Notificator;
import viewtify.Viewtify;

public class PriceNotify extends LineMark {

    /** The reusable translated text. */
    private static final Variable<String> ALERT = I.translate("Alert Price");

    public PriceNotify(ChartCanvas canvas) {
        super(canvas, canvas.axisY, ChartStyles.PriceSignal);
    }

    /**
     * Notify by price.
     * 
     * @param e
     */
    public void notifyByPrice(MouseEvent e) {
        double position = e.getY();

        findLabelByPosition(position).to(this::removeLabel, () -> {
            Num price = Num.of(canvas.axisY.getValueForPosition(position)).scale(canvas.chart.market.v.service.setting.base.scale);
            TickLable label = createLabel(price, ALERT.v);

            label.add(canvas.chart.market.v.signalByPrice(price).on(Viewtify.UIThread).to(exe -> {
                removeLabel(label);

                MarketService service = canvas.chart.market.v.service;
                Num p = exe.price.scale(service.setting.target.scale);
                String title = "🔊  " + service.id + " " + p;
                I.make(Notificator.class).priceSignal.notify(title, I.translate("The specified price ({0}) has been reached.", p));
            }));
        });
    }
}
