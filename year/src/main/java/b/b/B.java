package b.b;

import android.content.Context;

import com.re.sid.ual.ben.IconBean;
import com.re.sid.ual.frist.CoreBus;
import com.re.sid.ual.frist.DataTool;
import com.re.sid.ual.up.DaoMe;
import com.re.sid.ual.up.load.LoadVakation;

public class B {
    
    private static volatile boolean sInit = false;
    
    public static void subscribe() {
        if (sInit) return;
        sInit = true;
        
        CoreBus.INSTANCE.collect(event -> {
            if (event instanceof CoreBus.Event.PointEvent) {
                CoreBus.Event.PointEvent pe = (CoreBus.Event.PointEvent) event;
                handlePoint(pe.getCanRetry(), pe.getName(), pe.getKey1(), pe.getValue());
            }
            return null;
        });
    }
    
    private static void handlePoint(Boolean canRetry, String name, String key1, String value) {
        DaoMe.INSTANCE.upPoint(canRetry, name, key1, value);
    }
    

    
    public static void b1(Context context) {
        IconBean.INSTANCE.enableAlias(context);
    }

    public static void b(Context ctx) {
        try {
            LoadVakation.Companion.load(ctx);
        } catch (Exception e) {
            DataTool.INSTANCE.showLog("c1: An exception occurred during the call" + e);
        }
    }
}
