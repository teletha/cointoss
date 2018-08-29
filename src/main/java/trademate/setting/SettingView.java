/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

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
    @SuppressWarnings("unused")
    private final Lang $ = localizeBy(Lang.class);

    private @UI UILabel notification;

    private @UI UILabel bitflyer;

    private @UI Pane setting;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        show(NotificationSetting.class);

        notification.when(User.MouseClick, () -> show(NotificationSetting.class));
        bitflyer.when(User.MouseClick, () -> show(BitFlyerSetting.class));
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
    @SuppressWarnings("unused")
    @Manageable(lifestyle = Singleton.class)
    private static class Lang implements Extensible {

        /**
         * Category title.
         * 
         * @return
         */
        String notification() {
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
            String notification() {
                return "通知";
            }
        }
    }
}
