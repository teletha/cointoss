/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import cointoss.util.Network;
import kiss.I;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.setting.Notificator.Notify;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UISlider;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.UIText;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;
import viewtify.ui.toast.ToastSettingView;
import viewtify.ui.view.PreferenceViewBase;
import viewtify.util.Icon;

class NotificatorSetting extends PreferenceViewBase {

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
    private ToastSettingView toast;

    /** The LINE configuration UI. */
    private UIText<String> lineAccessToken;

    /** The LINE configuration UI. */
    private UIButton lineTest;

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable<String> category() {
        return en("Event");
    }

    interface style extends StyleDSL {
        Style NotificationTypeTable = () -> {
            display.maxHeight(228, px);
        };
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL implements FormStyles, SettingStyles {
        {
            $(vbox, () -> {
                // Notification Types
                $(vbox, () -> {
                    label(en("Notification Type"), Heading);
                    $(notifications, style.NotificationTypeTable, () -> {
                        $(name, Label);
                        $(desktop);
                        $(line);
                        $(sound, Input);
                    });
                });

                // Sound
                $(vbox, () -> {
                    label(en("Sound Notification"), Heading);
                    form(en("Volume Level"), soundMasterVolume);
                });

                // Desktop
                $(vbox, () -> {
                    label(en("Desktop Notification"), Heading);
                    $(toast);
                });

                // LINE
                $(vbox, () -> {
                    label(en("LINE Notification"), Heading);
                    label(en("You can notify LINE by specifying the access token acquired from [LINE Notify](https://notify-bot.line.me/)."), SettingStyles.Description);
                    form(en("Access Token"), lineAccessToken);
                    form(en("Test message sending"), lineTest);
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
        desktop.text(en("Desktop")).renderAsCheckBox(notify -> notify.ⅰ.onDesktop, UICheckBox::sync);
        line.text(en("LINE")).renderAsCheckBox(notify -> notify.ⅰ.onLine, (ui, model, disposer) -> {
            ui.sync(model, disposer).disableWhen(notificator.lineAccessToken, String::isEmpty);
        });
        sound.text(en("Sound")).renderAsComboBox(notify -> notify.ⅰ.onSound, (ui, model, disposer) -> {
            ui.items(Sound.values()).sync(model, disposer);
        });

        // For Sound
        soundMasterVolume.snapToTicks(true).showTickLabels(true).showTickMarks(true).sync(notificator.masterVolume);

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