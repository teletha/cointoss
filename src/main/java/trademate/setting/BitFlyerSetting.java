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
import viewtify.ui.UICheckBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UIText;

/**
 * @version 2018/08/27 18:53:30
 */
@Manageable(lifestyle = Singleton.class)
public class BitFlyerSetting extends View<Lang> {

    private UIPassword apiKey;

    private UIPassword apiSecret;

    private UICheckBox allowAccountAccess;

    private UIText loginId;

    private UIPassword password;

    private UIPassword accountId;

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
                    vbox(Block, () -> {
                        label("BitFlyer", Heading);
                        label($.publicAPIDescription(), Description);
                        hbox(FormRow, () -> {
                            label("API Key", FormLabel);
                            $(apiKey, FormInput);
                        });
                        hbox(FormRow, () -> {
                            label("API Secret", FormLabel);
                            $(apiSecret, FormInput);
                        });

                        label("非公開APIの利用", Heading);
                        label($.privateAPIDescription(), Description);
                        label($.privateAPIWarning(), Description, Warning);
                        hbox(FormRow, () -> {
                            label("Login ID", FormLabel);
                            $(loginId, FormInput);
                        });
                        hbox(FormRow, () -> {
                            label("Password", FormLabel);
                            $(password, FormInput);
                        });
                        hbox(FormRow, () -> {
                            label("Account ID", FormLabel);
                            $(accountId, FormInput);
                        });
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
         * Desciption for private API.
         * 
         * @return
         */
        String privateAPIDescription() {
            return "";
        }

        /**
         * Warning for private API.
         * 
         * @return
         */
        String privateAPIWarning() {
            return "";
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

            /**
             * {@inheritDoc}
             */
            @Override
            String privateAPIDescription() {
                return "非公開APIを利用して取引の高速化を図ります。\r\nブラウザを使用して自動でログインを行いアカウント固有のIDやセッション情報を取得します。";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String privateAPIWarning() {
                return "この設定を行うとあなたのアカウントに対する全ての操作を許可することになります。ご承知の上、使用してください。";
            }
        }
    }
}
