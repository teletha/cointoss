/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.List;
import java.util.Objects;

import cointoss.util.ring.PrefixRingBuffer;
import psychopath.File;
import psychopath.Locator;

public class SpecializedCodeGenerator {

    /**
     * Generates the specialized code for each type from the specified source code.
     * 
     * @param sourceCode
     * @param types
     */
    private static void write(Class sourceCode) {
        write(sourceCode, Type.values());
    }

    /**
     * Generates the specialized code for each type from the specified source code.
     * 
     * @param sourceCode
     * @param types
     */
    private static void write(Class sourceCode, Type... types) {
        Objects.requireNonNull(sourceCode);

        // find source code
        File sourceFile = Locator.directory("src/test/java").file(sourceCode.getName().replace('.', '/') + ".java");

        for (Type type : types) {
            File generateFile = Locator.directory("src/main/auto").file(type.replace(sourceCode.getName().replace('.', '/') + ".java"));
            List<String> lines = sourceFile.lines().map(line -> type.replace(line)).toList();
            generateFile.text(lines);
            System.out.println("Generate " + generateFile);
        }
    }

    /**
     * 
     */
    public enum Type {

        Object("", "", "E", false),

        Int("Int", "int", "int", true),

        Long("Long", "long", "long", true),

        Double("Double", "double", "double", true);

        private final String Prefix;

        private final String prefix;

        private final String specializable;

        private final boolean removeGeneric;

        /**
         * @param specializedType
         */
        private Type(String upperCasePrefix, String lowerCasePrefix, String specializable, boolean removeGeneric) {
            this.Prefix = upperCasePrefix;
            this.prefix = lowerCasePrefix;
            this.specializable = specializable;
            this.removeGeneric = removeGeneric;
        }

        String replace(String text) {
            String result = text.replace("Prefix", Prefix).replace("Specializable", specializable);
            if (removeGeneric) {
                result = result.replace("<" + specializable + ">", "");
            }
            return result;
        }
    }

    /**
     * Generate code.
     */
    public static void main(String[] args) {
        SpecializedCodeGenerator.write(PrefixRingBuffer.class);
    }
}
