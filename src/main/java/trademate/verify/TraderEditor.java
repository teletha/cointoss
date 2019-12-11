/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.verify;

import cointoss.Trader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kiss.model.Property;
import viewtify.ui.UI;
import viewtify.ui.View;

/**
 * UI for editing {@link Trader} properties.
 */
class TraderEditor extends View {

    /** The target {@link Trader}. */
    private final ParameterizedTraderBuilder builder;

    /** The property editor view. */
    final ObservableList<PropertyEditor> editors = FXCollections.observableArrayList();

    /**
     * Create UI for {@link Trader}.
     * 
     * @param trader
     */
    TraderEditor(ParameterizedTraderBuilder builder) {
        this.builder = builder;
    }

    /**
     * UI definition.
     */
    class view extends UI {
        {
            $(vbox, editors);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        for (Property p : BackTestView.editableProperties(builder.model)) {
            editors.add(new PropertyEditor(builder, p, builder.model.get(builder.trader, p)));
        }
    }
}