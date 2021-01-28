/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.SPARSE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import kiss.I;

public class TimeseriseFile {

    private final FileChannel channel;

    /**
     * Create DB file.
     * 
     * @param file
     */
    public TimeseriseFile(Path file) {
        try {
            this.channel = FileChannel.open(file, CREATE, SPARSE, READ, WRITE);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
