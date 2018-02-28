/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.okcoin;

/**
 * @version 2017/07/29 0:23:36
 */
public class OKCoinExecution {

    public long date;

    public long date_ms;

    public float price;

    public float amount;

    public long tid;

    public String type;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OKCoinExecution [date=" + date + ", date_ms=" + date_ms + ", price=" + price + ", amount=" + amount + ", tid=" + tid + ", type=" + type + "]";
    }
}
