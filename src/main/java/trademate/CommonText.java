/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import kiss.Transcript;

/**
 * 
 */
public interface CommonText {

    Transcript SiDe = new Transcript("Side");

    Transcript Buy = new Transcript("Buy");

    Transcript Sell = new Transcript("Sell");

    Transcript Long = new Transcript("Long");

    Transcript Short = new Transcript("Short");

    Transcript Price = new Transcript("Price");

    Transcript Amount = new Transcript("Amount");

    Transcript Profit = new Transcript("Profit");

    Transcript Date = new Transcript("Date");

    Transcript Cancel = new Transcript("Cancel");

    Transcript OpenPrice = new Transcript("Open");

    Transcript ClosePrice = new Transcript("Close");

    Transcript HighPrice = new Transcript("High");

    Transcript LowPrice = new Transcript("Low");
}