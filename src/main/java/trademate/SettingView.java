/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UILabel;
import viewtify.ui.helper.User;

/**
 * @version 2018/08/29 3:51:53
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View {

    /** The message resource. */
    private final Lang $ = localizeBy(Lang.class);

    private @UI UILabel marketCategory;

    private @UI UILabel notificationCategory;

    private @UI Pane setting;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        show(NotificationSettingView.class);

        marketCategory.when(User.MouseClick, () -> show(MarketSettingView.class));
        notificationCategory.when(User.MouseClick, () -> show(NotificationSettingView.class));
    }

    /**
     * Show the specified setting view.
     * 
     * @param viewType
     */
    private <V extends View> void show(Class<V> viewType) {
        V view = I.make(viewType);

        ObservableList<Node> children = setting.getChildren();

        if (children.isEmpty()) {
            children.add(view.root());
        } else {
            children.set(0, view.root());
        }
    }

    /**
     * @version 2018/08/29 3:52:37
     */
    @Manageable(lifestyle = Singleton.class)
    private static class Lang implements Extensible {

        /**
         * Category title.
         * 
         * @return
         */
        String marketCategory() {
            return "Market";
        }

        /**
         * Category title.
         * 
         * @return
         */
        String notificationCategory() {
            return "Notification";
        }

        /**
         * @version 2018/08/29 3:53:49
         */
        private static class Lang_ja extends Lang {

            /**
             * {@inheritDoc}
             */
            @Override
            String marketCategory() {
                return "マーケット";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String notificationCategory() {
                return "通知";
            }
        }
    }
}
