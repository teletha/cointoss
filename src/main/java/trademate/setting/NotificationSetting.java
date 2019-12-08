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

import static trademate.setting.SettingStyles.*;
import static transcript.Transcript.en;

import java.time.Duration;

import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.setting.Notificator.DesktopPosition;
import trademate.setting.Notificator.Notify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.helper.User;
import viewtify.util.Icon;

@Managed(value = Singleton.class)
public class NotificationSetting extends View {

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** The desktop configuration UI. */
    private UISpinner<Duration> desktopDuration;

    /** The desktop configuration UI. */
    private UIComboBox<DesktopPosition> desktopPosition;

    /** The LINE configuration UI. */
    private UIPassword lineAccessToken;

    /** The LINE configuration UI. */
    private UIButton lineTest;

    /**
     * UI definition.
     */
    class view extends UI {
        {
            $(vbox, Root, () -> {
                // Notification Types
                $(vbox, Block, () -> {
                    label(en("Notification Type"), Heading);
                    $(hbox, FormRow, () -> {
                        label("", FormLabel);
                        label(en("Desktop"), FormCheck, FormHeaderLabel);
                        label(en("LINE"), FormCheck, FormHeaderLabel);
                        label(en("Sound"), FormCheck2, FormHeaderLabel);
                    });

                    for (Notify type : notificator.types()) {
                        $(new Setting(type));
                    }
                });

                // Desktop
                $(vbox, Block, () -> {
                    label(en("Desktop Notification"), Heading);
                    $(hbox, FormRow, () -> {
                        label(en("Display Time"), FormLabel);
                        $(desktopDuration, FormInput);
                    });
                    $(hbox, FormRow, () -> {
                        label(en("Display Position"), FormLabel);
                        $(desktopPosition, FormInput);
                    });
                });

                // LINE
                $(vbox, Block, () -> {
                    label(en("LINE Notification"), Heading);
                    label(en("You can notify LINE by specifying the access token acquired from [LINE Notify](https://notify-bot.line.me/)."), Description);
                    $(hbox, FormRow, () -> {
                        label(en("Access Token"), FormLabel);
                        $(lineAccessToken, FormInput);
                        $(lineTest, FormInput);
                    });
                });
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // For Desktop
        desktopPosition.items(DesktopPosition.values()).sync(notificator.desktopPosition);
        desktopDuration.items(I.signal(2).recurse(v -> v + 2).take(30).map(Duration::ofSeconds))
                .sync(notificator.desktopDuration)
                .format(duration -> duration.getSeconds() + en("seconds").get());

        // For LINE
        lineAccessToken.sync(notificator.lineAccessToken);
        lineTest.text(en("Send test message")).when(User.Action, () -> {
            I.make(Network.class).line("TEST", notificator.lineAccessToken.v).to(e -> {
                lineAccessToken.decorateBy(Icon.Success);
            }, e -> {
                lineAccessToken.invalid(en("The specified token [{0}] is incorrect. Specify the correct token and then test again.")
                        .with(lineAccessToken.value()));
            });
        });
    }

    /**
     * 
     */
    class Setting extends View {

        /** The notify type. */
        private final Notify notify;

        /** Enable desktop notification. */
        private UICheckBox desktop;

        /** Enable LINE notification. */
        private UICheckBox line;

        /** Enable sound notification. */
        private UIComboBox<Sound> sound;

        /**
         * @param notify
         */
        private Setting(Notify notify) {
            this.notify = notify;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            desktop.sync(notify.onDesktop);
            line.sync(notify.onLine).disableWhen(notificator.lineAccessToken.iŝ(String::isEmpty));
            sound.items(Sound.values()).sync(notify.onSound).when(User.Action, () -> sound.value().play());
        }

        class view extends UI {
            {
                $(hbox, FormRow, () -> {
                    label(notify.name, FormLabel);
                    $(hbox, FormCheck, () -> {
                        $(desktop);
                    });
                    $(hbox, FormCheck, () -> {
                        $(line);
                    });
                    $(hbox, FormCheck2, () -> {
                        $(sound);
                    });
                });
            }
        }
    }
}
