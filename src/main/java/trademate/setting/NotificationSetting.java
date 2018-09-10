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

import java.time.Duration;

import cointoss.util.Network;
import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import trademate.setting.NotificationSetting.Lang;
import trademate.setting.Notificator.DesktopPosition;
import trademate.setting.Notificator.Notify;
import viewtify.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.helper.User;
import viewtify.util.Icon;

/**
 * @version 2018/08/29 23:25:09
 */
@Manageable(lifestyle = Singleton.class)
public class NotificationSetting extends View<Lang> {

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
                        label($.notificationTitle(), Heading);
                        $(hbox, FormRow, () -> {
                            label("", FormLabel);
                            label($.desktopColumn(), FormCheck);
                            label($.lineColumn(), FormCheck);
                            label($.soundColumn(), FormCheck2);
                        });
                        $(longTrend);
                        $(shortTrend);
                        $(execution);
                        $(orderFailed);
                        $(priceSignal);
                    });

                    // Desktop
                    $(vbox, Block, () -> {
                        label($.desktopTitle(), Heading);
                        $(hbox, FormRow, () -> {
                            label($.desktopDurationLabel(), FormLabel);
                            $(desktopDuration, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label($.desktopPositionLabel(), FormLabel);
                            $(desktopPosition, FormInput);
                        });
                    });

                    // LINE
                    $(vbox, Block, () -> {
                        label($.lineTitle(), Heading);
                        label($.lineDescription(), Description);
                        $(hbox, FormRow, () -> {
                            label($.lineAccessTokenLabel(), FormLabel);
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
        desktopDuration.values(I.signalRange(2, 30, 2).map(Duration::ofSeconds))
                .model(notificator.desktopDuration)
                .text($::desktopDuration);
        desktopPosition.values(DesktopPosition.class).model(notificator.desktopPosition);

        // For LINE
        lineAccessToken.model(notificator.lineAccessToken);
        lineTest.when(User.Action, () -> {
            I.make(Network.class).line("TEST").to(e -> {
                lineAccessToken.decorateBy(Icon.Success);
            }, e -> {
                lineAccessToken.invalid($.lineTestFailed(lineAccessToken.value()));
            });
        });
    }

    /**
     * @version 2018/08/30 9:54:57
     */
    private class NotifySetting extends View<Lang> {

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
            line.model(notify.line).disableWhen(lineAccessToken.model().isEmpty());
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

    /**
     * @version 2018/08/27 21:10:05
     */
    @SuppressWarnings("unused")
    static class Lang implements Extensible {

        /**
         * Title for the notification.
         * 
         * @return
         */
        String notificationTitle() {
            return "Notification Type";
        }

        /**
         * Column label for the desktop notification.
         * 
         * @return
         */
        String desktopColumn() {
            return "Desktop";
        }

        /**
         * Column label for the LINE notification.
         * 
         * @return
         */
        String lineColumn() {
            return "LINE";
        }

        /**
         * Column label for the sound notification.
         * 
         * @return
         */
        String soundColumn() {
            return "Sound";
        }

        /**
         * Title for the desktop notification.
         * 
         * @return
         */
        String desktopTitle() {
            return "Desktop";
        }

        /**
         * Display duration label for desktop notification.
         * 
         * @return
         */
        String desktopDurationLabel() {
            return "Duration";
        }

        /**
         * Display duration of desktop notification.
         * 
         * @param duration
         * @return
         */
        String desktopDuration(Duration duration) {
            return duration.getSeconds() + " seconds";
        }

        /**
         * Display position label for desktop notification.
         * 
         * @return
         */
        String desktopPositionLabel() {
            return "Position";
        }

        /**
         * Title for LINE.
         * 
         * @return
         */
        String lineTitle() {
            return "LINE";
        }

        /**
         * Description for LINE.
         * 
         * @return
         */
        String lineDescription() {
            return "You can notify LINE by specifying the access token acquired from LINE Notify.";
        }

        /**
         * Label access token of LINE.
         * 
         * @return
         */
        String lineAccessTokenLabel() {
            return "Access Token";
        }

        /**
         * Label for the test button of LINE notify.
         * 
         * @return
         */
        String lineTest() {
            return "Send test message";
        }

        /**
         * LINE test was failed.
         * 
         * @param token
         * @return
         */
        String lineTestFailed(String token) {
            return "The specified token [" + token + "] is incorrect. Specify the correct token and then test again.";
        }

        /**
         * @version 2018/08/27 21:11:28
         */
        private static class Lang_ja extends Lang {

            @Override
            String notificationTitle() {
                return "通知の種類";
            }

            @Override
            String desktopColumn() {
                return "デスクトップ";
            }

            @Override
            String soundColumn() {
                return "音声";
            }

            @Override
            String desktopTitle() {
                return "デスクトップへの通知";
            }

            @Override
            String desktopDurationLabel() {
                return "表示時間";
            }

            @Override
            String desktopDuration(Duration duration) {
                return duration.getSeconds() + "秒";
            }

            @Override
            String desktopPositionLabel() {
                return "表示位置";
            }

            @Override
            String lineTitle() {
                return "LINEへの通知";
            }

            @Override
            String lineDescription() {
                return "[LINE Notify](https://notify-bot.line.me/)から取得したアクセストークンを指定することでLINEに通知することが出来ます。";
            }

            @Override
            String lineAccessTokenLabel() {
                return "アクセストークン";
            }

            @Override
            String lineTest() {
                return "テストメッセージを送信";
            }

            @Override
            String lineTestFailed(String token) {
                return "指定されたアクセストークン [" + token + "] は正しくありません。\r\n正しいトークンを指定した後に、再度テストを行ってください。";
            }
        }
    }
}
