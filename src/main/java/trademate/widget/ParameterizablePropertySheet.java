/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.widget;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.common.collect.Sets;

import kiss.I;
import kiss.Ⅱ;
import kiss.model.Model;
import kiss.model.Property;
import viewtify.Viewtify;
import viewtify.style.FormStyles;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboCheckBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;
import viewtify.ui.UITextValue;
import viewtify.ui.UserInterface;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.Actions;
import viewtify.ui.helper.User;

public class ParameterizablePropertySheet<M> extends View {

    /** The base instance. */
    public final ObjectProperty<M> base = new SimpleObjectProperty();

    /** The base model. */
    public final ReadOnlyObjectProperty<Model<M>> model = Viewtify.observe(base)
            .map(o -> Model.of(o))
            .to(SimpleObjectProperty.class, ObjectProperty::set);

    /** The property filter. */
    public final ObjectProperty<Predicate<Property>> acceptableProperty = new SimpleObjectProperty(I.accept());

    /** The property filter. */
    public final ObjectProperty<Predicate<Property>> rejectableProperty = new SimpleObjectProperty(I.reject());

    /** The value holder. */
    private final Map<Property, List<Object>> properties = new HashMap();

    /** Property editing UIs */
    private final ObservableList<PropertyEditorView> editors = FXCollections.observableArrayList();

    /**
     * 
     */
    public ParameterizablePropertySheet() {
    }

    /**
     * @param base
     */
    public ParameterizablePropertySheet(M base) {
        this.base.set(base);
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {
            $(vbox, () -> {
                $(editors);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Viewtify.observing(model)
                .effect(editors::clear)
                .flatIterable(Model<M>::properties)
                .take(acceptableProperty.get())
                .skip(rejectableProperty.get())
                .to(property -> {
                    editors.add(new PropertyEditorView(property, model.get().get(base.get(), property)));
                });
    }

    public List<M> build() {
        List<Set<Ⅱ<Property, Object>>> combinations = new ArrayList();

        for (Entry<Property, List<Object>> entry : properties.entrySet()) {
            combinations.add(I.signal(entry.getValue()).map(v -> I.pair(entry.getKey(), v)).toCollection(new LinkedHashSet<>()));
        }

        List<M> instances = new ArrayList();

        for (List<Ⅱ<Property, Object>> combined : Sets.cartesianProduct(combinations)) {
            M instance = I.make(model.get().type);

            for (Ⅱ<Property, Object> value : combined) {
                model.get().set(instance, value.ⅰ, value.ⅱ);
            }
            instances.add(instance);
        }
        return instances;
    }

    /**
     * 
     */
    class PropertyEditorView extends View {

        /** The target property. */
        private final Property property;

        /** The initial value of the property. */
        private final Object initialValue;

        /**
         * 
         */
        PropertyEditorView(Property property, Object initialValue) {
            this.property = property;
            this.initialValue = initialValue;
        }

        /**
         * UI definition.
         */
        class view extends ViewDSL {
            {
                form(property.name, FormStyles.FormInputMin, createUI(property));
            }
        }

        private UserInterface[] createUI(Property property) {
            Class type = property.model.type;

            if (type == boolean.class || type == Boolean.class) {
                return new UserInterface[] {createCheckBox((boolean) initialValue)};
            } else if (type.isEnum()) {
                return new UserInterface[] {createComboBox(initialValue)};
            } else if (isIntegral(property.model.type)) {
                return createIntegralRange((int) initialValue);
            } else if (isDecimal(property.model.type)) {
                return createDecimalRange((double) initialValue);
            } else {
                return new UserInterface[] {make(UIText.class)};
            }
        }

        /**
         * Build UI for boolean.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private UICheckBox createCheckBox(boolean initial) {
            return make(UICheckBox.class).value(initial).when(User.Action, (e, check) -> properties.put(property, List.of(check.value())));
        }

        /**
         * Build UI for enum.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private <E> UIComboCheckBox<E> createComboBox(E initial) {
            UIComboCheckBox<E> created = make(UIComboCheckBox.class);
            created.selectedItems().addListener((InvalidationListener) e -> {
                properties.put(property, (List<Object>) created.selectedItems());
            });

            return created.items((E[]) initial.getClass().getEnumConstants()).select(initial).title(property.model.type.getSimpleName());
        }

        /**
         * Build UI for integer.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private UserInterface[] createIntegralRange(int initial) {
            UISpinner<Integer> step = make(UISpinner.class);
            step.items(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000, 10000);

            UITextValue<Integer> start = make(UITextValue.class);
            UITextValue<Integer> end = make(UITextValue.class);

            start.value(initial).when(User.Scroll, Actions.traverseInt(step::value)).observe((prev, now) -> {
                end.value(v -> v + (now - prev));
            }).requireWhen(end).require(ui -> ui.value() <= end.value());
            end.value(initial)
                    .when(User.Scroll, Actions.traverseInt(step::value))
                    .require(ui -> start.value() <= ui.value())
                    .requireWhen(start);

            start.observe().merge(end.observe(), step.observe()).to(() -> {
                List values = new ArrayList();
                for (int i = start.value(); i <= end.value(); i += step.value()) {
                    values.add(i);
                }
                properties.put(property, values);
            });

            return new UserInterface[] {start, end, step};
        }

        /**
         * Build UI for decimal.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private UserInterface[] createDecimalRange(double initial) {
            UISpinner<Double> step = make(UISpinner.class);
            step.items(0.001, 0.002, 0.003, 0.004, 0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0);

            UITextValue<Double> start = make(UITextValue.class);
            UITextValue<Double> end = make(UITextValue.class);

            start.value(initial).when(User.Scroll, Actions.traverseDouble(step::value)).observe((prev, now) -> {
                end.value(v -> BigDecimal.valueOf(v).add(BigDecimal.valueOf(now).subtract(BigDecimal.valueOf(prev))).doubleValue());
            }).requireWhen(end).require(ui -> ui.value() <= end.value());
            end.value(initial)
                    .when(User.Scroll, Actions.traverseDouble(step::value))
                    .require(ui -> start.value() <= ui.value())
                    .requireWhen(start);

            start.observe().merge(end.observe(), step.observe()).to(() -> {
                List values = new ArrayList();
                for (double i = start.value(); i <= end.value(); i += step.value()) {
                    values.add(i);
                }
                properties.put(property, values);
            });

            return new UserInterface[] {start, end, step};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
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
         * Test whether property type is integral number.
         * 
         * @param type
         * @return
         */
        private boolean isDecimal(Class type) {
            return type == float.class || type == Float.class || type == double.class || type == Double.class || type == BigDecimal.class;
        }
    }

}