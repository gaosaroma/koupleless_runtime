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
package com.alipay.sofa.koupleless.common.api;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;

/**
 * <p>SpringBeanFinder class.</p>
 *
 * @author yuanyuan
 * @author zzl_i
 * @since 2023/12/8 5:26 下午
 *
 * SpringBeanFinder 查找基座bean工具类，无跨classloader支持
 * @version 1.0.0
 */
public class SpringBeanFinder {

    /**
     * <p>getBaseBean.</p>
     *
     * @param name a {@link java.lang.String} object
     * @return a {@link java.lang.Object} object
     */
    public static Object getBaseBean(String name) {
        Biz masterBiz = ArkClient.getMasterBiz();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getBizRuntimeContext(masterBiz);
        return bizRuntimeContext.getRootApplicationContext().getBean(name);
    }

    /**
     * <p>getBaseBean.</p>
     *
     * @param type a {@link java.lang.Class} object
     * @param <T> a T class
     * @return a T object
     */
    public static <T> T getBaseBean(Class<T> type) {
        Biz masterBiz = ArkClient.getMasterBiz();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getBizRuntimeContext(masterBiz);
        return bizRuntimeContext.getRootApplicationContext().getBean(type);
    }
}
