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

import java.math.BigInteger;
import java.util.List;

import kiss.model.Property;
import trademate.setting.SettingStyles;
import viewtify.ui.UI;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;
import viewtify.ui.UIPane;
import viewtify.ui.UIRange;
import viewtify.ui.UIText;
import viewtify.ui.UserInterface;
import viewtify.ui.View;
import viewtify.util.Range;

class PropertyEditor extends View {

    /** The associated trader builder. */
    private final ParameterizedTraderBuilder builder;

    /** The target property. */
    private final Property property;

    /** The initial value of the property. */
    private final Object initialValue;

    /** The title area. */
    private UILabel title;

    /** The input area. */
    private UIPane input;

    /**
     * 
     */
    PropertyEditor(ParameterizedTraderBuilder builder, Property property, Object initialValue) {
        this.builder = builder;
        this.property = property;
        this.initialValue = initialValue;
    }

    /**
     * UI definition.
     */
    class view extends UI implements SettingStyles {
        {
            $(hbox, FormRow, () -> {
                $(title, FormLabel);
                $(input, FormInput);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        title.text(property.name);
        input.set(makeInputUI(property.model.type));
    }

    /**
     * Create input area.
     * 
     * @param type
     * @return
     */
    private UserInterface makeInputUI(Class type) {
        if (type == boolean.class || type == Boolean.class) {
            return make(UICheckBox.class).value((boolean) initialValue);
        } else if (isIntegral(type)) {
            return ((UIRange<Integer>) make(UIRange.class))
                    .value(new Range<>((Integer) initialValue, (Integer) initialValue, v -> v + 1, v -> v - 1));
        } else if (type.isEnum()) {
            return ((UIComboBox<Object>) make(UIComboBox.class)).items(type.getEnumConstants()).value(initialValue);
        } else {
            return make(UIText.class);
        }
    }

    /**
     * Test whether property type is integral number.
     * 
     * @param type
     * @return
     */
    private boolean isIntegral(Class type) {
        return type == int.class || type == Integer.class || type == long.class || type == Long.class || type == BigInteger.class;
    }

    /**
     * List up all values.
     * 
     * @return
     */
    List<Object> values() {
        return null;
    }
}
