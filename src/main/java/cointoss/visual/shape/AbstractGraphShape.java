/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.shape;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ObservableList;

/**
 * @version 2017/09/26 1:06:29
 */
public abstract class AbstractGraphShape implements GraphShape {

    /**
     * Get styler.
     * 
     * @return
     */
    public final ObservableList<String> getStyleClass() {
        return getNode().getStyleClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ReadOnlyBooleanProperty validateProperty() {
        return validateWrapper().getReadOnlyProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isValidate() {
        return validateWrapper == null ? false : validateWrapper.get();
    }

    protected final void setValidate(final boolean value) {
        validateWrapper().set(value);
    }

    /**
     * @return
     */
    protected final ReadOnlyBooleanWrapper validateWrapper() {
        if (validateWrapper == null) {
            validateWrapper = new ReadOnlyBooleanWrapper(this, "validate", false);
        }
        return validateWrapper;
    }

    private ReadOnlyBooleanWrapper validateWrapper;

    /**
     * @return
     */
    protected final InvalidationListener getInvalidateListener() {
        if (invalidateSetter == null) {
            invalidateSetter = o -> {
                if (isValidate()) {
                    setValidate(false);
                }
            };

        }
        return invalidateSetter;
    }

    private InvalidationListener invalidateSetter = null;

}
