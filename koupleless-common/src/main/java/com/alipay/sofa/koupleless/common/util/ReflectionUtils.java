/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>ReflectionUtils class.</p>
 *
 * @author yuanyuan
 * @author zzl_i
 * @since 2023/9/25 5:06 下午
 * @version 1.0.0
 */
public class ReflectionUtils {

    private static Method method;

    static {
        try {
            Class<?> clazz = Class.forName("sun.reflect.Reflection");
            method = clazz.getDeclaredMethod("getCallerClass", new Class[] { int.class });
            method.setAccessible(true);
        } catch (Exception e) {
            method = null;
        }
    }

    /**
     * <p>getEquivalentStackTraceElement.</p>
     *
     * @param depth a int
     * @return a {@link java.lang.StackTraceElement} object
     */
    public static StackTraceElement getEquivalentStackTraceElement(final int depth) {
        // (MS) I tested the difference between using Throwable.getStackTrace() and Thread.getStackTrace(), and
        // the version using Throwable was surprisingly faster! at least on Java 1.8. See ReflectionBenchmark.
        final StackTraceElement[] elements = new Throwable().getStackTrace();
        int i = 0;
        for (final StackTraceElement element : elements) {
            if (isValid(element)) {
                if (i == depth) {
                    return element;
                }
                ++i;
            }
        }
        throw new IndexOutOfBoundsException(Integer.toString(depth));
    }

    /**
     * <p>isValid.</p>
     *
     * @param element a {@link java.lang.StackTraceElement} object
     * @return a boolean
     */
    public static boolean isValid(final StackTraceElement element) {
        // ignore native methods (oftentimes are repeated frames)
        if (element.isNativeMethod()) {
            return false;
        }
        final String cn = element.getClassName();
        // ignore OpenJDK internal classes involved with reflective invocation
        if (cn.startsWith("sun.reflect.")) {
            return false;
        }
        final String mn = element.getMethodName();
        // ignore use of reflection including:
        // Method.invoke
        // InvocationHandler.invoke
        // Constructor.newInstance
        if (cn.startsWith("java.lang.reflect.")
            && (mn.equals("invoke") || mn.equals("newInstance"))) {
            return false;
        }
        // ignore use of Java 1.9+ reflection classes
        if (cn.startsWith("jdk.internal.reflect.")) {
            return false;
        }
        // ignore Class.newInstance
        if (cn.equals("java.lang.Class") && mn.equals("newInstance")) {
            return false;
        }
        // ignore use of Java 1.7+ MethodHandle.invokeFoo() methods
        if (cn.equals("java.lang.invoke.MethodHandle") && mn.startsWith("invoke")) {
            return false;
        }
        // any others?
        return true;
    }

    /**
     * <p>executeReflectionLogic.</p>
     *
     * @param realFramesToSkip a int
     * @return a {@link java.lang.Class} object
     */
    public static Class<?> executeReflectionLogic(int realFramesToSkip) {
        // 在 JDK 8 下执行的方法逻辑
        if (method == null)
            throw new IllegalStateException("sun.reflect.Reflection initialization failure.");
        try {
            return (Class<?>) method.invoke(null, realFramesToSkip);
        } catch (Exception e) {
            throw new IllegalStateException(
                "An error occurs when invoking the sun.reflect.Reflection#getCallerClass", e);
        }
    }

    /**
     * <p>executeStackTraceLogic.</p>
     *
     * @param depth a int
     * @return a {@link java.lang.Class} object
     */
    public static Class<?> executeStackTraceLogic(int depth) {
        // 在 JDK 17 下执行的方法逻辑
        // 解除注释，编译成Class 并且放置到 META-INF/versions/17/com/alipay/sofa/serverless/common/util 下面
        // slower fallback method using stack trace
        final StackTraceElement element = getEquivalentStackTraceElement(depth);
        try {
            return LoaderUtil.loadClass(element.getClassName());
        } catch (final ClassNotFoundException e) {
            //continue
        }
        return null;
    }

    /**
     * <p>getCallerClass.</p>
     *
     * @param realFramesToSkip a int
     * @return a {@link java.lang.Class} object
     */
    public static Class<?> getCallerClass(int realFramesToSkip) {
        try {
            return executeReflectionLogic(realFramesToSkip);
        } catch (Exception e) {
            return executeStackTraceLogic(realFramesToSkip);
        }
    }

    /**
     * Set field of specified object to value, will try to operate on super class until success
     *
     * @param fieldName a {@link java.lang.String} object
     * @param o a {@link java.lang.Object} object
     * @param value a T object
     * @param <T> a T class
     */
    public static <T> void setField(String fieldName, Object o, T value) {
        Class<?> klass = o.getClass();
        while (klass != null) {
            try {
                Field f = klass.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(o, value);
                return;
            } catch (Exception e) {
                klass = klass.getSuperclass();
            }
        }
    }

}
