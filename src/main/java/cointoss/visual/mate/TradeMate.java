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

import java.lang.reflect.Field;
import java.nio.file.StandardWatchEventKinds;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import filer.Filer;
import kiss.Disposable;
import kiss.I;

/**
 * @version 2017/11/13 16:58:58
 */
public class TradeMate extends Application {

    static {
        I.load(View.class, false);
    }

    /** The root controller. */
    private static TradeMateController controller;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        FXMLLoader loader = new FXMLLoader(system.getResource("TradeMate.fxml"));
        VBox root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(getClass().getSimpleName());
        stage.show();

        // setup views
        for (View view : I.find(View.class)) {
            // inject FXML
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
                        field.set(view, node);
                    }
                }
            }

            // initialize view
            view.initialize();
        }

        Filer.observe(Filer.locate("src/main/resources/TradeMate.css")).to(e -> {
            if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                ObservableList<String> list = scene.getStylesheets();
                list.clear();
                list.add(system.getResource("TradeMate.css").toExternalForm());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        for (Disposable disposable : View.terminators) {
            disposable.dispose();
        }
    }

    /**
     * Access to controller.
     * 
     * @return
     */
    public static TradeMateController cotroller() {
        return controller;
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
