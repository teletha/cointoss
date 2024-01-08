/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.verify;

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
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import cointoss.trade.Trader;
import kiss.I;
import kiss.Model;
import kiss.Property;
import kiss.Ⅱ;
import viewtify.style.FormStyles;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboCheckBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;
import viewtify.ui.UserInterface;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.Actions;
import viewtify.ui.helper.User;

public class BotEditor extends View {

    /** The base instance. */
    public final Trader trader;

    /** The trader model. */
    private final Model<Trader> model;

    /** The property filter. */
    public final ObjectProperty<Predicate<Property>> acceptableProperty = new SimpleObjectProperty<>(I::accept);

    /** The property filter. */
    public final ObjectProperty<Predicate<Property>> rejectableProperty = new SimpleObjectProperty<>(I::reject);

    /** The value holder. */
    private final Map<Property, List<Object>> properties = new HashMap();

    /** Property editing UIs */
    private final ObservableList<PropertyEditor> editors = FXCollections.observableArrayList();

    /**
     * @param trader
     */
    public BotEditor(Trader trader) {
        this.trader = trader;
        this.model = Model.of(trader);
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {
            $(vbox, editors);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        I.signal(model)
                .effect(editors::clear)
                .flatIterable(Model::properties)
                .take(acceptableProperty.get())
                .skip(rejectableProperty.get())
                .skip(p -> p.name.equals("scenarios"))
                .to(property -> {
                    BotEditor.PropertyEditor form = new PropertyEditor(property, model.get(trader, property));

                    if (property.name.equals("enable")) {
                        editors.add(0, form);
                    } else {
                        editors.add(form);
                    }
                });
    }

    /**
     * Create all combination of parameterized traders.
     * 
     * @return
     */
    public List<Trader> build() {
        List<Set<Ⅱ<Property, Object>>> combinations = new ArrayList();

        for (Entry<Property, List<Object>> entry : properties.entrySet()) {
            combinations.add(I.signal(entry.getValue()).map(v -> I.pair(entry.getKey(), v)).toCollection(new LinkedHashSet<>()));
        }

        List<Trader> instances = new ArrayList();

        for (List<Ⅱ<Property, Object>> combined : Sets.cartesianProduct(combinations)) {
            Trader trader = I.make(model.type);
            trader.disable();

            for (Ⅱ<Property, Object> value : combined) {
                model.set(trader, value.ⅰ, value.ⅱ);
            }
            instances.add(trader);
        }
        return instances;
    }

    /**
     * 
     */
    class PropertyEditor extends View {

        /** The target property. */
        private final Property property;

        /** The initial value of the property. */
        private final Object initialValue;

        /**
         * 
         */
        PropertyEditor(Property property, Object initialValue) {
            this.property = property;
            this.initialValue = initialValue;
        }

        /**
         * UI definition.
         */
        class view extends ViewDSL {
            {
                form(StringUtils.capitalize(property.name), FormStyles.InputMin, createUI(property));
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
                return new UserInterface[] {new UIText(this, String.class)};
            }
        }

        /**
         * Build UI for boolean.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private UICheckBox createCheckBox(boolean initial) {
            UICheckBox box = new UICheckBox(this).value(initial);

            box.observing().to(v -> properties.put(property, List.of(v)));

            return box;
        }

        /**
         * Build UI for enum.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private <E> UIComboCheckBox<E> createComboBox(E initial) {
            UIComboCheckBox<E> created = new UIComboCheckBox(this);
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
            UISpinner<Integer> step = new UISpinner(this);
            step.items(1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000);

            UIText<Integer> start = new UIText(this, Integer.class);
            UIText<Integer> end = new UIText(this, Integer.class);

            start.value(initial)
                    .acceptIntegralInput()
                    .when(User.Scroll, Actions.traverseInt(step::value))
                    .observe((prev, now) -> end.value(v -> v + (now - prev)))
                    .verify(en("The start value must be less than the end value."), ui -> ui.value() <= end.value())
                    .verifyWhen(end.isChanged());
            end.value(initial)
                    .acceptIntegralInput()
                    .when(User.Scroll, Actions.traverseInt(step::value))
                    .verify(en("The end value must be greater than the start value."), ui -> start.value() <= ui.value())
                    .verifyWhen(start.isChanged());

            int length = start.value().toString().length();
            step.value(2 <= length && length <= 5 ? step.items().get((length - 1) * 3) : 1);

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
            UISpinner<Double> step = new UISpinner(this);
            step.items(0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0);

            UIText<Double> start = new UIText(this, Double.class);
            UIText<Double> end = new UIText(this, Double.class);

            start.value(initial)
                    .acceptDecimalInput()
                    .when(User.Scroll, Actions.traverseDouble(step::value))
                    .observe((prev, now) -> end.value(v -> BigDecimal.valueOf(v)
                            .add(BigDecimal.valueOf(now).subtract(BigDecimal.valueOf(prev)))
                            .doubleValue()))
                    .verify(en("The start value must be less than the end value."), ui -> ui.value() <= end.value())
                    .verifyWhen(end.isChanged());
            end.value(initial)
                    .acceptDecimalInput()
                    .when(User.Scroll, Actions.traverseDouble(step::value))
                    .verify(en("The end value must be greater than the start value."), ui -> start.value() <= ui.value())
                    .verifyWhen(start.isChanged());

            int length = start.value().toString().replace(".", "").length();
            step.value(1 <= length && length <= 4 ? step.items().get((4 - length) * 3) : 0.001);

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