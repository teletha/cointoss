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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

import com.google.common.collect.Sets;

import kiss.I;
import kiss.Ⅱ;
import kiss.model.Model;
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
    private final Map<Property, List<Object>> propertyValues = new HashMap();

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

        for (Entry<Property, List<Object>> entry : propertyValues.entrySet()) {
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

        /** The title area. */
        private UILabel title;

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
            return make(UICheckBox.class).value(initial)
                    .when(User.Action, (e, check) -> propertyValues.put(property, List.of(check.value())));
        }

        /**
         * Build UI for enum.
         * 
         * @param initial The initial value.
         * @return A create UI.
         */
        private <E> UIComboBox<E> createComboBox(E initial) {
            UIComboBox<E> created = make(UIComboBox.class);

            return created.items((E[]) initial.getClass().getEnumConstants())
                    .value(initial)
                    .when(User.Action, () -> propertyValues.put(property, List.of(created.value())));
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
            start.value(initial).style(SettingStyles.FormInputMin).when(User.Scroll, Actions.traverseInt(step::value));

            UITextValue<Integer> end = make(UITextValue.class);
            end.value(initial).style(SettingStyles.FormInputMin).when(User.Scroll, Actions.traverseInt(step::value));

            Viewtify.observe(start.valueProperty())
                    .merge(Viewtify.observe(end.valueProperty()), Viewtify.observe(step.valueProperty()))
                    .to(() -> {
                        propertyValues.put(property, IntStream.iterate(start.value(), v -> v <= end.value(), v -> v + step.value())
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
    }

}