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

import cointoss.util.array.WrapperArray;
import cointoss.util.function.ToWrapperTriFunction;
import cointoss.util.function.WrapperPentaFunction;
import cointoss.util.function.WrapperTetraFunction;
import cointoss.util.function.WrapperTriFunction;
import cointoss.util.map.ConcurrentNavigableWrapperMap;
import cointoss.util.map.ConcurrentWrapperMap;
import cointoss.util.map.NavigableWrapperMap;
import cointoss.util.map.SkipListWrapperMap;
import cointoss.util.map.WrapperMap;
import cointoss.util.ring.WrapperRingBuffer;
import cointoss.util.set.NavigableWrapperSet;
import cointoss.util.set.SortedWrapperSet;
import cointoss.util.set.WrapperSet;
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
            List<String> lines = sourceFile.lines().map(line -> type.replace(line)).skip(line -> line.equals("SKIPLINE")).toList();
            generateFile.text(lines);
            System.out.println("Generate " + generateFile);
        }
    }

    /**
     * 
     */
    public enum Type {

        Object(false, "E", "", "E", "null"),

        Int(true, "int", "Int", "Integer", "0"),

        Long(true, "long", "Long", "Long", "0L"),

        Double(true, "double", "Double", "Double", "0d");

        private final boolean numeric;

        private final String primitiveName;

        private final String wrapperName;

        private final String wrapperType;

        private final String initialValue;

        /**
         * @param specializedType
         */
        private Type(boolean numeric, String primitiveName, String wrapperName, String wrapperType, String initialValue) {
            this.numeric = numeric;
            this.primitiveName = primitiveName;
            this.wrapperName = wrapperName;
            this.wrapperType = wrapperType;
            this.initialValue = initialValue;
        }

        String replace(String text) {
            if (text.startsWith("import " + SpecializedCodeGenerator.class.getCanonicalName())) {
                if (text.startsWith("import " + WrapperFunction.class.getCanonicalName())) {
                    return "import java.util.function." + wrapperName + "Function;";
                } else {
                    return "SKIPLINE";
                }
            }

            String primitiveFunction = WrapperFunction.class.getSimpleName();

            // initial value
            text = text.replaceAll("Wrapper\\.initital\\(\\)", initialValue);

            // new int[size] or (E[]) Array.newInstance(Object.class, size)
            text = text.replaceAll("Wrapper\\.newArray\\((.+)\\)", //
                    numeric ? "new " + primitiveName + "[$1]" : "(E[]) java.lang.reflect.Array.newInstance(Object.class, $1)");

            // increment and decrement
            text = text.replaceAll("Primitive\\.increment\\((.+), (.+)\\)", "$1 += $2");
            text = text.replaceAll("Primitive\\.decrement\\((.+), (.+)\\)", "$1 -= $2");

            // Primitive and Wrapper
            text = text.replace(primitiveFunction, wrapperName + "Function");
            text = text.replace("Primitive", primitiveName);
            text = text.replaceAll("(\\w*)Wrapper\\d?(\\w*)<Wrapper\\d?>", //
                    "$1" + wrapperName + "$2" + (numeric ? "" : "<" + wrapperType + ">"));
            text = text.replaceAll("(\\W)Wrapper\\d?(\\W)", "$1" + wrapperType + "$2");
            text = text.replaceAll("Wrapper\\d?", wrapperName);

            return text;
        }
    }

    /**
     * Generate code.
     */
    public static void main(String[] args) {
        // Array
        SpecializedCodeGenerator.write(WrapperRingBuffer.class);
        SpecializedCodeGenerator.write(WrapperArray.class, Type.Int, Type.Long, Type.Double);

        // Set
        SpecializedCodeGenerator.write(WrapperSet.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(SortedWrapperSet.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(SortedWrapperSet.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(NavigableWrapperSet.class, Type.Int, Type.Long, Type.Double);

        // Map
        SpecializedCodeGenerator.write(WrapperMap.class, Type.Int, Type.Double);
        SpecializedCodeGenerator.write(NavigableWrapperMap.class, Type.Int, Type.Double);
        SpecializedCodeGenerator.write(ConcurrentWrapperMap.class, Type.Int, Type.Double);
        SpecializedCodeGenerator.write(ConcurrentNavigableWrapperMap.class, Type.Int, Type.Double);
        SpecializedCodeGenerator.write(SkipListWrapperMap.class, Type.Long);

        // Function
        SpecializedCodeGenerator.write(WrapperPentaFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(WrapperTetraFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(WrapperTriFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(ToWrapperTriFunction.class, Type.Int, Type.Long, Type.Double);
    }

    /**
     * Replaceable type for wrapper types.
     */
    public static interface Wrapper {

        static int compare(Primitive one, Primitive other) {
            throw new Error("Dummy code");
        }

        /**
         * Create inital value.
         * 
         * @return
         */
        public static <AnyType> AnyType initital() {
            throw new Error("Dummy code");
        }

        /**
         * Create array.
         * 
         * @param size
         * @return
         */
        public static <AnyType> AnyType[] newArray(int size) {
            throw new Error("Dummy code");
        }
    }

    /**
     * Replaceable type for primitive function types.
     */
    public static interface WrapperFunction<V> {

        V apply(Primitive value);
    }

    /**
     * Replaceable type for primitive types.
     */
    public static interface Primitive extends Wrapper {

        /**
         * This code will be replaced by increment code of primitive type (i.e. base += 3).
         * 
         * @param base A base value.
         * @param increment A increment size.
         * @return Increment code.
         */
        public static Primitive increment(Primitive base, Primitive increment) {
            throw new Error("Dummy code");
        }

        /**
         * This code will be replaced by decrement code of primitive type (i.e. base -= 3).
         * 
         * @param base A base value.
         * @param decrement A decrement size.
         * @return Decrement code.
         */
        public static Primitive decrement(Primitive base, Primitive decrement) {
            throw new Error("Dummy code");
        }
    }
}
