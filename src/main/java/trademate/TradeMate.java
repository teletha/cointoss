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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
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
import kiss.Storable;
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
import viewtify.ui.helper.Actions;
import viewtify.ui.helper.User;

@Managed(value = Singleton.class)
public class TradeMate extends View {

    private final LayoutManager layout = I.make(LayoutManager.class);

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
                c.menu().text(en("Detach as window")).when(User.Action, () -> detachAsWindow(tab, service));
            });
            return new TradingView(tab, service);
        }, tab -> {
            if (layout.isWindow(service)) {
                detachAsWindow(tab, service);
            }
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
    private void detachAsWindow(UITab tab, MarketService service) {
        // remove content
        Pane content = unmerge(tab);

        layout.addWindow(service);

        Scene scene = new Scene(content, content.getPrefWidth(), content.getPrefHeight());
        scene.getStylesheets().addAll(main.ui.getScene().getStylesheets());

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().addAll(((Stage) main.ui.getScene().getWindow()).getIcons());
        stage.setTitle(tab.getText());
        stage.setOnCloseRequest(e -> {
            stage.close();
            merge(content);
            layout.removeWindow(service);
        });
        stage.show();
    }

    /**
     * Tile the specified tab.
     * 
     * @param tab
     */
    private void tileInPane(UITab tab) {
        Node contents = unmerge(tab);

        // move to tile area
        split.ui.getItems().add(contents);

        // relayout
        reallocate(split.ui);
    }

    /**
     * Merge tab from the detached contents.
     * 
     * @param pane
     */
    private void merge(Pane pane) {
        Tab tab = (Tab) pane.getProperties().remove("trademate-tab");
        tab.setContent(pane);
        tab.setDisable(false);
    }

    /**
     * Unmerge contents from tab.
     * 
     * @param tab
     */
    private Pane unmerge(Tab tab) {
        // remove contents from tab and disable tab
        Pane contents = (Pane) tab.getContent();
        tab.setContent(null);
        tab.setDisable(true);

        // store associated tab
        contents.getProperties().put("trademate-tab", tab);

        // select the nearest enable tab
        SingleSelectionModel<Tab> model = tab.getTabPane().getSelectionModel();
        if (!Actions.selectPrev(model)) {
            Actions.selectNext(model);
        }
        return contents;
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
    @Managed(Singleton.class)
    private static class LayoutManager implements Storable<LayoutManager> {

        /** The windowed services. */
        public List<String> windows = new ArrayList();

        /**
         * Hide
         */
        private LayoutManager() {
            restore();
        }

        /**
         * Add windowed service.
         * 
         * @param service
         */
        private void addWindow(MarketService service) {
            windows.add(service.marketIdentity());
            store();
        }

        /**
         * Remove windowed service.
         * 
         * @param service
         */
        private void removeWindow(MarketService service) {
            windows.remove(service.marketIdentity());
            store();
        }

        /**
         * Check whether the specified service is windowed or not.
         * 
         * @param service
         * @return
         */
        private boolean isWindow(MarketService service) {
            return windows.contains(service.marketIdentity());
        }
    }

    /**
     * 
     */
    private enum LayoutKind {
        Normal, Tile, Window;
    }
}
