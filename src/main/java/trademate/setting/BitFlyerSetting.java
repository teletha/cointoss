/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import cointoss.market.bitflyer.BitFlyerAccount;
import kiss.I;
import kiss.Variable;
import viewtify.style.FormStyles;
import viewtify.ui.UICheckSwitch;
import viewtify.ui.UIText;
import viewtify.ui.ViewDSL;
import viewtify.ui.view.PreferenceViewBase;

public class BitFlyerSetting extends PreferenceViewBase {

    /** The account info. */
    private final BitFlyerAccount account = I.make(BitFlyerAccount.class);

    private UIText<String> apiKey;

    private UIText<String> apiSecret;

    private UIText<String> loginId;

    private UIText<String> loginPassword;

    private UICheckSwitch loginBackground;

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable<String> title() {
        return en("BitFlyer");
    }

    class view extends ViewDSL implements FormStyles, SettingStyles {

        {
            $(vbox, () -> {
                title(en("Usage of Public API"), en("Please get API key and API secret to use the public API provided by [BitFlyer](https://lightning.bitflyer.com/developer)."));
                form("API Key", apiKey);
                form("API Secret", apiSecret);

                title(en("Usage of Private API"), en("We will try to speed up trading by using private API.\nTradeMate acquires the account specific infomation(e.g. session id) by logging in automatically."), en("WARNING : This setting will allow all operations on your account."));
                form("Login ID", loginId);
                form(en("Password"), loginPassword);
                form(en("Login explicitly"), FormStyles.InputMin, loginBackground);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        apiKey.sync(account.apiKey).masking(true);
        apiSecret.sync(account.apiSecret).masking(true);
        loginId.sync(account.loginId);
        loginPassword.sync(account.loginPassword).masking(true);
    }
}