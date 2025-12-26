package com.helico.bacter.pylori;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;

import java.util.List;

import c.c.C;
import com.re.sid.ual.frist.CoreBus;

import ad.AdE;
/**
 * Date：2025/9/25
 * Describe:
 * com.ak.impI.Core
 */
public class Core {

    public static long insAppTime = 0L; //installAppTime
    public static Application mApp;


    public static void a(Application ctx) {
        mApp = ctx;
        // 初始化订阅
        a.a.A.subscribe();
        b.b.B.subscribe();
        
        pE("test_d_load");
        inIf(mApp);
        AdE.a2();
    }
    
    public static List<Activity> a0() {
        return CoreBus.INSTANCE.getActivities();
    }
    
    public static void pE(String string, String value) {
        boolean canRetry;
        switch (string) {
            case "config_G":
            case "cf_fail":
            case "pop_fail":
            case "advertise_limit":
                canRetry = true;
                break;
            default:
                canRetry = false;
                break;
        }
        CoreBus.INSTANCE.emit(new CoreBus.Event.PointEvent(canRetry, string, "string", value));
    }

    public static void pE(String string) {
        pE(string, "");
    }

    public static void postAd(String string) {
        CoreBus.INSTANCE.emit(new CoreBus.Event.PostAd(string));
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
