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

import javafx.scene.Node;

import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import stylist.value.Color;
import trademate.Theme;
import trademate.setting.AppearanceSetting.Lang;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIColorPicker;
import viewtify.ui.View;
import viewtify.ui.helper.User;
import viewtify.util.FXUtils;

/**
 * @version 2018/09/10 20:28:10
 */
@Manageable(lifestyle = Singleton.class)
public class AppearanceSetting extends View<Lang> {

    private Theme theme = I.make(Theme.class);

    private UIColorPicker buy;

    private UIColorPicker sell;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, Root, () -> {
                    $(vbox, Block, () -> {
                        label($::color, Heading);
                        $(hbox, FormRow, () -> {
                            label($::buy, FormLabel);
                            $(buy, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label($::sell, FormLabel);
                            $(sell, FormInput);
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
        buy.model(theme.Long).when(User.Action, c -> {
            Color color = FXUtils.color(buy.value());
            Node ui = Viewtify.view().ui();
            System.out.println(ui + "   " + color.toRGB());
            ui.setStyle("-fx-background:" + color.toRGB() + ";");
        });
    }

    /**
     * @version 2018/09/10 20:18:55
     */
    static class Lang implements Extensible {

        String color() {
            return "Colors";
        }

        String buy() {
            return "Long";
        }

        String sell() {
            return "Short";
        }

        /**
         * @version 2018/09/10 20:29:01
         */
        private static class Lang_ja extends Lang {

            /**
             * {@inheritDoc}
             */
            @Override
            String color() {
                return "色";
            }
        }
    }
}
