/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import cointoss.order.OrderResponse.Kind;
import cointoss.util.arithmetic.Num;

public record OrderResponse(Kind type, String id, Num size, Num price) {

    /**
     * Shorthand for {@link OrderResponseType#Accepted}
     * 
     * @param id
     * @return
     */
    public static OrderResponse accepted(String id) {
        return new OrderResponse(Kind.Accepted, id, null, null);
    }

    /**
     * Shorthand for {@link OrderResponseType#Rejected}
     * 
     * @param id
     * @return
     */
    public static OrderResponse rejected(String id) {
        return new OrderResponse(Kind.Rejected, id, null, null);
    }

    /**
     * Shorthand for {@link OrderResponseType#Cancelled}
     * 
     * @param id
     * @return
     */
    public static OrderResponse cancelled(String id) {
        return new OrderResponse(Kind.Cancelled, id, null, null);
    }

    /**
     * Shorthand for {@link OrderResponseType#Executed}
     * 
     * @param id
     * @return
     */
    public static OrderResponse executed(String id, Num size, Num price) {
        return new OrderResponse(Kind.Executed, id, size, price);
    }

    /**
     * 
     */
    public enum Kind {
        Accepted, Rejected, Executed, Cancelled;
    }
}
