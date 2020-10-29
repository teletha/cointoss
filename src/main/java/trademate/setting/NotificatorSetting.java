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
import cointoss.util.Primitives;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import stylist.Style;
import stylist.StyleDSL;
import trademate.setting.Notificator.Notify;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISlider;
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
import viewtify.util.ScreenSelector;

@Managed(Singleton.class)
class NotificatorSetting extends View {

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** The notificator configuration UI. */
    private UITableView<Notify> notifications;

    private UITableColumn<Notify, String> name;

    private UITableColumn<Notify, Notify> desktop;

    private UITableColumn<Notify, Notify> line;

    private UITableColumn<Notify, Notify> sound;

    /** The desktop configuration UI. */
    private UISlider soundMasterVolume;;

    /** The desktop configuration UI. */
    private UISpinner<Duration> desktopDuration;

    /** The desktop configuration UI. */
    private UIComboBox<ScreenSelector> desktopMonitor;

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

                // Sound
                $(vbox, Block, () -> {
                    label(en("Sound Notification"), Heading);
                    form(en("Volume Level"), soundMasterVolume);
                });

                // Desktop
                $(vbox, Block, () -> {
                    label(en("Desktop Notification"), Heading);
                    form(en("Display Monitor"), desktopMonitor);
                    form(en("Display Location"), desktopPosition);
                    form(en("Display Time"), desktopDuration);
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
        notifications.items(notificator.types()).simplify();
        name.modelByVar(n -> n.name);
        desktop.text(en("Desktop")).renderAsCheckBox(notify -> notify.onDesktop, UICheckBox::sync);
        line.text(en("LINE")).renderAsCheckBox(notify -> notify.onLine, (ui, model) -> {
            ui.sync(model).disableWhen(notificator.lineAccessToken, String::isEmpty);
        });
        sound.text(en("Sound")).renderAsComboBox(notify -> notify.onSound, (ui, model) -> {
            ui.items(Sound.values()).sync(model).when(User.Action, () -> ui.value().play());
        });

        // For Sound
        soundMasterVolume.snapToTicks(true).showTickLabels(true).showTickMarks(true).sync(notificator.masterVolume);

        // For Desktop
        desktopMonitor.items(ScreenSelector.values()).sync(Toast.setting.screen);
        desktopPosition.items(Corner.values()).sync(Toast.setting.area);
        desktopDuration.items(1, 30, Duration::minutes)
                .sync(Toast.setting.autoHide)
                .format(duration -> Primitives.roundString(duration.toMinutes(), 0) + en("mins"));
        desktopNumber.items(1, 15, Integer::valueOf).sync(Toast.setting.max);

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