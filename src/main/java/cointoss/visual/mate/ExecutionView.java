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

import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import cointoss.Execution;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UISpinner;

/**
 * @version 2017/12/01 16:58:49
 */
public class ExecutionView extends View {

    /** The time format. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** The execution list. */
    private @FXML ListView<Execution> executionList;

    /** The execution list. */
    private @FXML ListView executionCumulativeList;

    /** UI for interval configuration. */
    private @FXML UISpinner<Integer> takerSize;

    /** Parent View */
    private @FXML TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // configure UI
        executionList.setCellFactory(v -> new Cell());
        executionCumulativeList.setCellFactory(v -> new Cell());
        takerSize.values(IntStream.range(1, 51).boxed()).initial(10);

        // load execution log
        Viewtify.inWorker(() -> {
            return view.market().timeline.on(Viewtify.UIThread).to(e -> {
                ObservableList<Execution> items = executionList.getItems();
                items.add(0, e);

                if (100 < items.size()) {
                    items.remove(items.size() - 1);
                }

                if (e.cumulativeSize.isGreaterThanOrEqual(takerSize.ui.getValue())) {
                    ObservableList<Execution> bigs = executionCumulativeList.getItems();

                    bigs.add(0, e);

                    if (1000 < bigs.size()) {
                        bigs.remove(bigs.size() - 1);
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
        private final UI ui = Viewtify.wrap(this, ExecutionView.this);

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
                ui.style(e.side);
            }
        }
    }
}
