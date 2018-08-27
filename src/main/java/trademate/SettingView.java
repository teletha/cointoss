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
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.helper.User;

/**
 * @version 2018/08/27 18:53:30
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View {

    /** The message resource. */
    private static final Lang message = I.i18n(Lang.class);

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    private @UI NotifySetting longTrend;

    private @UI NotifySetting shortTrend;

    private @UI NotifySetting execution;

    private @UI NotifySetting orderFailed;

    private @UI NotifySetting priceSignal;

    private @UI DesktopSetting desktop;

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
    }

    /**
     * @version 2018/08/27 18:53:26
     */
    private class NotifySetting extends View {

        private Notify notify;

        private @UI UICheckBox notification;

        private @UI UICheckBox external;

        private @UI UIComboBox<Sound> sound;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            notification.model(notify.notification);
            external.model(notify.external);
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
            duration.values(I.signalRange(2, 30, 2).map(Duration::ofSeconds))
                    .model(notificator.desktopDuration)
                    .text(message::desktopDuration);
            position.values(DesktopPosition.class).model(notificator.desktopPosition);
        }
    }

    /**
     * @version 2018/08/27 21:10:05
     */
    @SuppressWarnings("unused")
    private static class Lang implements Extensible {

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
         * @version 2018/08/27 21:11:28
         */
        private static class Lang_ja extends Lang {

            /**
             * {@inheritDoc}
             */
            @Override
            String desktopDuration(Duration duration) {
                return duration.getSeconds() + "秒";
            }
        }
    }
}
