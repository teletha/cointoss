/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import cointoss.Execution;
import cointoss.util.Num;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UISpinner;
import viewtify.ui.UserInterface;

/**
 * @version 2017/12/01 16:58:49
 */
public class ExecutionView extends View {

    /** The time format. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** The execution list. */
    private @UI ListView<Execution> executionList;

    /** The execution list. */
    private @UI ListView executionCumulativeList;

    /** UI for interval configuration. */
    private @UI UISpinner<Integer> takerSize;

    /** Parent View */
    private @UI TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        ObservableList<Execution> commulatives = executionCumulativeList.getItems();
        FilteredList<Execution> filtered = new FilteredList<>(commulatives);

        // configure UI
        executionList.setCellFactory(v -> new Cell());
        executionCumulativeList.setItems(filtered);
        executionCumulativeList.setCellFactory(v -> new Cell());
        takerSize.values(IntStream.range(1, 51).boxed())
                .initial(10)
                .observe(size -> filtered.setPredicate(e -> e.cumulativeSize.isGreaterThanOrEqual(size)));

        // load execution log
        Viewtify.inWorker(() -> {
            return view.market().timeline.on(Viewtify.UIThread).to(e -> {
                ObservableList<Execution> items = executionList.getItems();
                items.add(0, e);

                if (100 < items.size()) {
                    items.remove(items.size() - 1);
                }
            });
        });

        // load big taker log
        Viewtify.inWorker(() -> {
            return view.market().timelineByTaker.on(Viewtify.UIThread).to(e -> {
                if (e.cumulativeSize.isGreaterThanOrEqual(Num.ONE)) {
                    commulatives.add(0, e);

                    if (2000 < commulatives.size()) {
                        commulatives.remove(commulatives.size() - 1);
                    }
                }
            });
        });
    }

    /**
     * @version 2017/11/13 21:35:32
     */
    private class Cell extends ListCell<Execution> {

        /** The enhanced ui. */
        private final UserInterface ui = Viewtify.wrap(this, ExecutionView.this);

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
                setText(formatter.format(e.exec_date.plusHours(9)) + "  " + e.price + "円  " + e.cumulativeSize.scale(6));
                ui.styleOnly(e.side);
            }
        }
    }
}
