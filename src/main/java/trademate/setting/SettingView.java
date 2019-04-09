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

import kiss.Manageable;
import kiss.Singleton;
import stylist.Style;
import stylist.StyleDSL;
import transcript.Transcript;
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
                    $(vbox, $.CategoryPane, () -> {
                        $(general, $.CategoryLabel);
                        $(appearance, $.CategoryLabel);
                        $(chart, $.CategoryLabel);
                        $(notification, $.CategoryLabel);
                        $(bitflyer, $.CategoryLabel);
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

        general.text($.General).when(User.MouseClick, () -> select(general, GeneralSetting.class));
        appearance.text($.Appearance).when(User.MouseClick, () -> select(appearance, AppearanceSetting.class));
        chart.text($.Chart).when(User.MouseClick, () -> select(appearance, ChartSetting.class));
        notification.text($.Notification).when(User.MouseClick, () -> select(notification, NotificationSetting.class));
        bitflyer.text($.Bitflyer).when(User.MouseClick, () -> select(bitflyer, BitFlyerSetting.class));
    }

    private void select(UILabel selected, Class<? extends View> view) {
        for (UILabel label : List.of(general, appearance, notification, bitflyer)) {
            if (label == selected) {
                label.style($.Selected);
            } else {
                label.unstyle($.Selected);
            }
        }
        setting.set(view);
    }

    /**
     * Resource definition.
     */
    interface $ extends StyleDSL {

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

        Transcript General = Transcript.en("General");

        Transcript Appearance = Transcript.en("Appearance");

        Transcript Chart = Transcript.en("Chart");

        Transcript Notification = Transcript.en("Notification");

        Transcript Bitflyer = Transcript.en("Bitflyer");
    }
}
