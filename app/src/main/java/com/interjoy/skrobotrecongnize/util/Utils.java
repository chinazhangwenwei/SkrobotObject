package com.interjoy.skrobotrecongnize.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.interjoy.skrobotrecongnize.R;
import com.interjoy.skrobotrecongnize.bean.PersonRelation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.interjoy.skrobotrecongnize.activity.MainActivity.BOY_TYPE;
import static com.interjoy.skrobotrecongnize.activity.MainActivity.GIRL_TYPE;

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
     * 创建保存人脸图片的路径
     */
//    public static String createFilePath() {
//        String createPath = CREATE_PATH_FAIL;
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            File dir = new File(ABSOLUTE_PATH + "/Face");
//            if (!dir.exists()) dir.mkdirs();
//            createPath = dir.getAbsolutePath();
//        }
//        return createPath;
//    }

    /**
     * 创建保存人脸模型 和 待识别人脸图片的路径
     */
    public static String createFilePath() {
        String SdCardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String path = "";
        File dirModel = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dirModel = new File(SdCardRootPath + "/Face/Model");
            if (!dirModel.exists()) {
                dirModel.mkdirs();
            }
            File dirFaceDatabase = new File(SdCardRootPath + "/Face/FaceDatabase");
            if (!dirFaceDatabase.exists()) dirFaceDatabase.mkdirs();
            path = dirModel.getParentFile().getAbsolutePath();
        }
        return path;
    }

    public static boolean copyDataM(Context context, String modelPath) {
        boolean isOK = false;
        try {
            File file = new File(modelPath + "/Model", "Data.m");
            InputStream is = context.getResources().openRawResource(R.raw.data);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                if (fis.available() == is.available()) return true;
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[2048];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            isOK = true;
            fos.flush();
            fos.close();
            is.close();
        } catch (Exception e) {
            isOK = false;
            e.printStackTrace();
        }
        return isOK;
    }

    private static final String TAG = "Utils";

    public static List<PersonRelation> readDataFile(Context context) {
        InputStreamReader reader = null;
        InputStream inputStream;
        List<PersonRelation> personRelations = new ArrayList<>(160);

        try {
            inputStream = context.getResources().openRawResource(R.raw.two_person);
            reader = new InputStreamReader(inputStream, "UTF-8");

            BufferedReader bufferedReader = new BufferedReader(reader);
            String str;

            while ((str = bufferedReader.readLine()) != null) {
//                Log.d(TAG, "readDataFile: " + str);
                if (TextUtils.isEmpty(str) || str.equals("##")) {
                    break;
                }
                String arrayContent[] = str.split("#");
                String tempAge[];
                PersonRelation personRelation = new PersonRelation();
                for (int i = 0; i < arrayContent.length; i++) {
                    String temp = arrayContent[i];
                    switch (i) {
                        case 0:
                            tempAge = temp.split("-");
                            personRelation.minAge = Integer.parseInt(tempAge[0]);
                            personRelation.maxAge = Integer.parseInt(tempAge[1]);
                            break;
                        case 1:
                            if (temp.equals("不限")) {
                                personRelation.relationType = PersonRelation.ANY_RELATION;

                            } else if (temp.equals("男男")) {
                                personRelation.relationType = PersonRelation.BOY_BOY;
                            } else if (temp.equals("女女")) {
                                personRelation.relationType = PersonRelation.GIRL_GIRL;
                            } else if (temp.equals("男女") ||
                                    temp.equals("女男")) {
                                personRelation.relationType = PersonRelation.BOY_GIRL;
                            }
                            break;
                        case 2:
                            personRelation.relationDescribe = temp;
                            break;
                        case 3:
                            if (temp.contains("*")) {
                                tempAge = temp.split("\\*");
                                if (tempAge[0].equals("女")) {
                                    personRelation.smallAge = GIRL_TYPE;

                                } else if (tempAge[0].equals("男")) {
                                    personRelation.smallAge = BOY_TYPE;
                                }
                                temp = tempAge[1];
                            }
                            tempAge = temp.split("-");
                            personRelation.smallPersonMinAge = Short.parseShort(tempAge[0]);
                            personRelation.smallPersonMaxAge = Short.parseShort(tempAge[1]);
                            break;
                        case 4:
                            tempAge = temp.split("\\*");
                            personRelation.describes = new ArrayList<>(6);

                            for (int j = 0; j < tempAge.length; j += 2) {
                                PersonRelation.PersonDescribe personDescribe =
                                        new PersonRelation.PersonDescribe();
                                personDescribe.describe = tempAge[j];
                                personDescribe.songId = tempAge[j + 1];
//                                Log.d(TAG, "readDataFile: " + personDescribe.describe);
//                                Log.d(TAG, "readDataFile: " + personDescribe.songId);
                                personRelation.describes.add(personDescribe);
                            }
                            break;
                    }
                }


                personRelations.add(personRelation);
            }

        } catch (IOException e) {
//            personRelations = null;
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return personRelations;
    }


}
