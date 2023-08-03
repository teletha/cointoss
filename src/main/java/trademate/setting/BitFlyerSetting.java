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
import kiss.Managed;
import kiss.Singleton;
import viewtify.style.FormStyles;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

@Managed(value = Singleton.class)
public class BitFlyerSetting extends View {

    /** The account info. */
    private final BitFlyerAccount account = I.make(BitFlyerAccount.class);

    private UIText<String> apiKey;

    private UIText<String> apiSecret;

    private UIText<String> loginId;

    private UIText<String> loginPassword;

    private UICheckBox loginBackground;

    class view extends ViewDSL implements FormStyles, SettingStyles {

        {
            $(vbox, () -> {
                $(vbox, Block, () -> {
                    label("BitFlyer", Heading);
                    label(en("Please get API key and API secret to use the public API provided by [BitFlyer](https://lightning.bitflyer.com/developer)."), Description);
                    form("API Key", apiKey);
                    form("API Secret", apiSecret);

                    label(en("Usage of Private API"), Heading);
                    label(en("We will try to speed up trading by using private API.\nTradeMate acquires the account specific infomation(e.g. session id) by logging in automatically."), Description);
                    label(en("WARNING : This setting will allow all operations on your account."), Description, Warning);
                    form("Login ID", loginId);
                    form(en("Password"), loginPassword);
                    form(en("Login explicitly"), loginBackground);
                });
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