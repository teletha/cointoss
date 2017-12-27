/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import static cointoss.Order.State.*;
import static java.util.concurrent.TimeUnit.*;

import java.math.RoundingMode;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javafx.scene.control.Spinner;
import javafx.scene.input.ScrollEvent;
import javafx.scene.media.AudioClip;

import cointoss.Order;
import cointoss.Order.Quantity;
import cointoss.Order.State;
import cointoss.Side;
import cointoss.util.Num;
import kiss.WiseBiConsumer;
import trademate.NotificationType;
import trademate.TradingView;
import viewtify.UI;
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
public class OrderBuilder extends View {

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

    /** UI */
    private @UI UIText orderSize;

    /** UI */
    private @UI UISpinner<Num> orderSizeAmount;

    /** UI */
    private @UI UIText orderPrice;

    /** UI */
    private @UI UISpinner<Num> orderPriceAmount;

    /** UI */
    private @UI UISpinner<Integer> orderDivideSize;

    /** UI */
    private @UI UISpinner<Integer> orderDivideIntervalAmount;

    /** UI */
    private @UI UISpinner<Num> optimizeThreshold;

    /** UI */
    private @UI UIText orderPriceInterval;

    /** UI */
    private @UI UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private @UI UIButton orderLimitLong;

    /** UI */
    private @UI UIButton orderLimitShort;

    /** UI */
    private @UI UIComboBox<Quantity> orderQuantity;

    /** UI */
    private @UI TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(Num.of("0.01"), Num.of("0.1"), Num.ONE).initial(Num.ONE);

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(Num.ONE, Num.HUNDRED, Num.THOUSAND, Num.of(10000)).initial(Num.ONE);

        orderDivideSize.values(IntStream.range(1, 31).boxed()).initial(1);
        orderDivideIntervalAmount.values(IntStream.range(0, 5).boxed())
                .initial(0)
                .disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        optimizeThreshold.values(Num.range(0, 20)).initial(Num.ZERO);
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

        orderQuantity.values(Quantity.values()).initial(Quantity.GoodTillCanceled).observe(v -> {
            System.out.println(v);

            try {
                AudioClip audioClip = new AudioClip(ClassLoader.getSystemResource("sound/Start.m4a").toExternalForm());
                audioClip.play();
                System.out.println("PLAY");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(v);
        });
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
        OrderSet set = new OrderSet();

        // ========================================
        // Create Model
        // ========================================
        Num size = orderSize.valueOr(Num.ZERO);
        Num price = orderPrice.valueOr(Num.ZERO);
        int divideSize = orderDivideSize.value();
        int increaseInterval = orderDivideIntervalAmount.value();
        Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);
        Quantity quantity = orderQuantity.value();
        long group = System.nanoTime();

        for (int i = 0; i < divideSize; i++) {
            Num optimizedSize = increaseInterval == 0 ? Num.ZERO
                    : Num.of(i).divide(increaseInterval).scale(0, RoundingMode.FLOOR).multiply(orderSizeAmount.value());
            Num optimizedPrice = view.board.book.computeBestPrice(side, price, optimizeThreshold.value(), Num.of(2));

            Order order = Order.limit(side, size.plus(optimizedSize), optimizedPrice).type(quantity);
            order.state.set(State.REQUESTING);
            order.state.observe().take(CANCELED, COMPLETED).take(1).to(() -> set.sub.remove(order));
            order.group = group;

            set.sub.add(order);

            price = optimizedPrice.plus(priceInterval);
        }

        // ========================================
        // Create View Model
        // ========================================
        view.orders.createOrderItem(set);
        view.positions.createPositionItem(set);

        // ========================================
        // Request to Server
        // ========================================
        for (Order order : set.sub) {
            view.console.info("Request order [{}]", order);

            Viewtify.inWorker(() -> {
                view.market().request(order).to(o -> {
                    view.console.info("Accept order [{}]", o);
                }, e -> {
                    view.console.notify(NotificationType.OrderFailed, "Reject order [{}]", order, e);
                });
            });
        }
    }

}
