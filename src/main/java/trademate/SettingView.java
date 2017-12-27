/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import kiss.Manageable;
import kiss.Singleton;
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIText;

/**
 * @version 2017/12/15 9:30:13
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View {

    private @UI Notification longTrend;

    private @UI Notification shortTrend;

    private @UI Notification execution;

    private @UI Notification orderFailed;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    /**
     * @param type
     * @return
     */
    public boolean shouldNotify(NotificationType type) {
        return by(type).notification.isSelected().get();
    }

    private Notification by(NotificationType type) {
        switch (type) {
        case OrderAccepted:
            return longTrend;

        case OrderFailed:
            return orderFailed;
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * @version 2017/12/15 9:37:47
     */
    private static class Notification extends View {

        private @UI UICheckBox notification;

        private @UI UICheckBox sound;

        private @UI UIText soundFile;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            notification.initial(true);
            sound.initial(false);
            soundFile.disableWhen(sound.isNotSelected());
        }
    }
}
