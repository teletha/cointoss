/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.function;

import javax.annotation.processing.Generated;

import cointoss.util.SpecializedCodeGenerator.Primitive;

@Generated("SpecializedCodeGenerator")
public interface ToWrapperTriFunction<Param1, Param2, Param3> {

    /**
     * Compose value from three arguments.
     * 
     * @param param1 The first prameter.
     * @param param2 The second parameter.
     * @param param3 The third parameter.
     * @return A composed value.
     */
    Primitive applyAsWrapper(Param1 param1, Param2 param2, Param3 param3);
}