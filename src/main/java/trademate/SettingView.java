/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

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
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UISpinner;
import viewtify.ui.helper.User;
import viewtify.util.Icon;

/**
 * @version 2018/08/27 18:53:30
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View {

    /** The message resource. */
    private final Lang $ = localizeBy(Lang.class);

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    private @UI DesktopSetting desktop;

    private @UI NotifySetting longTrend;

    private @UI NotifySetting shortTrend;

    private @UI NotifySetting execution;

    private @UI NotifySetting orderFailed;

    private @UI NotifySetting priceSignal;

    /** The access token for LINE. */
    private @UI UIPassword lineAccessToken;

    /** The access token tester. */
    private @UI UIButton lineTest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        longTrend.notify = notificator.longTrend;
        shortTrend.notify = notificator.shortTrend;
        execution.notify = notificator.execution;
        orderFailed.notify = notificator.orderFailed;
        priceSignal.notify = notificator.priceSignal;

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
     * @version 2018/08/28 17:53:05
     */
    private class NotifySetting extends View {

        private Notify notify;

        /** Enable desktop notification. */
        private @UI UICheckBox desktop;

        /** Enable LINE notification. */
        private @UI UICheckBox line;

        /** Enable sound notification. */
        private @UI UIComboBox<Sound> sound;

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
    }

    /**
     * @version 2018/08/27 19:35:15
     */
    private class DesktopSetting extends View {

        private @UI UISpinner<Duration> duration;

        private @UI UIComboBox<DesktopPosition> position;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            duration.values(I.signalRange(2, 30, 2).map(Duration::ofSeconds)).model(notificator.desktopDuration).text($::desktopDuration);
            position.values(DesktopPosition.class).model(notificator.desktopPosition);
        }
    }

    /**
     * @version 2018/08/27 21:10:05
     */
    @SuppressWarnings("unused")
    private static class Lang implements Extensible {

        /**
         * Title for the notification.
         * 
         * @return
         */
        String notificationTitle() {
            return "Notification";
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

            /**
             * {@inheritDoc}
             */
            @Override
            String notificationTitle() {
                return "通知設定";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String desktopColumn() {
                return "デスクトップ";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String soundColumn() {
                return "音声";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String longTrendRow() {
                return "買いトレンド";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String shortTrendRow() {
                return "売りトレンド";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String executionRow() {
                return "約定";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String orderFailedRow() {
                return "注文失敗";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String priceSignalRow() {
                return "指定値へ到達";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String desktopTitle() {
                return "デスクトップへの通知";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String desktopDurationLabel() {
                return "表示時間";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String desktopDuration(Duration duration) {
                return duration.getSeconds() + "秒";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String desktopPositionLabel() {
                return "表示位置";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String lineTitle() {
                return "LINEへの通知";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String lineDescription() {
                return "LINE Notifyから取得したアクセストークンを指定することでLINEに通知することが出来ます。";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String lineAccessTokenLabel() {
                return "アクセストークン";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String lineTest() {
                return "テストメッセージを送信";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String lineTestFailed(String token) {
                return "指定されたアクセストークン [" + token + "] は正しくありません。\r\n正しいトークンを指定した後に、再度テストを行ってください。";
            }
        }
    }
}
