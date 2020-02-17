/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.extension;

public enum ScenePart implements TradePart {

    /** Entry is just requested. */
    Entry,

    /** Entry is executed partially. */
    EntryPartially,

    /** Complete entry. */
    EntryCompletely,

    /** Complete entry by multiple executions. */
    EntryMultiple,

    /** Complete entry by multiple executions over long time. */
    EntrySeparately,

    /** Entry was requested and canceled. */
    EntryCanceled,

    /** Entry is executed partially and the remaining is canceled. */
    EntryPartiallyCanceled,

    /** Complete entry and exit is just requested. */
    Exit,

    /** Complete entry and exit is executed partially. */
    ExitPartially,

    /** Complete entry and half exit is executed completely. */
    ExitHalf,

    /** Complete entry and exit. */
    ExitCompletely,

    /** Complete entry and complete exit by multiple executions. */
    ExitMultiple,

    /** Complete entry and complete exit by multiple executions over long time. */
    ExitSeparately,

    /** Complete entry and complete exit later. */
    ExitLater,

    /** Complete entry and exit is canceled. */
    ExitCanceled,

    /** Complete entry and first exit was canceled then second exit is requested. */
    ExitCanceledThenOtherExit,

    /** Complete entry and first exit was canceled then second exit is completed. */
    ExitCanceledThenOtherExitCompletely,

    /** Complete entry and exit is executed partially and the remaining is canceled. */
    ExitPartiallyCancelled,

    // entry partially cancelled and exit
    EntryPartiallyAndExitCompletely;
}
