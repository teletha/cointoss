/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.extension;

public enum ScenePart implements TradePart {

    /** Entry is just requested. */
    Entry(false),

    /** Entry is executed partially. */
    EntryPartially(false),

    /** Complete entry. */
    EntryCompletely(false),

    /** Complete entry by multiple executions. */
    EntryMultiple(false),

    /** Complete entry by multiple executions over long time. */
    EntrySeparately(false),

    /** Entry was requested and canceled. */
    EntryCanceled(false),

    /** Entry is executed partially and the remaining is canceled. */
    EntryPartiallyCanceled(false),

    /** Complete entry and exit is just requested. */
    Exit(true),

    /** Complete entry and exit is executed partially. */
    ExitPartially(true),

    /** Complete entry and exit. */
    ExitCompletely(true),

    /** Complete entry and complete exit by multiple executions. */
    ExitMultiple(true),

    /** Complete entry and complete exit by multiple executions over long time. */
    ExitSeparately(true),

    /** Complete entry and exit is canceled. */
    ExitCanceled(true),

    /** Complete entry and first exit was canceled then second exit is requested. */
    ExitCanceledThenOtherExit(true),

    /** Complete entry and first exit was canceled then second exit is completed. */
    ExitCanceledThenOtherExitCompletely(true),

    /** Complete entry and exit is executed partially and the remaining is canceled. */
    ExitPartiallyCancelled(true),

    /** entry partially cancelled and exit. */
    EntryPartiallyCanceledAndExitCompletely(true),

    ExitOneCompletedOtherRemained(true);

    /** The scene status. */
    public final boolean hasExit;

    private ScenePart(boolean hasExit) {
        this.hasExit = hasExit;
    }
}