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

import cointoss.Direction;
import cointoss.util.arithmetic.Num;

public abstract class OrderResponse {

    public final String id;

    /**
     * @param id
     */
    protected OrderResponse(String id) {
        this.id = id;
    }

    /**
     * 
     */
    public class Accepted extends OrderResponse {

        public final Direction side;

        public final Num size;

        public final Num price;

        public Accepted(String id, Direction side, Num size, Num price) {
            super(id);
            this.side = side;
            this.size = size;
            this.price = price;
        }
    }

    /**
     * 
     */
    public class Rejected extends OrderResponse {

        public final String reason;

        public Rejected(String id, String reason) {
            super(id);
            this.reason = reason;
        }
    }

    /**
     * 
     */
    public class Cancelled extends OrderResponse {
        public Cancelled(String id) {
            super(id);
        }
    }

    /**
     * 
     */
    public class Executed extends OrderResponse {

        public final Num size;

        public final Num price;

        public Executed(String id, Num size, Num price) {
            super(id);
            this.size = size;
            this.price = price;
        }
    }
}
