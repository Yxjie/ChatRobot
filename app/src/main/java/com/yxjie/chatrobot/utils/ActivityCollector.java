package com.yxjie.chatrobot.utils;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Created by yangxiangjie on 16/3/25.
 * Save Activity Tool
 */
public class ActivityCollector {

    private static ArrayList<Activity> activityList = new ArrayList<>();

    /**
     * 添加 activity
     *
     * @param activity activity
     */
    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    /**
     * 移除 activity
     *
     * @param activity activity
     */
    public static void removeActivity(Activity activity) {
        activityList.remove(activity);
    }


    /**
     * 关闭 所有Activity
     */
    public static void finshAll() {
        for (Activity activity : activityList
                ) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        System.gc();
    }
}
