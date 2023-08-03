/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;

class NormalLogTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    private NormalLog write(Object... lines) {
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

        return new NormalLog(file);
    }

    @Test
    void firstId() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLog file = write(e1);
        assert file.firstID() == e1.id;

        file = write(e1, e2);
        assert file.firstID() == e1.id;
    }

    @Test
    void firstIdOnEmptyFile() {
        NormalLog file = write();
        assert file.firstID() == -1;
    }

    @Test
    void lastID() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLog file = write(e1, e2);
        assert file.lastID() == e2.id;

        file = write(e1, e2, "3 corrupted");
        assert file.lastID() == e2.id;
    }

    @Test
    void lastIDOnEmptyFile() {
        NormalLog file = write();
        assert file.lastID() == -1;
    }

    @Test
    void isCorrupted() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLog file = write(e1);
        assert file.isCorrupted() == false;

        file = write(e1, e2);
        assert file.isCorrupted() == false;

        file = write(e1, e2, "3 corrupted line");
        assert file.isCorrupted();
    }

    @Test
    void isCorruptedOnEmptyFile() {
        NormalLog file = write();
        assert file.isCorrupted() == false;
    }

    @Test
    void repair() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        NormalLog file = write(e1, e2, "3 corrupted line");
        file.repair();
        assert file.isCorrupted() == false;
    }

    @Test
    void repairOnEmptyFile() {
        NormalLog file = write();
        file.repair();
        assert file.isCorrupted() == false;
    }
}