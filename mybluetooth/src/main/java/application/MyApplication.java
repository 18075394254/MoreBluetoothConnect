package application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


/**
 * Created by Administrator on 18-5-22.
 */
public class MyApplication extends Application {

    private static Context context;



    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();

    }
    public static Context getContext(){
        return context;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
    /** 获取屏幕宽度 */
    public static int getWindowWidth() {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            int width=wm.getDefaultDisplay().getWidth();
        Log.i("ooo", "width = " + width);
            return width;
    }
    /** 获取屏幕高度 */
    @SuppressWarnings("deprecation")
    public static int getWindowHeight() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int height=wm.getDefaultDisplay().getHeight();
        Log.i("ooo", "height = " + height);
        return height;
    }



    /**
     * 判断是否为平板
     *
     * @return
     */
    public boolean isPad() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        // 屏幕宽度
        float screenWidth = display.getWidth();
        // 屏幕高度
        float screenHeight = display.getHeight();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        // 屏幕尺寸
        double screenInches = Math.sqrt(x + y);
        // 大于6尺寸则为Pad
        return screenInches >= 6.0;
    }
}
