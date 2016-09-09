package com.miao.android.myzhihudaily.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.miao.android.myzhihudaily.R;

/**
 * Created by Administrator on 2016/9/4.
 */
public class UtilFunctions {

    public static int getThemeState(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        return sp.getInt("theme", 0);
    }

    public static void setThemeState(Context context, int themeValue) {
        SharedPreferences.Editor editor = context.getSharedPreferences("user_settings",
                Context.MODE_PRIVATE).edit();
        editor.putInt("theme", themeValue);
        editor.apply();
    }

    public static void setTheme(Activity activity) {
        if (UtilFunctions.getThemeState(activity) == 0) {
            activity.setTheme(R.style.DayTheme);
        }else {
            activity.setTheme(R.style.NightTheme);
        }

        // change the status bar's color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = activity.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            if (UtilFunctions.getThemeState(activity) == 0){
                window.setStatusBarColor(activity.getResources().getColor(R.color.colorStatusBarLight));
            } else {
                window.setStatusBarColor(activity.getResources().getColor(R.color.colorStatusBarDark));
            }

        }
    }
}
