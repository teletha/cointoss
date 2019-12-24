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

import cointoss.market.bitflyer.BitFlyerAccount;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import transcript.Lang;
import transcript.Transcript;
import viewtify.ui.UI;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.helper.User;

@Managed(value = Singleton.class)
public class BitFlyerSetting extends View {

    /** The account info. */
    private final BitFlyerAccount account = I.make(BitFlyerAccount.class);

    private UIPassword apiKey;

    private UIPassword apiSecret;

    private UIText loginId;

    private UIPassword loginPassword;

    private UIPassword accountId;

    private UIPassword accountToken;

    private UICheckBox loginBackground;

    class view extends UI {
        Transcript PublicAPIDescription = Transcript
                .en("Please get API key and API secret to use the public API provided by [BitFlyer](https://lightning.bitflyer.jp/developer).");

        Transcript PrivateAPITitle = Transcript.en("Usage of Private API");

        Transcript PrivateAPIDescription = Transcript
                .en("We will try to speed up trading by using private API.\nTradeMate acquires the account specific infomation(e.g. session id) by logging in automatically.");

        Transcript PrivateAPIWarning = Transcript.en("WARNING : This setting will allow all operations on your account.");

        Transcript LoginExplicitly = Transcript.en("Login explicitly");

        {
            $(vbox, Root, () -> {
                $(vbox, Block, () -> {
                    label("BitFlyer", Heading);
                    label(PublicAPIDescription, Description);
                    form("API Key", apiKey);
                    form("API Secret", apiSecret);

                    label(PrivateAPITitle, Heading);
                    label(PrivateAPIDescription, Description);
                    label(PrivateAPIWarning, Description, Warning);
                    form("Login ID", loginId);
                    form(en("Password"), loginPassword);
                    form(en("Account ID"), accountId);
                    form(en("Account Token"), accountToken);
                    form(LoginExplicitly, loginBackground);
                });
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        apiKey.sync(account.apiKey);
        apiSecret.sync(account.apiSecret);
        loginId.sync(account.loginId);
        loginPassword.sync(account.loginPassword);
        accountId.sync(account.accountId);
        accountToken.sync(account.accountToken);

        loginBackground.when(User.Action).to(() -> {
            if (Lang.current() == Lang.EN) {
                Lang.JA.setDefault();
            } else {
                Lang.EN.setDefault();
            }
        });
    }
}
