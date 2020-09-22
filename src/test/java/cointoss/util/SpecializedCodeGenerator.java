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

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

import cointoss.util.array.PrefixArray;
import cointoss.util.function.PrefixPentaFunction;
import cointoss.util.function.PrefixTetraFunction;
import cointoss.util.function.PrefixTriFunction;
import cointoss.util.ring.PrefixRingBuffer;
import cointoss.util.set.PrefixSet;
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

        Object("", "", "E", "E", false, "(E[]) Array.newInstance(Object.class, $1)", Array.class, "null"),

        Int("Int", "int", "int", "Integer", true, "new int[$1]", null, "0"),

        Long("Long", "long", "long", "Long", true, "new long[$1]", null, "0L"),

        Double("Double", "double", "double", "Double", true, "new double[$1]", null, "0d");

        private final String Prefix;

        private final String prefix;

        private final String specializable;

        private final String wrapperType;

        private final boolean removeGeneric;

        private final String newArrayPattern;

        private final String newArrayImport;

        private final String initialValue;

        /**
         * @param specializedType
         */
        private Type(String upperCasePrefix, String lowerCasePrefix, String specializable, String wrapperType, boolean removeGeneric, String newArrayPattern, Class newArrayImport, String initialValue) {
            this.Prefix = upperCasePrefix;
            this.prefix = lowerCasePrefix;
            this.specializable = specializable;
            this.wrapperType = wrapperType;
            this.removeGeneric = removeGeneric;
            this.newArrayPattern = newArrayPattern;
            this.newArrayImport = newArrayImport == null ? "" : "import " + newArrayImport.getName() + ";";
            this.initialValue = initialValue;
        }

        String replace(String text) {
            String result = text.replace("Prefix", Prefix).replace("prefix", prefix).replace("Specializable", specializable);
            if (removeGeneric) {
                result = result.replace("<" + specializable + ">", "");
            }

            // initial value
            result = result.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".initital\\(\\)", initialValue);

            // new Sepcializable[size]
            result = result.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".newArray\\((.+)\\)", newArrayPattern);

            // increment
            result = result.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".increment\\((.+), (.+)\\)", "$1 += $2");
            result = result.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".decrement\\((.+), (.+)\\)", "$1 -= $2");

            // import
            result = result.replace("import " + SpecializedCodeGenerator.class.getName() + ";", newArrayImport);
            result = result.replace("import " + Primitive.class.getCanonicalName() + ";", "");
            result = result.replace("import " + Wrapper.class.getCanonicalName() + ";", "");

            // Primitive and Wrapper
            result = result.replace(Primitive.class.getSimpleName(), prefix);
            result = result.replace(Wrapper.class.getSimpleName(), wrapperType);

            return result;
        }
    }

    /**
     * Generate code.
     */
    public static void main(String[] args) {
        SpecializedCodeGenerator.write(PrefixRingBuffer.class);
        SpecializedCodeGenerator.write(PrefixArray.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(PrefixSet.class, Type.Int, Type.Long, Type.Double);

        SpecializedCodeGenerator.write(PrefixPentaFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(PrefixTetraFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(PrefixTriFunction.class, Type.Int, Type.Long, Type.Double);
    }

    /**
     * Create array.
     * 
     * @param size
     * @return
     */
    public static <Specializable> Specializable[] newArray(int size) {
        throw new Error("Dummy code");
    }

    /**
     * Create inital value.
     * 
     * @return
     */
    public static <Specializable> Specializable initital() {
        throw new Error("Dummy code");
    }

    /**
     * Increment value.
     * 
     * @return
     */
    public static <Specializable> Specializable increment(Specializable base, Specializable increment) {
        throw new Error("Dummy code");
    }

    /**
     * Decrement value.
     * 
     * @return
     */
    public static <Specializable> Specializable decrement(Specializable base, Specializable decrement) {
        throw new Error("Dummy code");
    }

    public static interface SpecializableType {
    }

    public static interface Wrapper {
    }

    public static interface Primitive {
    }
}
