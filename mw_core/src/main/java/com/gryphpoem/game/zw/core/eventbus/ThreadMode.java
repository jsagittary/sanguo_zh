/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gryphpoem.game.zw.core.eventbus;

/**
 * 线程类型
 */
public enum ThreadMode {
    /**
     * 当前线程执行
     */
    POSTING,
    /**
     * 指定的后台线程
     */
    BACKGROUND,
    /**
     * 每次都创建一个线程
     */
    ASYNC
}