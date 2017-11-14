/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;

import org.controlsfx.validation.ValidationSupport;

import cointoss.util.Num;
import cointoss.visual.mate.View;

/**
 * @version 2017/11/14 23:47:09
 */
public class OrderControl extends View {

    private @FXML TextField orderSize;

    private @FXML Spinner<Num> orderSizeAmount;

    private @FXML TextField orderPrice;

    private @FXML Spinner<Num> orderPriceAmount;

    private @FXML Spinner<Integer> orderDivideSize;

    private @FXML TextField orderPriceInterval;

    private @FXML Button orderBuy;

    private @FXML Button orderSell;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.setText("0");
        orderSize.setOnScroll(changeByWheel(orderSize, orderSizeAmount));
        orderSizeAmount.setValueFactory(spinnerV(Num.of("0.01"), Num.of("0.1"), Num.ONE));

        orderPrice.setText("0");
        orderPrice.setOnScroll(changeByWheel(orderPrice, orderPriceAmount));
        orderPriceAmount.setValueFactory(spinnerV(Num.ONE, Num.HUNDRED, Num.THOUSAND));

        orderDivideSize.setValueFactory(spinnerV(1, 2, 4, 5, 8, 10));
        orderPriceInterval.getParent().disableProperty().bind(orderDivideSize.valueProperty().isEqualTo(1));

        // validate order condition
        ValidationSupport support = new ValidationSupport();
        support.registerValidator(orderSize, false, requirePositiveNumber());
        support.registerValidator(orderPrice, false, requirePositiveNumber());

        orderBuy.getParent().disableProperty().bind(support.invalidProperty());
    }

    /**
     * Support wheel change.
     * 
     * @param source
     * @param amount
     * @return
     */
    private EventHandler<? super ScrollEvent> changeByWheel(TextField source, Spinner amount) {
        return e -> {
            System.out.println(source + "  " + amount);
        };
    }

    /**
     * @version 2017/11/15 0:58:48
     */
    private static class PriceValueFactory extends SpinnerValueFactory<Num> {

        /** The amount of step. */
        private final Num amount;

        /**
         * @param amount
         */
        private PriceValueFactory(String amount) {
            this.amount = Num.of(amount);
            setValue(Num.ZERO);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void decrement(int steps) {
            Num value = getValue().minus(amount.multiply(steps));
            value = Num.max(value, Num.ZERO);

            setValue(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void increment(int steps) {
            Num value = getValue().plus(amount.multiply(steps));
            value = Num.min(value, Num.MAX);

            setValue(value);
        }

    }
}
