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
import trademate.preference.Notificator;
import trademate.preference.Notificator.DesktopPosition;
import trademate.preference.Notificator.Notify;
import trademate.preference.Sound;
import trademate.setting.NotificationSetting.Lang;
import viewtify.View;
import viewtify.dsl.UIDefinition;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UISpinner;
import viewtify.ui.helper.User;
import viewtify.util.Icon;

/**
 * @version 2018/08/29 23:25:09
 */
@Manageable(lifestyle = Singleton.class)
public class NotificationSetting extends View<Lang> {

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** Enable desktop notification. */
    private UICheckBox longTrendDesktop;

    /** Enable LINE notification. */
    private UICheckBox longTrendLine;

    /** Enable sound notification. */
    private UIComboBox<Sound> longTrendSound;

    /** Enable desktop notification. */
    private UICheckBox shortTrendDesktop;

    /** Enable LINE notification. */
    private UICheckBox shortTrendLine;

    /** Enable sound notification. */
    private UIComboBox<Sound> shortTrendSound;

    /** Enable desktop notification. */
    private UICheckBox executionDesktop;

    /** Enable LINE notification. */
    private UICheckBox executionLine;

    /** Enable sound notification. */
    private UIComboBox<Sound> executionSound;

    /** Enable desktop notification. */
    private UICheckBox orderFailedDesktop;

    /** Enable LINE notification. */
    private UICheckBox orderFailedLine;

    /** Enable sound notification. */
    private UIComboBox<Sound> orderFailedSound;

    /** Enable desktop notification. */
    private UICheckBox priceSignalDesktop;

    /** Enable LINE notification. */
    private UICheckBox priceSignalLine;

    /** Enable sound notification. */
    private UIComboBox<Sound> priceSignalSound;

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
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                vbox(Root, () -> {
                    // Notification Types
                    vbox(Block, () -> {
                        label(message.notificationTitle(), Heading);
                        hbox(FormRow, () -> {
                            label("", FormLabel);
                            hbox(FormCheck, label(message.desktopColumn()));
                            hbox(FormCheck, label(message.lineColumn()));
                            hbox(FormCheck2, label(message.soundColumn()));
                        });
                        hbox(FormRow, () -> {
                            label(message.longTrendRow(), FormLabel);
                            hbox(FormCheck, longTrendDesktop);
                            hbox(FormCheck, longTrendLine);
                            hbox(FormCheck2, longTrendSound);
                        });
                        hbox(FormRow, () -> {
                            label(message.shortTrendRow(), FormLabel);
                            hbox(FormCheck, shortTrendDesktop);
                            hbox(FormCheck, shortTrendLine);
                            hbox(FormCheck2, shortTrendSound);
                        });
                        hbox(FormRow, () -> {
                            label(message.executionRow(), FormLabel);
                            hbox(FormCheck, executionDesktop);
                            hbox(FormCheck, executionLine);
                            hbox(FormCheck2, executionSound);
                        });
                        hbox(FormRow, () -> {
                            label(message.orderFailedRow(), FormLabel);
                            hbox(FormCheck, orderFailedDesktop);
                            hbox(FormCheck, orderFailedLine);
                            hbox(FormCheck2, orderFailedSound);
                        });
                        hbox(FormRow, () -> {
                            label(message.priceSignalRow(), FormLabel);
                            hbox(FormCheck, priceSignalDesktop);
                            hbox(FormCheck, priceSignalLine);
                            hbox(FormCheck2, priceSignalSound);
                        });
                    });

                    // Desktop
                    vbox(Block, () -> {
                        label(message.desktopTitle(), Heading);
                        hbox(FormRow, () -> {
                            label(message.desktopDurationLabel(), FormLabel);
                            $(desktopDuration, FormInput);
                        });
                        hbox(FormRow, () -> {
                            label(message.desktopPositionLabel(), FormLabel);
                            $(desktopPosition, FormInput);
                        });
                    });

                    // LINE
                    vbox(Block, () -> {
                        label(message.lineTitle(), Heading);
                        label(message.lineDescription(), Description);
                        hbox(FormRow, () -> {
                            label(message.lineAccessTokenLabel(), FormLabel);
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
        // For Notification Type
        init(notificator.longTrend, longTrendDesktop, longTrendLine, longTrendSound);
        init(notificator.shortTrend, shortTrendDesktop, shortTrendLine, shortTrendSound);
        init(notificator.execution, executionDesktop, executionLine, executionSound);
        init(notificator.orderFailed, orderFailedDesktop, orderFailedLine, orderFailedSound);
        init(notificator.priceSignal, priceSignalDesktop, priceSignalLine, priceSignalSound);

        // For Desktop
        desktopDuration.values(I.signalRange(2, 30, 2).map(Duration::ofSeconds))
                .model(notificator.desktopDuration)
                .text(message::desktopDuration);
        desktopPosition.values(DesktopPosition.class).model(notificator.desktopPosition);

        // For LINE
        lineAccessToken.model(notificator.lineAccessToken);
        lineTest.when(User.Action, () -> {
            I.make(Network.class).line("TEST").to(e -> {
                lineAccessToken.decorateBy(Icon.Success);
            }, e -> {
                lineAccessToken.invalid(message.lineTestFailed(lineAccessToken.value()));
            });
        });
    }

    private void init(Notify notify, UICheckBox desktop, UICheckBox line, UIComboBox<Sound> sound) {
        desktop.model(notify.desktop);
        line.model(notify.line).disableWhen(lineAccessToken.model().isEmpty());
        sound.values(Sound.values()).model(notify.sound).when(User.Action, e -> {
            sound.value().play();
        });
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
         * Row label for the long trend.
         * 
         * @return
         */
        String longTrendRow() {
            return "Long Trend";
        }

        /**
         * Row label for the short trend.
         * 
         * @return
         */
        String shortTrendRow() {
            return "Long Trend";
        }

        /**
         * Row label for the execution.
         * 
         * @return
         */
        String executionRow() {
            return "Execution";
        }

        /**
         * Row label for the failed order.
         * 
         * @return
         */
        String orderFailedRow() {
            return "Order Failed";
        }

        /**
         * Row label for the price signal.
         * 
         * @return
         */
        String priceSignalRow() {
            return "Price Signal";
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
            String longTrendRow() {
                return "買いトレンド";
            }

            @Override
            String shortTrendRow() {
                return "売りトレンド";
            }

            @Override
            String executionRow() {
                return "約定";
            }

            @Override
            String orderFailedRow() {
                return "注文失敗";
            }

            @Override
            String priceSignalRow() {
                return "指定値へ到達";
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
