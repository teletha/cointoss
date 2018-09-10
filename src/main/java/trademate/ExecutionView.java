/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static trademate.TradeMateStyle.*;

import java.util.stream.IntStream;

import javafx.scene.control.ListCell;

import cointoss.Execution;
import cointoss.util.Chrono;
import cointoss.util.Num;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.UserInterface;
import viewtify.ui.View;

/**
 * @version 2018/08/30 12:53:20
 */
public class ExecutionView extends View {

    /** The execution list. */
    private UIListView<Execution> executionList;

    /** The execution list. */
    private UIListView<Execution> executionCumulativeList;

    /** UI for interval configuration. */
    private UISpinner<Integer> takerSize;

    /** Parent View */
    private TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, S.Root, () -> {
                    $(executionList);
                    $(hbox, () -> {
                        $(takerSize, S.TakerSize);
                    });
                    $(executionCumulativeList);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // configure UI
        takerSize.values(IntStream.range(1, 51).boxed()).initial(10);
        executionList.cell(v -> new Cell(false));
        executionCumulativeList.cell(v -> new Cell(true)).take(takerSize, (e, size) -> e.cumulativeSize.isGreaterThanOrEqual(size));

        // load execution log
        Viewtify.inWorker(() -> {
            return view.market().timeline.on(Viewtify.UIThread).to(e -> {
                executionList.add(0, e);

                if (100 < executionList.size()) {
                    executionList.removeLast();
                }
            });
        });

        // load big taker log
        Viewtify.inWorker(() -> {
            return view.market().timelineByTaker.on(Viewtify.UIThread).to(e -> {
                if (e.cumulativeSize.isGreaterThanOrEqual(Num.ONE)) {
                    executionCumulativeList.add(0, e);

                    if (2000 < executionCumulativeList.size()) {
                        executionCumulativeList.removeLast();
                    }
                }
            });
        });
    }

    /**
     * @version 2017/11/13 21:35:32
     */
    private class Cell extends ListCell<Execution> {

        /** The format. */
        private final boolean comulative;

        /** The enhanced ui. */
        private final UserInterface ui = Viewtify.wrap(this, ExecutionView.this);

        /**
         * @param comulative
         */
        private Cell(boolean comulative) {
            this.comulative = comulative;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(Execution e, boolean empty) {
            super.updateItem(e, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(Chrono.system(e.date).format(Chrono.Time) + "  " + e.price + "円  " + (comulative ? e.cumulativeSize : e.size)
                        .scale(6) + "  " + e.delay);
                ui.styleOnly(Side.of(e.side));
            }
        }
    }

    /**
     * @version 2018/09/07 10:49:03
     */
    private interface S extends StyleDSL {

        Style Root = () -> {
            display.minWidth(210, px);

            $.descendant(() -> {
                text.unselectable();
            });
        };

        Style TakerSize = () -> {
            display.width(70, px);
        };
    }
}
