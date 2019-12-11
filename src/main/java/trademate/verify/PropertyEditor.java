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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.scene.layout.HBox;

import kiss.model.Property;
import trademate.setting.SettingStyles;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;
import viewtify.ui.UITextValue;
import viewtify.ui.View;
import viewtify.ui.helper.User;

class PropertyEditor extends View {

    /** The associated trader builder. */
    private final ParameterizedTraderBuilder builder;

    /** The target property. */
    private final Property property;

    /** The initial value of the property. */
    private final Object initialValue;

    /** The title area. */
    private UILabel title;

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

                Class type = property.model.type;

                if (type == boolean.class || type == Boolean.class) {
                    $(createCheckBox((boolean) initialValue));
                } else if (type.isEnum()) {
                    $(createComboBox(initialValue));
                } else if (isIntegral(property.model.type)) {
                    $(createIntegralRange((int) initialValue));
                } else {
                    $(make(UIText.class), FormInput);
                }
            });
        }
    }

    /**
     * Build UI for boolean.
     * 
     * @param initial The initial value.
     * @return A create UI.
     */
    private UICheckBox createCheckBox(boolean initial) {
        UICheckBox created = make(UICheckBox.class);
        created.value(initial);

        created.when(User.Action, () -> {
            builder.propertyValues.put(property, List.of(created.value()));
        });

        return created;
    }

    /**
     * Build UI for enum.
     * 
     * @param initial The initial value.
     * @return A create UI.
     */
    private <E> UIComboBox<E> createComboBox(E initial) {
        UIComboBox<E> created = make(UIComboBox.class);
        created.items((E[]) initial.getClass().getEnumConstants());
        created.value(initial);

        created.when(User.Action, () -> {
            builder.propertyValues.put(property, List.of(created.value()));
        });

        return created;
    }

    /**
     * Build UI for integer.
     * 
     * @param initial The initial value.
     * @return A create UI.
     */
    private HBox createIntegralRange(int initial) {
        UISpinner<Integer> step = make(UISpinner.class);
        step.items(IntStream.rangeClosed(1, 100)).style(SettingStyles.FormInputMin);

        UITextValue<Integer> start = make(UITextValue.class);
        start.value(initial).style(SettingStyles.FormInputMin).when(User.Scroll, e -> {
            if (e.getDeltaY() > 0) {
                start.value(v -> v + step.value());
            } else {
                start.value(v -> v - step.value());
            }
        });

        UITextValue<Integer> end = make(UITextValue.class);
        end.value(initial).style(SettingStyles.FormInputMin).when(User.Scroll, e -> {
            if (e.getDeltaY() > 0) {
                end.value(v -> v + step.value());
            } else {
                end.value(v -> v - step.value());
            }
        });

        Viewtify.observe(start.valueProperty())
                .merge(Viewtify.observe(end.valueProperty()), Viewtify.observe(step.valueProperty()))
                .to(() -> {
                    builder.propertyValues.put(property, IntStream.iterate(start.value(), v -> v <= end.value(), v -> v + step.value())
                            .boxed()
                            .collect(Collectors.<Object> toList()));
                });

        return new HBox(start.ui, end.ui, step.ui);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        title.text(property.name);
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
