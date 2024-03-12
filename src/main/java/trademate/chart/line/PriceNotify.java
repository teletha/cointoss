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
import cointoss.util.arithmetic.Num;
import kiss.I;
import trademate.chart.Axis.TickLable;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartStyles;
import trademate.setting.Notificator;
import viewtify.Viewtify;

public class PriceNotify extends LineMark {

    public PriceNotify(ChartCanvas canvas) {
        super(canvas, canvas.axisY, ChartStyles.PriceSignal);
    }

    /**
     * Notify by price.
     * 
     * @param e
     */
    public void notifyByPrice(MouseEvent e) {
        double clickedPosition = e.getY();

        // check price range to add or remove
        for (TickLable mark : labels) {
            double markedPosition = canvas.axisY.getPositionForValue(mark.value.get());

            if (Math.abs(markedPosition - clickedPosition) < 5) {
                remove(mark);
                return;
            }
        }

        Num price = Num.of(canvas.axisY.getValueForPosition(clickedPosition)).scale(canvas.chart.market.v.service.setting.base.scale);
        TickLable label = createLabel(price);

        label.add(canvas.chart.market.v.signalByPrice(price).on(Viewtify.UIThread).to(exe -> {
            remove(label);

            MarketService service = canvas.chart.market.v.service;
            Num p = exe.price.scale(service.setting.target.scale);
            String title = "🔊  " + service.id + " " + p;
            I.make(Notificator.class).priceSignal.notify(title, I.translate("The specified price ({0}) has been reached.", p));
        }));
    }
}
