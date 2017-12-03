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

import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2017/11/26 13:44:20
 */
@Manageable(lifestyle = Singleton.class)
class OrderManager {

    /** The order set management. */
    private final ObservableList<OrderSet> managed = FXCollections.observableList(new LinkedList());

    /**
     * Request order to server.
     * 
     * @param order
     */
    void request(OrderSet set) {
        if (set.sub.size() == 1) {

        } else {

        }
    }
}
