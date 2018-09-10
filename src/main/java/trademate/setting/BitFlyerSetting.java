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

import cointoss.market.bitflyer.BitFlyerAccount;
import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import trademate.setting.BitFlyerSetting.Lang;
import viewtify.ui.UI;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIPassword;
import viewtify.ui.UIText;
import viewtify.ui.View;

/**
 * @version 2018/09/06 23:46:18
 */
@Manageable(lifestyle = Singleton.class)
public class BitFlyerSetting extends View<Lang> {

    /** The account info. */
    private final BitFlyerAccount account = I.make(BitFlyerAccount.class);

    private UIPassword apiKey;

    private UIPassword apiSecret;

    private UIText loginId;

    private UIPassword loginPassword;

    private UIPassword accountId;

    private UICheckBox loginBackground;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, Root, () -> {
                    $(vbox, Block, () -> {
                        label("BitFlyer", Heading);
                        label($.publicAPIDescription(), Description);
                        $(hbox, FormRow, () -> {
                            label("API Key", FormLabel);
                            $(apiKey, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label("API Secret", FormLabel);
                            $(apiSecret, FormInput);
                        });

                        label($.privateAPITitle(), Heading);
                        label($.privateAPIDescription(), Description);
                        label($.privateAPIWarning(), Description, Warning);
                        $(hbox, FormRow, () -> {
                            label("Login ID", FormLabel);
                            $(loginId, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label("Password", FormLabel);
                            $(loginPassword, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label("Account ID", FormLabel);
                            $(accountId, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label($.loginExplicitly(), FormLabel);
                            $(loginBackground, FormInput);
                        });
                    });
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        apiKey.model(account.apiKey);
        apiSecret.model(account.apiSecret);
        loginId.model(account.loginId);
        loginPassword.model(account.loginPassword);
        accountId.model(account.accountId);
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

        String privateAPITitle() {
            return "Usage of Private API";
        }

        /**
         * Desciption for private API.
         * 
         * @return
         */
        String privateAPIDescription() {
            return "We will try to speed up trading by using private API.\r\nTradeaMate acquires the account specific infomation(e.g. session id) by logging in automatically.";
        }

        /**
         * Warning for private API.
         * 
         * @return
         */
        String privateAPIWarning() {
            return "WARNING : This setting will allow all operations on your account.";
        }

        /**
         * Description label for login.
         * 
         * @return
         */
        String loginExplicitly() {
            return "Login explicitly";
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
            String privateAPITitle() {
                return "非公開APIの利用";
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
                return "注意 : この設定を行うとあなたのアカウントに対する全ての操作を許可することになります。";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String loginExplicitly() {
                return "ログイン画面を表示";
            }
        }
    }
}
