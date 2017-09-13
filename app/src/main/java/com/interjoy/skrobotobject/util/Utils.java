package com.interjoy.skrobotobject.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;

/**
 *
 */

public class Utils {
    public static boolean IS_DEBUG = false;

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获得屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }


    /**
     * @param mContext
     * @return判断文件是否可以用
     */
    public static boolean isUseFile(Context mContext) {
        File[] files = ContextCompat.getExternalFilesDirs(mContext, null);
        if (files != null && files.length >= 1) {
            return true;
        }
        return false;
    }

    /**
     * @param mContext
     * @return获取文件路径
     */
    public static String getPath(Context mContext) {
        File[] files = ContextCompat.getExternalFilesDirs(mContext, null);
        if (files != null && files.length >= 1) {
            return files[0].getAbsolutePath();
        }
        return null;
    }

    private static final String TAG = "Utils";

    /**
     * @return获取文件可用空间大小
     */
    public static boolean readSDCard() {
        String state = Environment.getExternalStorageState();
        boolean isAvailable = false;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            if ((availCount * blockSize >> 20) > 100) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

}
