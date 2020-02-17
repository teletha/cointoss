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

    // exit type
    Exit, ExitPartially, ExitCompletely, ExitCanceled, ExitPartiallyCancelled,

    /** Complete entry and complete exit later. */
    ExitLater,

    // entry partially cancelled and exit
    EntryPartiallyAndExitCompletely;
}
