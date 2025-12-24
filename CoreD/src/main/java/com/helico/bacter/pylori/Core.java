package com.helico.bacter.pylori;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import c.c.C;

import ad.AdE;
/**
 * Date：2025/9/25
 * Describe:
 * com.ak.impI.Core
 */
public class Core {

    public static long insAppTime = 0L; //installAppTime
    public static Application mApp;


    // todo  入口 记得做差异化
    public static void a(Application ctx) {
        mApp =  ctx;
        pE("test_d_load");
        inIf(mApp);
        AdE.a2();
    }

    public static void pE(String string, String value) {
        // todo 埋点上报 反射调用外面keep的方法
//        e.a(string, value);
        Log.e("TAG", "pE: "+string+"---"+value);
    }

    public static void pE(String string) {
        pE(string, "");
        Log.e("TAG", "pE 2: "+string);

    }

    public static void postAd(String string) {
        // todo 上报广告价值 反射调用外面keep的方法
    }


    public static String getStr(String key) {
        return C.getStr(key);
    }

    public static void saveC(String ke, String con) {
        C.saveC(ke, con);
    }

    public static int getInt(String key) {
        return C.getInt(key);
    }

    public static void saveInt(String key, int i) {
        C.saveInt(key, i);
    }

    private static void inIf(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            insAppTime = pi.firstInstallTime;
        } catch (Exception ignored) {
        }
    }
}
