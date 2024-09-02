/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.nio.file.FileSystem;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.jimfs.Jimfs;

import cointoss.execution.ExecutionLog;
import cointoss.execution.TestableExecutionLog;
import cointoss.verify.VerifiableMarketService;
import psychopath.Directory;
import psychopath.Locator;

public class TestableMarketService extends VerifiableMarketService {

    /** The virtual file system. */
    private static final FileSystem fs = Jimfs.newFileSystem();

    /** Expose the testable object. */
    public TestableExecutionLog log;

    public TestableMarketService() {
        super(RandomStringUtils.secure().nextAlphanumeric(10));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExecutionLog createExecutionLog() {
        return log = new TestableExecutionLog(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory directory() {
        return Locator.directory(fs.getPath(marketName));
    }
}
