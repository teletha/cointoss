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
     * @param message
     * @param service
     */
    NetworkError(Kind kind, String message, MarketService service) {
        super(kind.message + " " + message, null, true, true);

        this.kind = kind;
        this.service = service;
    }

    /**
     * Hide constructor.
     * 
     * @param cause
     * @param service
     */
    NetworkError(Kind kind, Throwable cause, MarketService service) {
        super(kind.message, cause, true, true);

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
     * Test whether the given error is specified {@link NetworkError} or not.
     * 
     * @param error
     * @param kind
     * @return
     */
    public static boolean check(Throwable error, Kind kind) {
        return error instanceof NetworkError x && x.kind == kind;
    }

    /**
     * Error type.
     */
    public enum Kind {
        Unauthenticated("Authetication is required. Please check your access token or password.", false),

        LimitOverflow("API limit has been exceeded. Please try again in a few minutes.", true),

        Maintenance("The market is under maintenance. Please refer to the official maintenance information.", true),

        InvalidOrder("The order quantity is too small. Please increase the order quantity.", false),

        Unkwnow("Unknown network error.", true);

        /** The associated message. */
        public final String message;

        /** The recoverable error or not. */
        public final boolean recoverable;

        private Kind(String message, boolean recoverable) {
            this.message = message;
            this.recoverable = recoverable;
        }
    }
}
