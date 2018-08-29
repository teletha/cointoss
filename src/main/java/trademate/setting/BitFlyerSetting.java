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

import kiss.Extensible;
import kiss.Manageable;
import kiss.Singleton;
import viewtify.UI;
import viewtify.View;
import viewtify.fxml.FXML;
import viewtify.ui.UILabel;
import viewtify.ui.UIPassword;

/**
 * @version 2018/08/27 18:53:30
 */
@Manageable(lifestyle = Singleton.class)
public class BitFlyerSetting extends View {

    /** The message resource. */
    private final Lang M = localizeBy(Lang.class);

    private @UI UILabel publicAPIDescription;

    private @UI UILabel apiKeyLabel;

    private @UI UIPassword apiKey;

    private @UI UILabel apiSecretLabel;

    private @UI UIPassword apiSecret;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        System.out.println("Market View");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FXML defineUI() {
        return new FXML() {
            {
                vbox(Root, () -> {
                    label("BitFlyer", Heading);
                    $(publicAPIDescription, Description);
                    hbox(FormRow, () -> {
                        $(apiKeyLabel, FormLabel);
                        $(apiKey, FormInput);
                    });
                    hbox(FormRow, () -> {
                        $(apiSecretLabel, FormLabel);
                        $(apiSecret, FormInput);
                    });
                });
            }
        };
    }

    /**
     * @version 2018/08/29 3:52:37
     */
    @SuppressWarnings("unused")
    @Manageable(lifestyle = Singleton.class)
    private static class Lang implements Extensible {

        /**
         * Label for API key
         * 
         * @return
         */
        String publicAPIDescription() {
            return "Please get API key and API secret to use the public API provided by [BitFlyer](https://lightning.bitflyer.jp/developer).";
        }

        String apiKeyLabel() {
            return "API Key";
        }

        String apiSecretLabel() {
            return "API Secret";
        }

        /**
         * @version 2018/08/29 3:53:49
         */
        private static class Lang_ja extends Lang {

            /**
             * {@inheritDoc}
             */
            @Override
            String publicAPIDescription() {
                return "[BitFlyer](https://lightning.bitflyer.jp/developer)の提供する公開APIを利用するためにAPIキーとAPIシークレットを取得してください。";
            }
        }
    }
}
