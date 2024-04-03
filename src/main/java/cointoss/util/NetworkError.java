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

import cointoss.MarketService;

@SuppressWarnings("serial")
public final class NetworkError extends Error {

    /** The target market. */
    public final MarketService service;

    /** The kind of error. */
    public final Kind kind;

    /**
     * Hide constructor.
     * 
     * @param cause
     * @param service
     */
    NetworkError(Kind kind, Throwable cause, MarketService service) {
        super(kind.message, cause, false, true);

        this.kind = kind;
        this.service = service;
    }

    /**
     * Locate error request.
     * 
     * @return
     */
    public String getLocation() {
        if (getCause() instanceof HttpRetryException x) {
            return x.getLocation();
        } else {
            return "";
        }
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
     * Error type.
     */
    public enum Kind {
        Unauthenticated("Authetication is required. Please check your access token or password."),

        LimitOverflow("API limit has been exceeded. Please try again in a few minutes."),

        Maintenance("The market is under maintenance. Please refer to the official maintenance information."),

        MinimumOrder("The order quantity is too small. Please increase the order quantity.");

        /** The associated message. */
        public final String message;

        private Kind(String message) {
            this.message = message;
        }
    }
}
