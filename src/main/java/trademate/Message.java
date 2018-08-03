/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import kiss.Extensible;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2018/08/03 17:01:00
 */
@SuppressWarnings("unused")
@Manageable(lifestyle = Singleton.class)
public class Message implements Extensible {

    /**
     * Log is not found.
     * 
     * @return
     */
    public String logIsNotFound() {
        return "No logs were found for the specified date.";
    }

    /**
     * Log is not found.
     * 
     * @return
     */
    public String endDateMustBeAfterStartDate() {
        return "The end date must be after the start date.";
    }

    /**
     * Japanese bundle.
     * 
     * @version 2018/08/03 17:06:23
     */
    private static class Message_ja extends Message {

        /**
         * {@inheritDoc}
         */
        @Override
        public String logIsNotFound() {
            return "指定された日付のログが見つかりません。";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String endDateMustBeAfterStartDate() {
            return "終了日は開始日よりも後にしてください。";
        }

    }
}
