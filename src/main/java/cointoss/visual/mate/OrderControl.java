/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import static java.util.concurrent.TimeUnit.*;

import java.util.function.Predicate;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.ScrollEvent;

import cointoss.MarketBackend;
import cointoss.Order;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import kiss.WiseBiConsumer;
import viewtify.User;
import viewtify.View;
import viewtify.ui.UIButton;
import viewtify.ui.UIText;

/**
 * @version 2017/11/14 23:47:09
 */
public class OrderControl extends View {

    private Predicate<UIText> positiveNumber = ui -> {
        try {
            return Num.of(ui.text()).isPositive();
        } catch (NumberFormatException e) {
            return false;
        }
    };

    private Predicate<UIText> negativeNumber = ui -> {
        try {
            return Num.of(ui.text()).isNegative();
        } catch (NumberFormatException e) {
            return false;
        }
    };

    /** The backend service. */
    private final MarketBackend service = BitFlyer.FX_BTC_JPY.service();

    private @FXML UIText orderSize;

    private @FXML Spinner<Num> orderSizeAmount;

    private @FXML UIText orderPrice;

    private @FXML Spinner<Num> orderPriceAmount;

    private @FXML Spinner<Integer> orderDivideSize;

    private @FXML UIText orderPriceInterval;

    private @FXML UIButton orderLimitBuy;

    private @FXML UIButton orderLimitSell;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.text("0").when(User.Scroll, changeBy(orderSizeAmount)).require(positiveNumber);
        orderSizeAmount.setValueFactory(spinnerV(Num.of("0.01"), Num.of("0.1"), Num.ONE));

        orderPrice.text("0").when(User.Scroll, changeBy(orderPriceAmount)).require(positiveNumber);
        orderPriceAmount.setValueFactory(spinnerV(Num.ONE, Num.HUNDRED, Num.THOUSAND));

        orderDivideSize.setValueFactory(spinnerV(1, 2, 4, 5, 8, 10));
        orderPriceInterval.parent().disableWhen(orderDivideSize.valueProperty().isEqualTo(1));

        // validate order condition
        orderLimitBuy.parent().disableWhen(orderSize.isInvalid().or(orderPrice.isInvalid()));

        orderLimitBuy.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.BUY).to(this::requestOrder);
        orderLimitSell.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.SELL).to(this::requestOrder);
    }

    /**
     * Support wheel change.
     * 
     * @param source
     * @param amount
     * @return
     */
    private WiseBiConsumer<ScrollEvent, UIText> changeBy(Spinner<Num> spinner) {
        return (e, ui) -> {
            Num current = Num.of(ui.text());
            double deltaY = e.getDeltaY();

            if (deltaY > 0) {
                // increment
                ui.text(current.plus(spinner.getValue()));
            } else if (deltaY < 0) {
                // decrement
                ui.text(current.minus(spinner.getValue()));
            }
        };
    }

    /**
     * Request order by API.
     * 
     * @param side
     * @return
     */
    private void requestOrder(Side side) {
        Num size = orderSize.valueOr(Num.ZERO);
        Num price = orderPrice.valueOr(Num.ZERO);
        Integer divideSize = orderDivideSize.getValue();
        Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);

        System.out.println("OK " + side + "  " + size + "  " + price + "  " + divideSize + "  " + priceInterval);

        for (int i = 0; i < divideSize; i++) {
            service.request(Order.limit(side, size, price)).to(id -> {
                System.out.println("SUCCESS " + id);
            });
            price = price.plus(priceInterval);
        }
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
