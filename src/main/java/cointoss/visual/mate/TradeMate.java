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

import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import cointoss.Execution;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import filer.Filer;
import kiss.I;
import viewtify.Viewtify;

/**
 * @version 2017/11/13 16:58:58
 */
public class TradeMate extends Application {

    static {
        I.load(Num.Codec.class, false);
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
        Viewtify.initialize(scene);

        Filer.observe(Filer.locate("src/main/resources/TradeMate.css")).to(e -> {
            if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                ObservableList<String> list = scene.getStylesheets();
                list.clear();
                list.add(system.getResource("TradeMate.css").toExternalForm());
            }
        });

        // load execution log
        Viewtify.inWorker(() -> {
            return BitFlyer.FX_BTC_JPY.log().fromToday().throttle(100, TimeUnit.MILLISECONDS).on(Viewtify.UIThread).to(e -> {
                priceLatest.setText(e.price.toString());

                ObservableList<Execution> items = executionList.getItems();

                items.add(0, e);

                if (100 < items.size()) {
                    items.remove(items.size() - 1);
                }
            });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        Viewtify.terminate();
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
