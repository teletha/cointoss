/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import javafx.util.Duration;

import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.setting.Notificator.Notify;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;
import viewtify.ui.toast.Toast;
import viewtify.util.Corner;
import viewtify.util.Icon;

@Managed(Singleton.class)
class NotificatorSetting extends View {

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** The notificator configuration UI. */
    private UITableView<Notify> notifications;

    private UITableColumn<Notify, Variable<String>> name;

    private UITableColumn<Notify, Notify> desktop;

    private UITableColumn<Notify, Notify> line;

    private UITableColumn<Notify, Notify> sound;

    /** The desktop configuration UI. */
    private UISpinner<Duration> desktopDuration;

    /** The desktop configuration UI. */
    private UIComboBox<Corner> desktopPosition;

    /** The desktop configuration UI. */
    private UISpinner<Integer> desktopNumber;

    /** The LINE configuration UI. */
    private UIText lineAccessToken;

    /** The LINE configuration UI. */
    private UIButton lineTest;

    interface style extends StyleDSL {
        Style Types = () -> {
            display.height(190, px);
        };
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL implements FormStyles, SettingStyles {
        {
            $(vbox, () -> {
                // Notification Types
                $(vbox, Block, () -> {
                    label(en("Notification Type"), Heading);
                    $(notifications, style.Types, () -> {
                        $(name, FormLabel);
                        $(desktop);
                        $(line);
                        $(sound, FormInput);
                    });
                });

                // Desktop
                $(vbox, Block, () -> {
                    label(en("Desktop Notification"), Heading);
                    form(en("Display Time"), desktopDuration);
                    form(en("Display Location"), desktopPosition);
                    form(en("Number of Displays"), desktopNumber);
                });

                // LINE
                $(vbox, Block, () -> {
                    label(en("LINE Notification"), Heading);
                    label(en("You can notify LINE by specifying the access token acquired from [LINE Notify](https://notify-bot.line.me/)."), Description);
                    form(en("Access Token"), lineAccessToken, lineTest);
                });
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // For Notifications
        notifications.items(notificator.types()).operatable(false).simplify();
        name.model(n -> n.name);
        desktop.text(en("Desktop")).renderAsCheckBox(n -> n.onDesktop, UICheckBox::sync);
        line.text(en("LINE")).renderAsCheckBox(n -> n.onLine, (ui, value) -> {
            ui.sync(value).disableWhen(notificator.lineAccessToken.observing().is(String::isEmpty));
        });
        sound.text(en("Sound")).renderAsComboBox(n -> n.onSound, (ui, value) -> {
            ui.items(Sound.values()).sync(value).when(User.Action, () -> ui.value().play());
        });

        // For Desktop
        desktopPosition.items(Corner.values()).sync(Toast.setting.area);
        desktopDuration.items(I.signal(1).recurse(v -> v + 1).take(60).map(Duration::minutes))
                .sync(Toast.setting.autoHide)
                .format(duration -> String.valueOf(duration.toMinutes()) + en("mins"));
        desktopNumber.items(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).sync(Toast.setting.max);

        // For LINE
        lineAccessToken.sync(notificator.lineAccessToken).masking(true);
        lineTest.text(en("Send test message")).when(User.Action, () -> {
            Network.line(en("LINE Access Token Test"), en("The specified token is valid."), notificator.lineAccessToken.v).to(e -> {
                lineAccessToken.decorateBy(Icon.Success);
            }, e -> {
                e.printStackTrace();
                lineAccessToken
                        .invalid(en("The specified token [{0}] is incorrect. Specify the correct token and then test again.", lineAccessToken
                                .value()));
            });
        });
    }
}