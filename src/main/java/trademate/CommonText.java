/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import kiss.I;
import kiss.Variable;

public interface CommonText {

    Variable<String> Side = I.translate("Side");

    Variable<String> Buy = I.translate("Buy");

    Variable<String> Sell = I.translate("Sell");

    Variable<String> Long = I.translate("Long");

    Variable<String> Short = I.translate("Short");

    Variable<String> Price = I.translate("Price");

    Variable<String> Amount = I.translate("Amount");

    Variable<String> Profit = I.translate("Profit");

    Variable<String> Date = I.translate("Date");

    Variable<String> Cancel = I.translate("Cancel");

    Variable<String> OpenPrice = I.translate("Open");

    Variable<String> ClosePrice = I.translate("Close");

    Variable<String> HighPrice = I.translate("High");

    Variable<String> LowPrice = I.translate("Low");

    Variable<String> Market = I.translate("Market");
}