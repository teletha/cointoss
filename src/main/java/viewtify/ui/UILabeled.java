/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui;

import java.util.Objects;

import javafx.scene.control.Labeled;

/**
 * @version 2017/11/15 9:54:15
 */
public abstract class UILabeled<Self extends UILabeled, W extends Labeled> extends UI<Self, W> {

    /**
     * Enchanced view.
     * 
     * @param ui
     */
    protected UILabeled(W ui) {
        super(ui);
    }

    /**
     * Get text.
     * 
     * @param text
     */
    public String text() {
        return ui.getText();
    }

    /**
     * Set text.
     * 
     * @param text
     */
    public Self text(Object text) {
        ui.setText(Objects.toString(text));
        return (Self) this;
    }
}
