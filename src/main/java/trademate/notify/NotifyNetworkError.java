/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.notify;

import cointoss.util.NetworkError;
import kiss.I;
import kiss.Variable;
import viewtify.ui.toast.Toast;
import viewtify.ui.toast.Toastable;

public class NotifyNetworkError implements Toastable<NetworkError> {

    private static final Variable<String> OpenURL = I.translate("Open the errored URL");

    /**
     * {@inheritDoc}
     */
    @Override
    public void show(NetworkError message) {
        Toast.show(message.service + "\n" + I.translate(message.kind.message) + "\n[" + OpenURL + "](" + message.getLocation() + ")");
    }
}
