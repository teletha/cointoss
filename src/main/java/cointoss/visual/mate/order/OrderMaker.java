/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate.order;

import static java.util.concurrent.TimeUnit.*;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.input.ScrollEvent;

import cointoss.MarketBackend;
import cointoss.Order;
import cointoss.Order.Quantity;
import cointoss.OrderState;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import cointoss.visual.mate.TradingView;
import kiss.I;
import kiss.WiseBiConsumer;
import viewtify.User;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;

/**
 * @version 2017/11/27 23:21:48
 */
public class OrderMaker extends View {

    private Predicate<UIText> positiveNumber = ui -> {
        try {
            return Num.of(ui.text()).isPositive();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    private Predicate<UIText> negativeNumber = ui -> {
        try {
            return Num.of(ui.text()).isNegative();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    /** The backend service. */
    private final MarketBackend service = BitFlyer.FX_BTC_JPY.service();

    /** The backend service. */
    private final OrderManager manager = I.make(OrderManager.class);

    /** UI */
    private @FXML UIText orderSize;

    /** UI */
    private @FXML UISpinner<Num> orderSizeAmount;

    /** UI */
    private @FXML UIText orderPrice;

    /** UI */
    private @FXML UISpinner<Num> orderPriceAmount;

    /** UI */
    private @FXML UISpinner<Integer> orderDivideSize;

    /** UI */
    private @FXML UIText orderPriceInterval;

    /** UI */
    private @FXML UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private @FXML UIButton orderLimitLong;

    /** UI */
    private @FXML UIButton orderLimitShort;

    /** UI */
    private @FXML UIComboBox<Quantity> orderQuantity;

    /** UI */
    private @FXML TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(Num.of("0.01"), Num.of("0.1"), Num.ONE).initial(Num.ONE);

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(Num.ONE, Num.HUNDRED, Num.THOUSAND).initial(Num.ONE);

        orderDivideSize.values(IntStream.range(1, 31).boxed()).initial(1);
        orderPriceInterval.initial("0")
                .when(User.Scroll, changeBy(orderPriceIntervalAmount.ui))
                .require(positiveNumber)
                .parent()
                .disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        orderPriceIntervalAmount.values(Num.TEN, Num.HUNDRED, Num.THOUSAND).initial(Num.TEN);

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid().or(orderPrice.isInvalid()));

        orderLimitLong.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.BUY).to(this::requestOrder);
        orderLimitShort.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.SELL).to(this::requestOrder);

        orderQuantity.values(Quantity.values()).initial(Quantity.GoodTillCanceled);
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
        Viewtify.inWorker(() -> {
            // ========================================
            // Create Model
            // ========================================
            OrderSet set = new OrderSet();

            Num size = orderSize.valueOr(Num.ZERO);
            Num price = orderPrice.valueOr(Num.ZERO);
            int divideSize = orderDivideSize.value();
            Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);
            Quantity quantity = orderQuantity.value();

            for (int i = 0; i < divideSize; i++) {
                Order order = Order.limit(side, size, price).type(quantity);
                order.child_order_state.set(OrderState.REQUESTING);

                set.sub.add(order);

                price = price.plus(priceInterval);
            }

            // ========================================
            // Create View Model
            // ========================================
            view.catalog.add(set);

            // ========================================
            // Request to Server
            // ========================================
            for (Order order : set.sub) {
                view.console.write("Request order [{}]", order);

                service.request(order).to(id -> {
                    order.child_order_acceptance_id = id;
                    order.child_order_state.set(OrderState.ACTIVE);

                    view.console.write("Accept order [{}]", order);
                });
            }
        });
    }

}
