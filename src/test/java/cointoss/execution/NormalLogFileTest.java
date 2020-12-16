/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;

class NormalLogFileTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    private NormalLogFile write(Object... lines) {
        Path file = room.locateFile("test.log");

        StringBuilder builder = new StringBuilder();
        for (Object line : lines) {
            builder.append(line);

            if (line instanceof Execution) {
                builder.append("\r\n");
            }
        }

        try {
            Files.writeString(file, builder.toString());
        } catch (IOException e) {
            throw I.quiet(e);
        }

        return new NormalLogFile(file);
    }

    @Test
    void isCorrupted() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLogFile file = write(e1);
        assert file.isCorrupted() == false;

        file = write(e1, e2);
        assert file.isCorrupted() == false;

        file = write(e1, e2, "3 corrupted line");
        assert file.isCorrupted();
    }

    @Test
    void repair() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLogFile file = write(e1, e2, "3 corrupted line");
        file.repair();
        assert file.isCorrupted() == false;
    }

    @Test
    void readLastId() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLogFile file = write(e1, e2);
        assert file.readLastId() == 2;
    }
}
