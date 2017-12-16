/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart2;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.util.StringConverter;

/**
 * @author RobTerpilowski
 */
public class DecimalAxisFormatter extends StringConverter<Number> {

    protected DecimalFormat decimalFormat;

    public DecimalAxisFormatter(String format) {
        decimalFormat = new DecimalFormat(format);
    }

    public DecimalAxisFormatter(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    @Override
    public String toString(Number object) {
        return decimalFormat.format(object.doubleValue());
    }

    @Override
    public Number fromString(String string) {
        try {
            return decimalFormat.parse(string);
        } catch (ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

}