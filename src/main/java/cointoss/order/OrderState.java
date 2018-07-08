/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

/**
 * @version 2018/07/08 10:40:49
 */
public enum OrderState {
    INIT, REQUESTING, ACTIVE, COMPLETED, CANCELED, EXPIRED, REJECTED;
}