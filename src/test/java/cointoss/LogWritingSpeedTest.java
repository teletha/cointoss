/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import antibug.CleanRoom;

/**
 * @version 2018/05/19 9:21:21
 */
public class LogWritingSpeedTest {

    int count = 10000000;

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void single() throws IOException {
        Writer writer = Files.newBufferedWriter(room.locateFile("test"));

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            writer.append(i + "\r\n");
        }
        long end = System.currentTimeMillis();
        System.out.println("SINGLE : " + (end - start));
    }

    @Test
    void multiline() throws IOException {
        Writer writer = Files.newBufferedWriter(room.locateFile("test2"));

        int buffer = 10;
        long start = System.currentTimeMillis();

        for (int i = 0; i < count / buffer; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < buffer; j++) {
                builder.append(i + "\r\n");
            }
            writer.append(builder.toString());
        }
        long end = System.currentTimeMillis();
        System.out.println("MULTI : " + (end - start));
    }

    @Test
    void zip() throws FileNotFoundException, IOException {
        CsvWriterSettings writerConfig = new CsvWriterSettings();
        writerConfig.getFormat().setDelimiter(' ');

        Path output = room.locateFile("test1");
        CsvWriter writer = new CsvWriter(new FileOutputStream(output.toFile()), StandardCharsets.ISO_8859_1, writerConfig);

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            writer.writeRow(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("ZIP : " + (end - start));
    }
}
