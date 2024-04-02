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

@SuppressWarnings("serial")
public class NetworkError extends Error {

    /**
     * Hide constructor.
     * 
     * @param cause
     */
    protected NetworkError(String message, Throwable cause) {
        super(message, cause, false, true);
    }

    /**
     * Build user-friendly error message.
     * 
     * @param service
     * @param cause
     * @return
     */
    private static String buildErrorMessage(String message, Throwable cause) {
        if (cause instanceof HttpRetryException exception) {
            return message + " " + exception.getLocation() + " --> " + exception.getReason();
        } else {
            return message + " " + cause.getLocalizedMessage();
        }
    }

    public static class AuthenticationError extends NetworkError {

        AuthenticationError(Throwable cause) {
            super(buildErrorMessage("Authetication is required.", cause), cause);
        }
    }

    public static class RateLimitError extends NetworkError {

        RateLimitError(Throwable cause) {
            super(buildErrorMessage("RateLimit is overflow.", cause), cause);
        }
    }

    public static class MaintenanceError extends NetworkError {

        MaintenanceError(Throwable cause) {
            super(buildErrorMessage("Market is maintenance now.", cause), cause);
        }
    }
}
