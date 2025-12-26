package a.a;

import android.app.Activity;

import com.re.sid.ual.ben.GetZenbox;
import com.re.sid.ual.frist.CoreBus;
import com.re.sid.ual.up.DaoMe;

import java.util.List;

public class A {
    
    private static volatile boolean sInit = false;
    
    public static void subscribe() {
        if (sInit) return;
        sInit = true;
        
        // 注册 Activity 提供者
        CoreBus.INSTANCE.registerActivityProvider(() -> GetZenbox.INSTANCE.getActivityList());
        
        // 订阅事件
        CoreBus.INSTANCE.collect(event -> {
            if (event instanceof CoreBus.Event.PostAd) {
                handlePostAd(((CoreBus.Event.PostAd) event).getData());
            }
            return null;
        });
    }
    
    private static void handlePostAd(String num) {
        DaoMe.INSTANCE.upAd(num);
    }
    
    public static List<Activity> a() {
        return GetZenbox.INSTANCE.getActivityList();
    }

}
