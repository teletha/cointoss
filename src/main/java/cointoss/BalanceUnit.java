/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/08/15 16:31:11
 */
public class BalanceUnit {

    /** The currency code. */
    public String currency_code;

    /** The total currency amount. */
    public Decimal amount;

    /** The available currency amount. */
    public Decimal available;
}
