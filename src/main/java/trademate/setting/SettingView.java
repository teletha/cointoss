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

import static trademate.setting.SettingStyles.*;

import kiss.Extensible;
import kiss.Manageable;
import kiss.Singleton;
import viewtify.UI;
import viewtify.ui.UILabel;
import viewtify.ui.UIPane;
import viewtify.ui.View;
import viewtify.ui.helper.User;

/**
 * @version 2018/08/29 3:51:53
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View<SettingView.Lang> {

    private UILabel notification;

    private UILabel bitflyer;

    private UIPane setting;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                hbox(() -> {
                    vbox(CategoryPane, () -> {
                        $(notification, CategoryLabel);
                        $(bitflyer, CategoryLabel);
                    });
                    $(setting);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        setting.set(NotificationSetting.class);

        notification.when(User.MouseClick, () -> setting.set(NotificationSetting.class));
        bitflyer.when(User.MouseClick, () -> setting.set(BitFlyerSetting.class));
    }

    /**
     * @version 2018/08/29 3:52:37
     */
    @SuppressWarnings("unused")
    @Manageable(lifestyle = Singleton.class)
    static class Lang implements Extensible {

        /**
         * Category title.
         * 
         * @return
         */
        String notification() {
            return "Notification";
        }

        /**
         * Category title.
         * 
         * @return
         */
        String bitflyer() {
            return "BitFlyer";
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
