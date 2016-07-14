package com.yxjie.chatrobot.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Created by yangxiangjie on 16/3/25.
 * 工作线程 与 UI线程类
 */
public class ThreadHelper {

    /**
     * 工作线程
     */
    private static final HandlerThread sWorkerThread;
    /**
     * 工作线程 Handler
     */
    private static final Handler sWorker;

    private static Handler sUIHandler;

    private static volatile ThreadHelper helper;


    static {
        sWorkerThread = new HandlerThread("sWorkerThread");
        sWorkerThread.start();
        sWorker = new Handler(sWorkerThread.getLooper());
    }

    private ThreadHelper() {
    }

    /**
     * 获取 ThreadHelper 实例
     *
     * @return ThreadHelper
     */
    public static ThreadHelper getInstance() {
        if (null == helper) {
            synchronized (ThreadHelper.class) {
                if (null == helper) {
                    helper = new ThreadHelper();
                }
            }
        }
        return helper;
    }

    /**
     * 判断 当前是否未UI 线程
     *
     * @return true UI线程
     */
    private static boolean isCurrentUIThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 获取 UI线程
     *
     * @return Handler
     */
    private static Handler getUIHandler() {
        synchronized (ThreadHelper.class) {
            if (null == sUIHandler) {
                sUIHandler = new Handler(Looper.getMainLooper());
            }
        }
        return sUIHandler;
    }

    /**
     * UI线程 运行
     *
     * @param runnable run
     */
    public static void runOnUIThread(Runnable runnable) {
        if (isCurrentUIThread()) {
            runnable.run();
        } else {
            getUIHandler().post(runnable);
        }
    }

    /**
     * 工作线程 运行
     *
     * @param runnable run
     */
    public static void runOnWorkerThread(Runnable runnable) {
        if (isCurrentUIThread()) {
            sWorker.post(runnable);
        } else {
            runnable.run();
        }
    }


}
