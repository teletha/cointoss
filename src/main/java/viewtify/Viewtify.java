/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;

import org.controlsfx.tools.ValueExtractor;

import kiss.Disposable;
import kiss.I;
import kiss.model.Model;

/**
 * @version 2017/11/15 9:52:40
 */
public class Viewtify {

    static {
        I.load(View.class, false);

        ValueExtractor.addObservableValueExtractor(c -> c instanceof Spinner, c -> ((Spinner) c).valueProperty());
    }

    /**
     * Initialize {@link Viewtify}.
     */
    public static void initialize(Scene scene) throws Exception {
        for (View view : I.find(View.class)) {
            // inject FXML defined components
            for (Field field : view.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(FXML.class)) {
                    String id = "#" + field.getName();
                    Node node = scene.lookup(id);

                    if (node == null) {
                        // If this exception will be thrown, it is bug of this program. So we must
                        // rethrow the wrapped error in here.
                        throw new Error("Node [" + id + "] is not found.");
                    } else {
                        field.setAccessible(true);

                        Class<?> type = field.getType();

                        if (type.getName().startsWith("viewtify.ui.")) {
                            // viewtify ui
                            Constructor constructor = Model.collectConstructors(type)[0];
                            constructor.setAccessible(true);

                            field.set(view, constructor.newInstance(node));
                        } else {
                            // javafx ui
                            field.set(view, node);

                            enhanceNode(node);
                        }
                    }
                }
            }
            view.initialize();
        }
    }

    /**
     * Enhance Node.
     */
    private static void enhanceNode(Node node) {
        if (node instanceof Spinner) {
            Spinner spinner = (Spinner) node;
            spinner.setOnScroll(e -> {
                if (e.getDeltaY() > 0) {
                    spinner.increment();
                } else if (e.getDeltaY() < 0) {
                    spinner.decrement();
                }
            });
        }
    }

    /**
     * Terminate application.
     */
    public static void terminate() {
        for (Disposable disposable : View.terminators) {
            disposable.dispose();
        }
    }
}
