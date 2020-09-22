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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import cointoss.util.array.WrapperArray;
import cointoss.util.function.WrapperPentaFunction;
import cointoss.util.function.WrapperTetraFunction;
import cointoss.util.function.WrapperTriFunction;
import cointoss.util.map.ConcurrentNavigableWrapperMap;
import cointoss.util.map.ConcurrentWrapperMap;
import cointoss.util.map.NavigableWrapperMap;
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
            List<String> lines = sourceFile.lines().map(line -> type.replace(line)).toList();
            generateFile.text(lines);
            System.out.println("Generate " + generateFile);
        }
    }

    /**
     * 
     */
    public enum Type {

        Object("", "E", "E", "<E>", "(E[]) Array.newInstance(Object.class, $1)", Array.class, "null", Function.class),

        Int("Int", "int", "Integer", "", "new int[$1]", null, "0", IntFunction.class),

        Long("Long", "long", "Long", "", "new long[$1]", null, "0L", LongFunction.class),

        Double("Double", "double", "Double", "", "new double[$1]", null, "0d", DoubleFunction.class);

        private final String Prefix;

        private final String prefix;

        private final String wrapperType;

        private final String erasedWrapper;

        private final String newArrayPattern;

        private final String newArrayImport;

        private final String initialValue;

        private String functionImport;

        /**
         * @param specializedType
         */
        private Type(String upperCasePrefix, String lowerCasePrefix, String wrapperType, String erasedType, String newArrayPattern, Class newArrayImport, String initialValue, Class functionType) {
            this.Prefix = upperCasePrefix;
            this.prefix = lowerCasePrefix;
            this.wrapperType = wrapperType;
            this.erasedWrapper = erasedType;
            this.newArrayPattern = newArrayPattern;
            this.newArrayImport = newArrayImport == null ? "" : "import " + newArrayImport.getName() + ";";
            this.initialValue = initialValue;
            this.functionImport = "import " + functionType.getCanonicalName() + ";";
        }

        String replace(String text) {
            // initial value
            text = text.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".initital\\(\\)", initialValue);

            // new Sepcializable[size]
            text = text.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".newArray\\((.+)\\)", newArrayPattern);

            // increment
            text = text.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".increment\\((.+), (.+)\\)", "$1 += $2");
            text = text.replaceAll(SpecializedCodeGenerator.class.getSimpleName() + ".decrement\\((.+), (.+)\\)", "$1 -= $2");

            // import
            text = text.replace("import " + SpecializedCodeGenerator.class.getName() + ";", newArrayImport);
            text = text.replace("import " + Wrapper.class.getCanonicalName() + ";", "");
            text = text.replace("import " + Primitive.class.getCanonicalName() + ";", "");
            text = text.replace("import " + Erasable.class.getCanonicalName() + ";", "");
            text = text.replace("import " + PrimitiveFunction.class.getCanonicalName() + ";", functionImport);

            // Primitive and Wrapper
            String primitiveFunction = PrimitiveFunction.class.getSimpleName();
            String primitive = Primitive.class.getSimpleName();
            String wrapper = Wrapper.class.getSimpleName();
            String erasable = "@" + Erasable.class.getSimpleName() + " ";

            text = text.replace(primitiveFunction, Prefix + "Function");
            text = text.replace(primitive, prefix);
            text = text.replace("<" + erasable + wrapper + ">", erasedWrapper);
            text = text.replaceAll("(\\W)" + wrapper + "(\\W)", "$1" + wrapperType + "$2");
            text = text.replace(wrapper, Prefix);

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

        // Function
        SpecializedCodeGenerator.write(WrapperPentaFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(WrapperTetraFunction.class, Type.Int, Type.Long, Type.Double);
        SpecializedCodeGenerator.write(WrapperTriFunction.class, Type.Int, Type.Long, Type.Double);
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

        static int compare(Primitive one, Primitive other) {
            throw new Error("Dummy code");
        }
    }

    public static interface Primitive extends Wrapper {
    }

    public static interface PrimitiveFunction<V> {

        V apply(Primitive value);
    }

    @Target(ElementType.TYPE_PARAMETER)
    public @interface Erasable {
    }
}
