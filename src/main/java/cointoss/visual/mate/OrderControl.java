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

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Cell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ScrollEvent;

import cointoss.MarketBackend;
import cointoss.Order;
import cointoss.Order.Quantity;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import kiss.WiseBiConsumer;
import viewtify.User;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
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

    private @FXML UISpinner<Num> orderSizeAmount;

    private @FXML UIText orderPrice;

    private @FXML UISpinner<Num> orderPriceAmount;

    private @FXML UISpinner<Integer> orderDivideSize;

    private @FXML UIText orderPriceInterval;

    private @FXML UIButton orderLimitLong;

    private @FXML UIButton orderLimitShort;

    private @FXML UIComboBox<Quantity> orderQuantity;

    private @FXML TableView<Order> requestedOrders;

    private @FXML TableColumn<Order, String> requestedOrdersSide;

    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(Num.of("0.01"), Num.of("0.1"), Num.ONE).initial(Num.ONE);

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(Num.ONE, Num.HUNDRED, Num.THOUSAND).initial(Num.ONE);

        orderDivideSize.values(1, 2, 4, 5, 8, 10).initial(1);
        orderPriceInterval.initial("0").parent().disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid().or(orderPrice.isInvalid()));

        orderLimitLong.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.BUY).to(this::requestOrder);
        orderLimitShort.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.SELL).to(this::requestOrder);

        orderQuantity.values(Quantity.values()).initial(Quantity.GoodTillCanceled);

        ObservableList<TableColumn<Order, ?>> columns = requestedOrders.getColumns();
        columns.get(1).setCellValueFactory(e -> new SimpleObjectProperty(e.getValue().side()));
        columns.get(2).setCellValueFactory(e -> new SimpleObjectProperty(e.getValue().size()));
        columns.get(3).setCellValueFactory(e -> new SimpleObjectProperty(e.getValue().price()));

        requestedOrders.setCellFactory(c -> new Cell());
        requestedOrders.setItems(orders);

        // requestedOrdersDate.setCellValueFactory(e -> e.);
        // requestedOrdersDate.setCellFactory(e -> new TableCell());
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
            Num size = orderSize.valueOr(Num.ZERO);
            Num price = orderPrice.valueOr(Num.ZERO);
            Integer divideSize = orderDivideSize.value();
            Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);
            Quantity quantity = orderQuantity.value();

            for (int i = 0; i < divideSize; i++) {
                Order order = Order.limit(side, size, price).type(quantity);

                if (order.isLimit()) {
                    Viewtify.inUI(() -> {
                        orders.add(order);
                    });

                    service.request(order).on(Viewtify.UIThread).to(id -> {
                        System.out.println(id);
                    }, e -> {
                        e.printStackTrace();
                    });
                }
                price = price.plus(priceInterval);
            }
        });
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

    /**
     * @version 2017/11/19 15:40:13
     */
    private class Cell extends TableCell<Order, String> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                System.out.println(item);
            }
        }
    }
}
