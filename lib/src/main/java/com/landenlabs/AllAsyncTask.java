/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see https://LanDenLabs.com/
 */

package com.landenlabs;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * AllAsyncTask is a replacement for AsyncTask
 */
public class AllAsyncTask<IN, RUN, OUT> implements Runnable {
    static final int MESSAGE_FINISH = -100;
    static final int MESSAGE_ERROR = -101;
    static final int MESSAGE_STOP = -102;

    private static final AtomicInteger count = new AtomicInteger(0);
    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8);

    public int id;
    public IN initParams;
    public RUN runParams;
    public OUT outResult;
    public Exception error;
    public boolean okay = true;
    Function<AllAsyncTask<IN, RUN, OUT>, OUT> workFunc;
    Function1<Message> msgFunc;
    Function1<AllAsyncTask<IN, RUN, OUT>> finishFunc;
    Function1<AllAsyncTask<IN, RUN, OUT>> errorFunc;
    private final Handler uiHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == MESSAGE_FINISH) {
                if (finishFunc != null) {
                    finishFunc.apply(AllAsyncTask.this);
                }
                // clear();
            } else if (message.what == MESSAGE_ERROR) {
                if (errorFunc != null) {
                    errorFunc.apply(AllAsyncTask.this);
                }
                // clear();
            } else if (message.what == MESSAGE_STOP) {
                okay = false;
                if (errorFunc != null) {
                    errorFunc.apply(AllAsyncTask.this);
                }
            } else {
                if (msgFunc != null) {
                    msgFunc.apply(message);
                }
            }

            return true;
        }
    });

    public AllAsyncTask<IN, RUN, OUT> init(
            @Nullable IN initParams,
            @Nullable Function<AllAsyncTask<IN, RUN, OUT>, Boolean> preMainThread) {
        id = count.getAndIncrement();
        this.initParams = initParams;
        if (preMainThread != null) {
            okay = preMainThread.apply(this);
        }
        return this;
    }

    public AllAsyncTask<IN, RUN, OUT> onThread(
            @Nullable RUN runParams,
            @NonNull Function<AllAsyncTask<IN, RUN, OUT>, OUT> workFunc) {
        this.runParams = runParams;
        this.workFunc = workFunc;
        return this;
    }

    public AllAsyncTask<IN, RUN, OUT> onMessage(Function1<Message> msgFunc) {
        this.msgFunc = msgFunc;
        return this;
    }

    public AllAsyncTask<IN, RUN, OUT> onFinish(
            @Nullable Function1<AllAsyncTask<IN, RUN, OUT>> finishFunc) {
        this.finishFunc = finishFunc;
        return this;
    }

    public AllAsyncTask<IN, RUN, OUT> onError(
            @Nullable Function1<AllAsyncTask<IN, RUN, OUT>> errorFunc) {
        this.errorFunc = errorFunc;
        return this;
    }

    public void start() {
        if (okay) {
            executor.execute(this);
        } else if (errorFunc != null) {
            errorFunc.apply(this);
        }
    }

    /*
     * Post message from WorkerThread to MainThread:
     */
    @WorkerThread
    public void postToMain(@NonNull Message message) {
        uiHandler.sendMessage(message);
    }

    @WorkerThread
    public void postToMain(@Nullable String msgStr) {
        if (msgStr != null) {
            Message message = Message.obtain();
            message.obj = msgStr;
            uiHandler.sendMessage(message);
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Message message = Message.obtain();
        try {
            message.what = MESSAGE_FINISH;
            this.outResult = workFunc.apply(this);
            message.obj = this;
        } catch (Exception ex) {
            message.what = MESSAGE_ERROR;
            this.error = ex;
            message.obj = this;
        }

        postToMain(message);
    }

    public interface Function1<TT> {
        void apply(TT var1);
    }
}