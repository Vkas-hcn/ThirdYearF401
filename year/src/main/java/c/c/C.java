package c.c;

import android.app.Application;

import androidx.datastore.preferences.core.PreferencesKeys;

import com.re.sid.ual.frist.DataTool;
import com.re.sid.ual.frist.GoErHave;
import com.re.sid.ual.frist.HandTool;

public class C {
    public static void c0(Application application) {
        GoErHave goOne = new GoErHave();
        goOne.goErHave(application);
    }

    public static void c(Application application) {
        HandTool.INSTANCE.loopStart(application);
    }
    public static String getStr(String key) {
        return DataTool.INSTANCE.getValue(
                PreferencesKeys.stringKey(key),
                ""
        );
    }

    public static void saveC(String key, String value) {
        DataTool.INSTANCE.setValue(
                PreferencesKeys.stringKey(key),
                value
        );
    }


    public static int getInt(String key) {
        return DataTool.INSTANCE.getValue(
                PreferencesKeys.intKey(key),
                0
        );
    }


    public static void saveInt(String key, int value) {
        DataTool.INSTANCE.setValue(
                PreferencesKeys.intKey(key),
                value
        );
    }
}
