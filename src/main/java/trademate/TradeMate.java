/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.List;
import java.util.Locale;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.MarketService;
import cointoss.market.binance.Binance;
import cointoss.market.bitfinex.Bitfinex;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitmex.BitMex;
import cointoss.util.Chrono;
import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import transcript.Lang;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UISplitPane;
import viewtify.ui.UITab;
import viewtify.ui.UITabPane;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;

@Managed(value = Singleton.class)
public class TradeMate extends View {

    /** The main split. */
    UISplitPane split;

    /** Tab Area */
    UITabPane main;

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(split, () -> {
                    $(main);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        main.policy(TabClosingPolicy.UNAVAILABLE)
                .policy(TabDragPolicy.REORDER)
                .load("Setting", SettingView.class)
                .load("Back Test", BackTestView.class);

        List<MarketService> services = List
                .of(BitFlyer.FX_BTC_JPY, BitFlyer.BTC_JPY, BitFlyer.ETH_JPY, BitFlyer.BCH_BTC, BitMex.XBT_USD, BitMex.ETH_USD, Binance.BTC_USDT, Binance.FUTURE_BTC_USDT, Bitfinex.BTC_USDT);
        for (MarketService service : services) {
            loadTabFor(service);
        }

        Chrono.seconds().map(Chrono.DateDayTime::format).on(Viewtify.UIThread).to(time -> {
            stage().v.setTitle(time);
        });
    }

    /**
     * Build tab with {@link MarketService}.
     * 
     * @param service
     */
    private void loadTabFor(MarketService service) {
        main.initial(0).load(service.marketReadableName(), tab -> {
            tab.context(c -> {
                c.menu().text(en("Arrange in tiles")).when(User.Action, () -> tileInPane(tab));
                c.menu().text(en("Detach as window")).when(User.Action, () -> detachAsWindow(tab));
            });

            return new TradingView(tab, service);
        });
    }

    /**
     * {@link TradeMate} will automatically initialize in the background if any tab has not been
     * activated yet.
     */
    public final void requestLazyInitialization() {
        for (UITab tab : main.items()) {
            if (!tab.isLoaded()) {
                Viewtify.inUI(() -> tab.load());
                return;
            }
        }
    }

    /**
     * Detach the specified tab.
     * 
     * @param tab
     */
    private void detachAsWindow(UITab tab) {
        TabPane pane = tab.getTabPane();
        int originalIndex = pane.getTabs().indexOf(tab);

        Pane content = (Pane) tab.getContent();
        tab.setContent(null);

        Scene scene = new Scene(content, content.getPrefWidth(), content.getPrefHeight());
        scene.getStylesheets().addAll(main.ui.getScene().getStylesheets());

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().addAll(((Stage) main.ui.getScene().getWindow()).getIcons());
        stage.setTitle(tab.getText());
        stage.setOnShown(e -> main.ui.getTabs().remove(tab));
        stage.setOnCloseRequest(e -> {
            stage.close();
            tab.setContent(content);
            main.ui.getTabs().add(originalIndex, tab);
        });
        stage.show();
    }

    /**
     * Tile the specified tab.
     * 
     * @param tab
     */
    private void tileInPane(UITab tab) {
        Node content = tab.getContent();
        tab.setContent(null);

        split.ui.getItems().add(content);

        reallocate(split.ui);
    }

    /**
     * Move tab.
     * 
     * @param tab
     * @param from
     * @param to
     */
    private void move(Tab tab, TabPane from, TabPane to) {
        ObservableList<Tab> froms = from.getTabs();

        if (froms.remove(tab)) {
            to.getTabs().add(tab);

            if (from == main.ui) {
                tab.setOnCloseRequest(e -> move(tab, tab.getTabPane(), main.ui));
            } else if (to == main.ui) {
                tab.setOnCloseRequest(null);
            }

            if (froms.isEmpty()) {
                // remove tab from parent split pane
                SplitPane parentSplit = findParentSplit(from);
                parentSplit.getItems().remove(from);
                reallocate(parentSplit);
            }
        }
    }

    /**
     * Add the specified tab to the target {@link TabPane}.
     * 
     * @param tab
     * @param to
     */
    private void add(Tab tab, TabPane to) {
        TabPane from = tab.getTabPane();

        if (from == to) {
            return;
        }

        remove(tab);
        to.getTabs().add(tab);
    }

    /**
     * Remove the specified tab from the current {@link TabPane}.
     * 
     * @param tab
     */
    private void remove(Tab tab) {
        TabPane pane = tab.getTabPane();
        ObservableList<Tab> tabs = pane.getTabs();
        if (tabs.remove(tab) && tabs.isEmpty()) {
            // remove tab from parent split pane
            SplitPane split = findParentSplit(pane);
            split.getItems().remove(pane);
            reallocate(split);
        }
    }

    /**
     * Find the nearest ancestor {@link SplitPane}.
     * 
     * @param node
     * @return
     */
    private SplitPane findParentSplit(Node node) {
        while (node instanceof SplitPane == false) {
            node = node.getParent();
        }
        return (SplitPane) node;
    }

    /**
     * Compute all positions for equal spacing.
     * 
     * @return
     */
    private void allocateEvenWidth() {
        int itemSize = split.ui.getItems().size();
        double equalSpacing = 1d / itemSize;
        double[] positions = new double[itemSize - 1];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = equalSpacing * (i + 1);
        }
        split.ui.setDividerPositions(positions);
    }

    /**
     * Compute all positions for equal spacing.
     * 
     * @return
     */
    private void reallocate(SplitPane split) {
        int itemSize = split.getItems().size();
        double equalSpacing = 1d / itemSize;
        double[] positions = new double[itemSize - 1];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = equalSpacing * (i + 1);
        }
        split.setDividerPositions(positions);
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        // initialize logger for non-main thread
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        // activate application
        Viewtify.application()
                .use(Theme.Dark)
                .icon("icon/app.png")
                .language(Lang.of(I.env("language", Locale.getDefault().getLanguage())))
                .onTerminating(Network::terminate)
                .activate(TradeMate.class);
    }

    /**
     * 
     */
    private static class TradeTabePane extends TabPane {

        /**
         * 
         */
        private TradeTabePane() {

        }
    }
}
