/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package divineboon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.jpountz.lz4.LZ4FrameInputStream;

public class LZ4Decompressor {
    public static void main(String[] args) throws IOException {
        Path inputFile = Paths.get("20250105.csv.lz4");
        Path outputFile = Paths.get("SOL.csv");

        try (InputStream fis = Files.newInputStream(inputFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                LZ4FrameInputStream lz4InputStream = new LZ4FrameInputStream(bis);
                OutputStream fos = Files.newOutputStream(outputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = lz4InputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            System.out.println("Decompression complete: " + outputFile.toAbsolutePath());
        }
    }
}
