/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;

import cointoss.Market;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import psychopath.Locator;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;

@Managed(value = Singleton.class)
public class TradeMate extends View {

    /** The tab loading strategy. */
    private final TabLoader loader = new TabLoader();

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(DockSystem.UI);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        DockSystem.register("Setting").contents(SettingView.class).closable(false);
        DockSystem.register("BackTest").contents(BackTestView.class).closable(false);
        DockSystem.register("Global Volume").contents(GlobalVolumeView.class).closable(false);

        // ========================================================
        // Create Tab for each Markets
        // ========================================================
        MarketServiceProvider.availableMarketServices().to(service -> {
            UITab tab = DockSystem.register(service.marketIdentity())
                    .closable(false)
                    .text(service.marketReadableName)
                    .contents(ui -> new TradingView(ui, service));

            loader.add(tab);
        });

        // ========================================================
        // Clock in Title bar
        // ========================================================
        Chrono.seconds().map(Chrono.DateDayTime::format).combineLatest(Wisdom.random()).on(Viewtify.UIThread).to(v -> {
            stage().v.setTitle(v.ⅰ + "  " + v.ⅱ);
        });
    }

    /**
     * {@link TradeMate} will automatically initialize in the background if any tab has not been
     * activated yet.
     */
    public final void requestLazyInitialization() {
        loader.tryLoad();
    }

    private static class TabLoader {

        /** The queue for loading tabs. */
        private final LinkedList<UITab> queue = new LinkedList();

        /** The current processing tab. */
        private UITab current;

        /**
         * Add tab to loading queue.
         * 
         * @param tab
         */
        private void add(UITab tab) {
            queue.add(tab);
        }

        private synchronized void tryLoad() {
            if (current == null || current.isLoaded()) {
                current = queue.pollFirst();

                if (current != null) {
                    if (current.isLoaded()) {
                        tryLoad();
                    } else {
                        Viewtify.inUI(() -> {
                            current.load();
                        });
                    }
                }
            }
        }
    }

    /**
     * Managing words of widsom.
     */
    private static class Wisdom {

        private static final List<String> words = Locator.file("wisdom.txt").lines().toList();

        private static final Random random = new Random();

        private static Signal<String> random() {
            return Chrono.minutes().map(v -> words.get(random.nextInt(words.size())));
        }
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application().logging(LogManager.getLogger()::error).use(Theme.Dark).icon("icon/app.png").activate(TradeMate.class);
    }
}