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
import kiss.Manageable;
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

@Manageable(lifestyle = Singleton.class)
public class NotificationSetting extends View {

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    private NotifySetting longTrend = new NotifySetting(notificator.longTrend);

    private NotifySetting shortTrend = new NotifySetting(notificator.shortTrend);

    private NotifySetting execution = new NotifySetting(notificator.execution);

    private NotifySetting orderFailed = new NotifySetting(notificator.orderFailed);

    private NotifySetting priceSignal = new NotifySetting(notificator.priceSignal);

    private UISpinner<Duration> desktopDuration;

    private UIComboBox<DesktopPosition> desktopPosition;

    /** The access token for LINE. */
    private UIPassword lineAccessToken;

    /** The access token tester. */
    private UIButton lineTest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
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
                        $(longTrend);
                        $(shortTrend);
                        $(execution);
                        $(orderFailed);
                        $(priceSignal);
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
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // For Desktop
        desktopDuration.values(I.signal(2).recurse(v -> v + 2).take(30).map(Duration::ofSeconds))
                .model(notificator.desktopDuration)
                .text(duration -> duration.getSeconds() + en("seconds").get());
        desktopPosition.values(DesktopPosition.class).model(notificator.desktopPosition);

        // For LINE
        lineAccessToken.model(notificator.lineAccessToken);
        lineTest.text(en("Send test message")).when(User.Action, () -> {
            I.make(Network.class).line("TEST").to(e -> {
                lineAccessToken.decorateBy(Icon.Success);
            }, e -> {
                lineAccessToken.invalid(en("The specified token [{0}] is incorrect. Specify the correct token and then test again.")
                        .with(lineAccessToken.value()));
            });
        });
    }

    /**
     * @version 2018/08/30 9:54:57
     */
    private class NotifySetting extends View {

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
        private NotifySetting(Notify notify) {
            this.notify = notify;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            desktop.model(notify.desktop);
            line.model(notify.line).disableWhen(notificator.lineAccessToken.iŝ(String::isEmpty));
            sound.values(Sound.values()).model(notify.sound).when(User.Action, e -> {
                sound.value().play();
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected UI declareUI() {
            return new UI() {
                {
                    $(hbox, FormRow, () -> {
                        label(notify, FormLabel);
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
            };
        }
    }
}
