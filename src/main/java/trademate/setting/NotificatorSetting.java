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

import static transcript.Transcript.en;

import java.time.Duration;

import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.setting.Notificator.DesktopPosition;
import trademate.setting.Notificator.Notify;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;
import viewtify.util.Icon;

@Managed(Singleton.class)
class NotificatorSetting extends View {

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

    interface style extends FormStyles, SettingStyles {
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {
            $(vbox, style.root, () -> {
                // Notification Types
                $(vbox, style.block, () -> {
                    label(en("Notification Type"), style.heading);
                    $(hbox, style.FormRow, () -> {
                        label("", style.FormLabel);
                        label(en("Desktop"), style.FormCheck, style.FormHeaderLabel);
                        label(en("LINE"), style.FormCheck, style.FormHeaderLabel);
                        label(en("Sound"), style.FormCheck2, style.FormHeaderLabel);
                    });

                    for (Notify type : notificator.types()) {
                        $(new Setting(type));
                    }
                });

                // Desktop
                $(vbox, style.block, () -> {
                    label(en("Desktop Notification"), style.heading);
                    form(en("Display Time"), desktopDuration);
                    form(en("Display Position"), desktopPosition);
                });

                // LINE
                $(vbox, style.block, () -> {
                    label(en("LINE Notification"), style.heading);
                    label(en("You can notify LINE by specifying the access token acquired from [LINE Notify](https://notify-bot.line.me/)."), style.Description);
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
        // For Desktop
        desktopPosition.items(DesktopPosition.values()).sync(notificator.desktopPosition);
        desktopDuration.items(I.signal(2).recurse(v -> v + 2).take(30).map(Duration::ofSeconds))
                .sync(notificator.desktopDuration)
                .format(duration -> duration.getSeconds() + en("seconds").get());

        // For LINE
        lineAccessToken.sync(notificator.lineAccessToken);
        lineTest.text(en("Send test message")).when(User.Action, () -> {
            I.make(Network.class)
                    .line(en("LINE Access Token Test"), en("The specified token is valid."), notificator.lineAccessToken.v)
                    .to(e -> {
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
            line.sync(notify.onLine).disableWhen(notificator.lineAccessToken.observing().is(String::isEmpty));
            sound.items(Sound.values()).sync(notify.onSound).when(User.Action, () -> sound.value().play());
        }

        class view extends ViewDSL {
            {
                $(hbox, FormStyles.FormRow, () -> {
                    label(notify.name, FormStyles.FormLabel);
                    $(hbox, FormStyles.FormCheck, () -> {
                        $(desktop);
                    });
                    $(hbox, FormStyles.FormCheck, () -> {
                        $(line);
                    });
                    $(hbox, FormStyles.FormCheck2, () -> {
                        $(sound);
                    });
                });
            }
        }
    }
}
