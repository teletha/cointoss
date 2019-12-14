/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.function.BiFunction;
import java.util.stream.IntStream;

import cointoss.execution.Execution;
import cointoss.util.Chrono;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.View;

public class ExecutionView extends View {

    /** The execution list. */
    private UIListView<Execution> executionList;

    /** The execution list. */
    private UIListView<Execution> executionCumulativeList;

    /** UI for interval configuration. */
    private UISpinner<Integer> takerSize;

    /** Parent View */
    private TradingView view;

    class view extends UI {
        {
            $(vbox, style.root, () -> {
                $(executionList);
                $(hbox, () -> {
                    $(takerSize, style.takerSize);
                });
                $(executionCumulativeList);
            });
        }
    }

    interface style extends StyleDSL {
        Style root = () -> {
            display.minWidth(170, px).maxWidth(170, px);

            $.descendant(() -> {
                text.unselectable();
            });
        };

        Style takerSize = () -> {
            display.width(70, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        int targetScale = view.market.service.setting.targetCurrencyScaleSize;

        // configure UI
        takerSize.initialize(IntStream.range(1, 51).boxed());
        executionList.renderByUI(() -> make(UILabel.class), execution(targetScale, false));
        executionCumulativeList.renderByUI(() -> make(UILabel.class), execution(targetScale, true))
                .take(takerSize, (e, size) -> size <= e.accumulative);

        // load execution log
        Viewtify.inWorker(() -> {
            return view.market.timeline.skipWhile(view.initializing).on(Viewtify.UIThread).to(e -> {
                executionList.addItemAtFirst(e);

                if (100 < executionList.size()) {
                    executionList.removeItemAtLast();
                }
            });
        });

        // load big taker log
        Viewtify.inWorker(() -> {
            return view.market.timelineByTaker.skipWhile(view.initializing).on(Viewtify.UIThread).to(e -> {
                if (1 <= e.accumulative) {
                    executionCumulativeList.addItemAtFirst(e);

                    if (1000 < executionCumulativeList.size()) {
                        executionCumulativeList.removeItemAtLast();
                    }
                }
            });
        });
    }

    /**
     * Rendering execution.
     * 
     * @param scale
     * @param accumulative
     * @return
     */
    private BiFunction<UILabel, Execution, UILabel> execution(int scale, boolean accumulative) {
        return (ui, e) -> {
            String text = Chrono.system(e.date).format(Chrono.Time) + "  " + e.price + " " + (accumulative ? scale(e.accumulative, scale)
                    : e.size.scale(scale)) + "  " + e.delay;

            return ui.text(text).styleOnly(TradeMateStyle.Side.of(e.direction));
        };
    }

    private double scale(double value, int scale) {
        double scaler = Math.pow(10, scale);
        return Math.round(value * scaler) / scaler;
    }
}
