/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import transcript.Transcript;

/**
 * 
 */
public interface CommonText {

    Transcript SiDe = Transcript.en("Side");

    Transcript Buy = Transcript.en("Buy");

    Transcript Sell = Transcript.en("Sell");

    Transcript Long = Transcript.en("Long");

    Transcript Short = Transcript.en("Short");

    Transcript Price = Transcript.en("Price");

    Transcript Amount = Transcript.en("Amount");

    Transcript Profit = Transcript.en("Profit");

    Transcript Date = Transcript.en("Date");

    Transcript Cancel = Transcript.en("Cancel");

    Transcript OpenPrice = Transcript.en("Open");

    Transcript ClosePrice = Transcript.en("Close");

    Transcript HighPrice = Transcript.en("High");

    Transcript LowPrice = Transcript.en("Low");
}
