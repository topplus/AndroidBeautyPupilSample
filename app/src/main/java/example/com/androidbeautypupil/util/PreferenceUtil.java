package example.com.androidbeautypupil.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * time: 15/7/17
 * description: sharedPreference管理类
 *
 * @author fandong
 */
public class PreferenceUtil {

    public static void putInt(Context context, String key, int value) {
        if (!TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = context.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return context.getSharedPreferences("config", Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        if (!TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = context.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    public static Boolean getBoolean(Context context, String key, boolean defaultValue) {
        return context.getSharedPreferences("config", Context.MODE_PRIVATE).getBoolean(key, defaultValue);
    }

}
