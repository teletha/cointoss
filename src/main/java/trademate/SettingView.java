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

import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import trademate.Notificator.Type;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIFileDialog;

/**
 * @version 2017/12/15 9:30:13
 */
@Manageable(lifestyle = Singleton.class)
public class SettingView extends View {

    private Notificator notificator = I.make(Notificator.class);

    private @UI NotificationSetting longTrend;

    private @UI NotificationSetting shortTrend;

    private @UI NotificationSetting execution;

    private @UI NotificationSetting orderFailed;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        longTrend.type = notificator.longTrend;
        shortTrend.type = notificator.shortTrend;
        execution.type = notificator.execution;
        orderFailed.type = notificator.orderFailed;
    }

    /**
     * @version 2017/12/15 9:37:47
     */
    private class NotificationSetting extends View {

        private Type type;

        private @UI UICheckBox notification;

        private @UI UIComboBox<String> sound;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            notification.model(type.notification);
            sound.values("Select File", "None", "System").initial("None").when(User.Action, e -> {
                if (sound.index() == 0) {
                    UIFileDialog.title("Select sound file.").filter("Sound Files", "*.aac", "*.mp3").select().to(path -> {
                        System.out.println(path);
                    });
                }
            });
        }
    }
}
