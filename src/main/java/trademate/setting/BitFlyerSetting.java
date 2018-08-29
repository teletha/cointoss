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
import trademate.setting.BitFlyerSetting.Lang;
import viewtify.View;
import viewtify.dsl.UIDefinition;
import viewtify.ui.UILabel;
import viewtify.ui.UIPassword;

/**
 * @version 2018/08/27 18:53:30
 */
@Manageable(lifestyle = Singleton.class)
public class BitFlyerSetting extends View<Lang> {

    private UILabel publicAPIDescription;

    private UIPassword apiKey;

    private UIPassword apiSecret;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                vbox(Root, () -> {
                    label("BitFlyer", Heading);
                    $(publicAPIDescription, Description);
                    hbox(FormRow, () -> {
                        label("API Key", FormLabel);
                        $(apiKey, FormInput);
                    });
                    hbox(FormRow, () -> {
                        label("API Secret", FormLabel);
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
    static class Lang implements Extensible {

        /**
         * Label for API key
         * 
         * @return
         */
        String publicAPIDescription() {
            return "Please get API key and API secret to use the public API provided by [BitFlyer](https://lightning.bitflyer.jp/developer).";
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
