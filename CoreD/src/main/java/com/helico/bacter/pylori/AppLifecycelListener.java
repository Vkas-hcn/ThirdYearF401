package com.helico.bacter.pylori;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ad.AdCenter;
import ad.AdE;

/**
 * Date：2025/9/26
 * Describe:
 */
public class AppLifecycelListener implements Application.ActivityLifecycleCallbacks {
    private int num = 0;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String name = activity.getClass().getSimpleName();
        Log.e("TAG", "onActivityCreated: "+name );
        if (name.equals(Constant.AC_NAME)) {
            AdCenter.showAd(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 使用 Builder 创建 TaskDescription
            ActivityManager.TaskDescription taskDescription = (new ActivityManager.TaskDescription.Builder()).setLabel("\t\n").build();
            activity.setTaskDescription(taskDescription);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        num++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        num--;
        if (num <= 0) {
            num = 0;
            AdE.finishAc();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        String name = activity.getClass().getSimpleName();
        if (name.equals(Constant.AC_NAME)) {
            View view = activity.getWindow().getDecorView();
            ((ViewGroup) view).removeAllViews();
        }
    }
}
