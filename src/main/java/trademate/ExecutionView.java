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

import java.util.stream.IntStream;

import cointoss.execution.Execution;
import cointoss.util.Chrono;
import cointoss.util.Primitives;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class ExecutionView extends View {

    /** The execution list. */
    private UIListView<Execution> executionList;

    /** The execution list. */
    private UIListView<Execution> executionCumulativeList;

    /** UI for interval configuration. */
    private UISpinner<Integer> takerSize;

    /** Parent View */
    private TradingView tradingView;

    class view extends ViewDSL {
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
        int scale = tradingView.market.service.setting.targetCurrencyScaleSize;

        // configure UI
        takerSize.initialize(IntStream.range(1, 51).boxed());
        executionList.render((label, e) -> update(label, e, false, scale));
        executionCumulativeList.render((label, e) -> update(label, e, true, scale)).take(takerSize, (e, size) -> size <= e.accumulative);

        // load execution log
        tradingView.service
                .add(tradingView.market.timeline.take(tradingView.chart.showRealtimeUpdate.observing()).on(Viewtify.UIThread).to(e -> {
                    executionList.addItemAtFirst(e);

                    if (100 < executionList.size()) {
                        executionList.removeItemAtLast();
                    }
                }));

        // load big taker log
        tradingView.service.add(tradingView.market.timelineByTaker.take(tradingView.chart.showRealtimeUpdate.observing())
                .on(Viewtify.UIThread)
                .to(e -> {
                    if (1 <= e.accumulative) {
                        executionCumulativeList.addItemAtFirst(e);

                        if (1000 < executionCumulativeList.size()) {
                            executionCumulativeList.removeItemAtLast();
                        }
                    }
                }));
    }

    private void update(UILabel label, Execution e, boolean accum, int scale) {
        String text = Chrono.system(e.date).format(Chrono.Time) + "  " + e.price + " " + (accum
                ? Primitives.roundDecimal(e.accumulative, scale)
                : e.size.scale(scale)) + "  " + e.delay;

        label.text(text).styleOnly(TradeMateStyle.Side.of(e.direction));
    }
}
