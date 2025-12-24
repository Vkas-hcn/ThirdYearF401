package c.c;

import android.app.Application;

import androidx.datastore.preferences.core.PreferencesKeys;

import com.re.sid.ual.frist.DataTool;
import com.re.sid.ual.frist.GoErHave;

public class C {
    public static void c0(Application application) {
        GoErHave goOne = new GoErHave();
        goOne.goErHave(application);
    }

    /**
     * 获取字符串
     */
    public static String getStr(String key) {
        return DataTool.INSTANCE.getValue(
                PreferencesKeys.stringKey(key),
                ""
        );
    }

    /**
     * 保存字符串
     */
    public static void saveC(String key, String value) {
        DataTool.INSTANCE.setValue(
                PreferencesKeys.stringKey(key),
                value
        );
    }

    /**
     * 获取整数
     */
    public static int getInt(String key) {
        return DataTool.INSTANCE.getValue(
                PreferencesKeys.intKey(key),
                0
        );
    }

    /**
     * 保存整数
     */
    public static void saveInt(String key, int value) {
        DataTool.INSTANCE.setValue(
                PreferencesKeys.intKey(key),
                value
        );
    }
}
