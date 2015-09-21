/*
 * Copyright 2015 Miklos Sagi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msagi.flashbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation class for FlashBus configuration.
 *
 * @author msagi (miklos.sagi@gmail.com)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface FlashBusConfiguration {

    /**
     * Package name to generate FlashBus with.
     *
     * @return The FlashBus class package name.
     */
    String packageName() default "com.msagi.flashbus";

    /**
     * Flag if FlashBus is to be generated in debug mode.
     *
     * @return The debug mode flag or false if not set.
     */
    boolean debug() default false;

    /**
     * List of sub-FlashBus class names to connect this FlashBus to.
     *
     * @return The list of fully qualified class names of sub-FlashBuses to connect to.
     */
    String[] connectTo() default {};
}
