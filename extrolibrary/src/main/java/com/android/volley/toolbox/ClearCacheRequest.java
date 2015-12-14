/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.volley.toolbox;

import android.os.Handler;
import android.os.Looper;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

/**
 * A synthetic request used for clearing the cache.
 * 用于人为清空 Http 缓存的请求。
 添加到 RequestQueue 后能很快执行，因为优先级很高，为Priority.IMMEDIATE。并且清空缓存的方法mCache.clear()写在了isCanceled()方法体中，能最早的得到执行。

 ClearCacheRequest 的写法不敢苟同，目前看来唯一的好处就是可以将清空缓存操作也当做一个请求。而在isCanceled()中做清空操作本身就造成了歧义，
 不看源码没人知道在NetworkDispatcher run 方法循环的过程中，isCanceled()这个读操作竟然做了可能造成缓存被清空。只能跟源码的解释一样当做一个 Hack 操作
 */
public class ClearCacheRequest extends Request<Object> {
    private final Cache mCache;
    private final Runnable mCallback;

    /**
     * Creates a synthetic request for clearing the cache.
     * @param cache Cache to clear
     * @param callback Callback to make on the main thread once the cache is clear,
     * or null for none
     */
    public ClearCacheRequest(Cache cache, Runnable callback) {
        super(Method.GET, null, null);
        mCache = cache;
        mCallback = callback;
    }

    @Override
    public boolean isCanceled() {
        // This is a little bit of a hack, but hey, why not.
        mCache.clear();
        if (mCallback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postAtFrontOfQueue(mCallback);
        }
        return true;
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }

    @Override
    protected Response<Object> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

    @Override
    protected void deliverResponse(Object response) {
    }
}
