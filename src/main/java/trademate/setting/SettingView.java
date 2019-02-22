/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import java.util.List;

import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.ui.UI;
import viewtify.ui.UILabel;
import viewtify.ui.UIPane;
import viewtify.ui.View;
import viewtify.ui.helper.User;

/**
 * @version 2018/08/29 3:51:53
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View {

    /** The locale resource. */
    private final Lang $ = I.i18n(Lang.class);

    private UILabel general;

    private UILabel appearance;

    private UILabel chart;

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
                $(hbox, () -> {
                    $(vbox, S.CategoryPane, () -> {
                        $(general, S.CategoryLabel);
                        $(appearance, S.CategoryLabel);
                        $(chart, S.CategoryLabel);
                        $(notification, S.CategoryLabel);
                        $(bitflyer, S.CategoryLabel);
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
        select(notification, NotificationSetting.class);

        general.text($::general).when(User.MouseClick, () -> select(general, GeneralSetting.class));
        appearance.text($::appearance).when(User.MouseClick, () -> select(appearance, AppearanceSetting.class));
        chart.text($::chart).when(User.MouseClick, () -> select(appearance, ChartSetting.class));
        notification.text($::notification).when(User.MouseClick, () -> select(notification, NotificationSetting.class));
        bitflyer.text($::bitflyer).when(User.MouseClick, () -> select(bitflyer, BitFlyerSetting.class));
    }

    private void select(UILabel selected, Class<? extends View> view) {
        for (UILabel label : List.of(general, appearance, notification, bitflyer)) {
            if (label == selected) {
                label.style(S.Selected);
            } else {
                label.unstyle(S.Selected);
            }
        }
        setting.set(view);
    }

    /**
     * @version 2018/09/10 9:53:37
     */
    interface S extends StyleDSL {

        Style CategoryPane = () -> {
            padding.top(40, px);
        };

        Style CategoryLabel = () -> {
            display.width(200, px).height(20, px);
            padding.vertical(10, px).left(40, px);
            cursor.pointer();
            font.size(16, px);

            $.hover(() -> {
                background.color("derive(-fx-base, 15%)");
            });
        };

        Style Selected = () -> {
            background.color("derive(-fx-base, 6%)");
        };

    }

    /**
     * @version 2018/09/10 20:14:06
     */
    @SuppressWarnings("unused")
    @Manageable(lifestyle = Singleton.class)
    static class Lang implements Extensible {
        /**
         * Category title.
         * 
         * @return
         */
        String general() {
            return "General";
        }

        /**
         * Category title.
         * 
         * @return
         */
        String appearance() {
            return "Appearance";
        }

        /**
         * Category title.
         * 
         * @return
         */
        String chart() {
            return "Chart";
        }

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
            String general() {
                return "一般";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String appearance() {
                return "外観";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String chart() {
                return "チャート";
            }

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
