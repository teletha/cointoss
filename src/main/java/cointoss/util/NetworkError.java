/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.net.HttpRetryException;

import cointoss.market.Exchange;
import kiss.I;
import kiss.Variable;

@SuppressWarnings("serial")
public abstract class NetworkError extends Error {

    /** The error message. */
    private static final Variable<String> messageAuthentication = I
            .translate("Authetication is required. Please check your access token or password.");

    /** The error message. */
    private static final Variable<String> messageLimitOverflow = I
            .translate("API limit has been exceeded. Please try again in a few minutes.");

    /** The error message. */
    private static final Variable<String> messageMaintenance = I
            .translate("The market is under maintenance. Please refer to the official maintenance information.");

    /** The error message. */
    private static final Variable<String> messageMinimumOrder = I
            .translate("The order quantity is too small. Please increase the order quantity.");

    private static final Variable<String> URL = I.translate("Open the target URL");

    /** The target exchange. */
    private final Exchange exchange;

    /**
     * Hide constructor.
     * 
     * @param cause
     * @param exchange
     */
    protected NetworkError(Variable<String> message, Throwable cause, Exchange exchange) {
        super(message.exact(), cause, false, true);

        this.exchange = exchange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        if (getCause() instanceof HttpRetryException x) {
            return super.getMessage() + "\t" + x.getLocation() + " --> " + x.getReason();
        } else {
            return super.getMessage();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (getCause() instanceof HttpRetryException x) {
            return exchange.name() + "\r\n" + super.getMessage() + "\n[" + URL + "](" + x.getLocation() + ")";
        } else {
            return super.getMessage();
        }
    }

    public static class UnauthenticatedAccess extends NetworkError {

        UnauthenticatedAccess(Throwable cause, Exchange exchange) {
            super(messageAuthentication, cause, exchange);
        }
    }

    public static class APILimitOverflow extends NetworkError {

        APILimitOverflow(Throwable cause, Exchange exchange) {
            super(messageLimitOverflow, cause, exchange);
        }
    }

    public static class MarketMaintenance extends NetworkError {

        MarketMaintenance(Throwable cause, Exchange exchange) {
            super(messageMaintenance, cause, exchange);
        }
    }

    public static class MinimumOrder extends NetworkError {

        MinimumOrder(Throwable cause, Exchange exchange) {
            super(messageMinimumOrder, cause, exchange);
        }
    }
}
