package com.interjoy.skrobotobject.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.interjoy.skeyesdk.SKEyeSDK;
import com.interjoy.skrobotobject.R;
import com.interjoy.skrobotobject.bean.ObjectsDescribe;
import com.interjoy.skrobotobject.bean.ResultTag;
import com.interjoy.skrobotobject.bean.ResultTags;
import com.interjoy.skrobotobject.media.MediaManager;
import com.interjoy.skrobotobject.util.OkHttpUtil;
import com.interjoy.skrobotobject.util.ToastUtil;
import com.interjoy.skrobotobject.util.Utils;
import com.interjoy.skrobotobject.widget.CTextView;
import com.interjoy.skrobotobject.widget.LineView;
import com.interjoy.skutils.ConstConfig;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.interjoy.skrobotobject.activity.LaunchActivity.hScreen;
import static com.interjoy.skrobotobject.activity.LaunchActivity.wScreen;
import static com.interjoy.skrobotobject.util.Utils.IS_DEBUG;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private boolean flag = true;

    private TextView tvContent;
    private FrameLayout fmRoot;
    private RelativeLayout reBottom;

    private volatile boolean isPlaying = false;


    public int RADIUS = 60;
    public int DIAMETER = RADIUS << 1;
    public final static int TEXT_SIZE = 26;
    public static int TEXT_PADDING = 10;


    public Typeface TEXT_TYPE;

    private int widthQzone;
    private int heightQzone;
    private SKEyeSDK skEyeSDK;
    private MediaManager mediaManager;
    private ExecutorService threadPool;
    private ImageView ivSwitch;

    private TextView tvSmallAge;

    private String api_key = "5487bf5feeafcf8d935b16435b693086";//APP KEY
    private String api_secret = "55b3a6567f7a24a675ade01fb163f178";//APP SECRET


    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     * 全屏设置逻辑
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            fmRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    //    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initSKSdk();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        delayedHide(100);
    }

    private void initData() {


    }

    private String getLocalFilePath(String url) {

        String path = Utils.getPath(MainActivity.this);
        File localMp3File = new File(path, url);
        if (localMp3File.exists()) {
            return localMp3File.getAbsolutePath();
        }
        return null;

    }

    private void initListener() {
        findViewById(R.id.iv_close).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        ivSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera == null) {
                    return;
                }

                if (curCameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    curCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;

                } else {
                    startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    curCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }

            }
        });
    }

    private void initSKSdk() {
        // 检测摄像头个数
        if (Camera.getNumberOfCameras() < 1) {
            ToastUtil.showToast(R.string.no_camera);
            finish();
            return;
        }

        //检测身份认证是否OK
        if (!Utils.isNetworkConnected(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("请在网络连接正常后重新打开应用。");
            builder.setTitle("提示");
            builder.setCancelable(false);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.create().show();
            return;
        }

        //TODO：初始化识别SDK
        skEyeSDK = new SKEyeSDK(getApplicationContext());
        skEyeSDK.SKEyeSDKInit(api_key, api_secret);

        threadPool = Executors.newFixedThreadPool(1);
        RADIUS = (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP,
                RADIUS, getResources().getDisplayMetrics());
        DIAMETER = RADIUS << 1;
        TEXT_PADDING = (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP,
                10, getResources().getDisplayMetrics());

        mediaManager = new MediaManager();
        mediaManager.setMediaListener(new MediaManager.MediaListener() {
            @Override
            public void startPlay() {
                isPlaying = true;

            }

            @Override
            public void endPlay() {
                isPlaying = false;
                closeAnimRight();
            }

            @Override
            public void errorPlay() {
                isPlaying = false;
                closeAnimRight();
            }
        });


        initView();
        initListener();
        initData();
    }


    /**
     * 相机初始化
     */
    private void initCamera() {
        if (mCamera != null) {
            try {
                Camera.Parameters params = mCamera.getParameters();
                if (getResources().getConfiguration().orientation !=
                        Configuration.ORIENTATION_LANDSCAPE) {
                    params.set("orientation", "portrait");
                    mCamera.setDisplayOrientation(90);
                    params.setRotation(90);
                } else {
                    params.set("orientation", "landscape");
                    mCamera.setDisplayOrientation(0);
                    params.setRotation(0);

                }

                Log.d(TAG, "initCamera: " + params.getMaxZoom());
                params.setPreviewSize(640, 480);
//                params.setZoom(0);
                // 实现自动对焦
                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains("continuous-video")) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                mCamera.setParameters(params);
                mCamera.getParameters().setPreviewFormat(ImageFormat.NV21);
                // 打开预览画面
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private SurfaceHolder surfaceHolder;

    /**
     * 初始化view
     */
    public void initView() {
        try {
            TEXT_TYPE = Typeface.createFromAsset(getAssets(), "katong.ttf");
        } catch (Exception e) {
            TEXT_TYPE = null;
        }

        tvContent = (TextView) findViewById(R.id.tv_content);
        if (TEXT_TYPE != null) {
            tvContent.setTypeface(TEXT_TYPE);
        }
        tvSmallAge = (TextView) findViewById(R.id.small_age);
        ivSwitch = (ImageView) findViewById(R.id.iv_switch);
        if (!checkCamerCount()) {
            ivSwitch.setVisibility(View.INVISIBLE);
        }

        mSurfaceView = (SurfaceView) findViewById(R.id.sv_camera);
        fmRoot = (FrameLayout) findViewById(R.id.fm_root);
        reBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        fmRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (wScreen > 900 && (getResources().getDisplayMetrics().densityDpi <= 160)) {
            ((ImageView) findViewById(R.id.iv_close)).setImageResource(R.drawable.d_close);
            ((ImageView) findViewById(R.id.iv_logo)).setImageResource(R.drawable.d_skeye);
        }
        Log.d(TAG, "initView: " + getResources().getDisplayMetrics().densityDpi);
        float ratio = 1.0f * wScreen / hScreen;

        int wView, hView;
        if (ratio >= 0.75f) {
            wView = wScreen;
            hView = (int) (0.75f * wScreen + 0.5f);
        } else {
            hView = hScreen;
            wView = (int) (1.25f * hScreen + 0.5f);
        }
        widthQzone = wScreen - DIAMETER;
        heightQzone = hScreen - DIAMETER - RADIUS;

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(wView, hView);// 获取布局
        mSurfaceView.setLayoutParams(lp); // 设置修改后的布局
        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCamera(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera != null) {
                    mCamera.startPreview();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (null != mCamera) {
                    mCamera.stopPreview();
                }
            }
        });
    }

    private boolean checkCamerCount() {
        int mCameraCount = Camera.getNumberOfCameras();
        if (mCameraCount > 1) {
            return true;
        } else {
            return false;
        }
    }

    private MyPreviewCallBack myPreviewCallBack;

    private void startCamera(int cameraCount) {

        try {
//            surfaceHolder.removeCallback(myPreviewCallBack);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();

            mCamera = null;
            mCamera = Camera.open(cameraCount);
            if (mCamera == null) {
                ToastUtil.showToast("Open Camera Failed!!!");
                return;
            }
            initCamera();

        } catch (Exception exception) {
            exception.printStackTrace();
            if (mCamera == null) {//特别提醒：API>23以后Android需要动态申请Camera权限
                ToastUtil.showToast("获取Camera权限失败了！！！");
                return;
            } else {
                mCamera.release();
                mCamera = null;
            }
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(surfaceHolder);// 设置显示面板控制器
                mCamera.setPreviewCallback(myPreviewCallBack);// 设置预览回调函数
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    int curCameraIndex;
    //TODO:该方法在surfaceView中调用

    private void startCamera(SurfaceHolder holder) {
        if (mCamera == null) {
            int mCameraCount = Camera.getNumberOfCameras();

            try {
                if (mCameraCount > 1) {
                    curCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    curCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                mCamera = Camera.open(curCameraIndex);
                if (mCamera == null) {
                    ToastUtil.showToast("Open Camera Failed!!!");
                    return;
                }
                initCamera();

            } catch (Exception exception) {
                exception.printStackTrace();
                if (mCamera == null) {//特别提醒：API>23以后Android需要动态申请Camera权限
                    ToastUtil.showToast("获取Camera权限失败了！！！");
                    return;
                } else {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
        if (myPreviewCallBack == null) {
            myPreviewCallBack = new MyPreviewCallBack();
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);// 设置显示面板控制器
                mCamera.setPreviewCallback(myPreviewCallBack);// 设置预览回调函数
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private int preViewCount = 0;

    class MyPreviewCallBack implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            {

//                preViewCount++;
//                if (preViewCount < 2) {
//                    return;
//                }
                preViewCount = 0;

                if (flag) {//TODO 等待收到返回值后再发下一张图片
                    flag = false;
                    if (mCamera == null) {
                        return;
                    }
                    Camera.Size size = mCamera.getParameters().getPreviewSize();
                    //TODO:上传图像数据到SDK,参数依次是Camera采集的：YUV数据，宽，高，识别结果的回调

                    sendYuvImageToServer(data, size.width, size.height);
                }
            }
        }
    }


    private byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;

        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }

        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }

//        saveImage(yuv, imageWidth, imageHeight);
        return yuv;

    }

    private ResultTag tempResult;
    private volatile int countResult = 0;
    private static final int COUNT_STRATEGY = 3;


    /**
     * 发送YUV图像
     *
     * @param yuvData yuv图像的byte数组
     * @param width   图像宽度
     * @param height  图像高度
     */
    private void sendYuvImageToServer(final byte[] yuvData, final int width,
                                      final int height) {
        // 获取YUV数据数组
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 直接调用
                    String response = skEyeSDK.SKEyeSDK_Image(ConstConfig.SKEyeSDK_SERVICE_NAME_OBJECT,
                            yuvData, width, height);
                    ResultTags result = JSON.parseObject(response, ResultTags.class);

                    if (result != null) {
                        List<ResultTag> resultTags = result.getTags();
                        if (resultTags != null && resultTags.size() > 0) {
                            int size = resultTags.size();
                            if (size > 3) {
                                size = 3;
                            }
                            for (int i = 0; i < size; i++) {
                                startAnimString(-1, 28, resultTags.get(i).getTag());
                            }
                            ResultTag resultTag = resultTags.get(0);
                            if (tempTag != null && tempTag.equals(resultTag)) {
                                repeatResult++;
                                if (repeatResult == COUNT_REPEAT_STRATEGY) {
                                    repeatResult = 0;
                                    if (!isPlaying) {
                                        isPlaying = true;
                                        mediaManager.playMusic(tempDescribe.getData().getType_audio());
//                                        mediaManager.playMusic(getAssets(),"12.wav");
                                        StringBuilder stringBuilder = new
                                                StringBuilder(tempDescribe.getData().getType_name());
                                        stringBuilder.append("  ").append(tempDescribe.getData().getType_english());
                                        startAnimLeft(stringBuilder.toString());
                                    }
                                }

                            } else {
                                if (resultTag.getConfidence() > 30 && resultTag.getConfidence() >= 70) {
                                    countResult = 0;
                                    sendHttpTag(resultTag.getTag());
                                } else {
                                    if (tempResult != null &&
                                            tempResult.getTag().equals(resultTag.getTag())) {
                                        countResult++;
                                    }
                                    if (tempResult != null &&
                                            (!tempResult.getTag().equals(resultTag.getTag()))) {
                                        countResult = 0;
                                        tempResult = null;

                                    }
                                    if (tempResult == null) {
                                        tempResult = resultTag;
                                        countResult++;
                                    }
                                    if (countResult == COUNT_STRATEGY) {
                                        tempResult = null;
                                        countResult = 0;
                                        sendHttpTag(resultTag.getTag());
                                    }
                                }
                            }
                        }

                        switch (result.getError_code()) {
                            case 10002:
                                startAnimString(-1, 28, "网络异常！");
                                break;
                        }

                    }

                    flag = true;
                } catch (Exception e) {
                    Log.d(TAG, "识别出错+run:error ");
                    if (e instanceof SocketTimeoutException) {
                        startAnimString(-1, 28, "网络异常！");
                    }
                    e.printStackTrace();
                    flag = true;
                }
            }
        });
    }

    private ObjectsDescribe tempDescribe;
    private volatile int repeatResult = 0;
    private volatile String tempTag;
    private static final int COUNT_REPEAT_STRATEGY = 4;


    private void sendHttpTag(String tag) throws Exception {

        RequestBody formBody = new FormBody.Builder()
                .add("type_name", tag)
                .build();
        Request request = new Request.Builder()
                .url("http://identifiar.sk-ai.com/Index/robot_speech")
                .post(formBody)
                .build();
        Call call = OkHttpUtil.enqueue(request);
        Response response1 = call.execute();

        if (response1.isSuccessful()) {
            String content = response1.body().string();
            ObjectsDescribe describe =
                    JSON.parseObject(content, ObjectsDescribe.class);
            if (describe.getCode() == 0) {
                if (!isPlaying) {
                    StringBuilder stringBuilder = new
                            StringBuilder(describe.getData().getType_name());
                    stringBuilder.append("  ").append(describe.getData().getType_english());
                    startAnimLeft(stringBuilder.toString());
                    mediaManager.playMusic(describe.getData().getType_audio());
//                    mediaManager.playMusic(getAssets(),"12.wav");
                    isPlaying = true;
                    tempDescribe = describe;
                    tempTag = tag;
                }
            }
        }
    }


    /**
     * 启动泡泡动画
     *
     * @param
     */
    private void startAnimString(@StringRes final int stringId,
                                 final int age, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkViewCount()) {
                    return;
                }
                final CTextView textView = new CTextView(MainActivity.this);
                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(DIAMETER, DIAMETER);
                textView.setLayoutParams(params);
                textView.setPadding(TEXT_PADDING, TEXT_PADDING, TEXT_PADDING, TEXT_PADDING);
                textView.setTextColor(Color.WHITE);
                if (TextUtils.isEmpty(content)) {
                    textView.setText(stringId);
                } else {
                    textView.setText(content);
                }

                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                textView.setOnAnimFinishListener(new CTextView.onAnimFinishListener() {
                    @Override
                    public void onFinish(CTextView textView) {
                        fmRoot.removeView(textView);
                    }
                });

                final LineView lineView = new LineView(MainActivity.this);

//                FrameLayout.LayoutParams lineParams =
//                        new FrameLayout.LayoutParams(DIAMETER, DIAMETER);
                lineView.setLayoutParams(params);
                lineView.setOnAnimFinishListener(new LineView.onAnimFinishListener() {
                    @Override
                    public void onFinish(LineView lineView1) {
                        fmRoot.removeView(lineView1);
                    }
                });


                try {
                    //获取随机数
                    Random randomX = new Random();
//                    ToastUtil.showToast("多宽" + widthQzone + "多高" + heightQzone);
                    int x = randomX.nextInt(widthQzone);
                    Random randomY = new Random();
                    int y = randomY.nextInt(heightQzone);
                    fmRoot.addView(textView);
                    textView.setX(x);
                    textView.setY(y);
                    textView.startAnim();

                    Random randomXL = new Random();
//                    ToastUtil.showToast("多宽" + widthQzone + "多高" + heightQzone);
                    int xL = randomXL.nextInt(widthQzone);
                    Random randomYL = new Random();
                    int yL = randomYL.nextInt(heightQzone);

                    fmRoot.addView(lineView);
                    lineView.setX(xL);
                    lineView.setY(yL);
                    lineView.startAnim();

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private boolean checkViewCount() {
        if (fmRoot == null) {
            return false;
        }
        return fmRoot.getChildCount() > 16;

    }


    private void startAnimLeft(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //设置语音内容
//                String content = sayVoice[currentIndex - 1];
                tvContent.setText(content);
                AnimationSet animationSet = new AnimationSet(true);
                //添加动画
                animationSet.addAnimation(new AlphaAnimation(0.2f, 1.0f));
                animationSet.addAnimation(new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, -1.0f,
                        Animation.ABSOLUTE, 0,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f));
                //设置插值器
                animationSet.setInterpolator(new LinearInterpolator());
                //设置动画持续时长
                animationSet.setDuration(500);
                //设置动画结束之后是否保持动画的目标状态
                animationSet.setFillAfter(true);
                //设置动画结束之后是否保持动画开始时的状态
//                animationSet.setFillBefore(false);
                reBottom.startAnimation(animationSet);
            }

        });


    }

    private void closeAnimRight() {

        AnimationSet animationSet = new AnimationSet(true);
        //添加动画
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.2f));
        animationSet.addAnimation(new TranslateAnimation(
                Animation.ABSOLUTE, 0,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f));
        //设置插值器
        animationSet.setInterpolator(new LinearInterpolator());
        //设置动画持续时长
        animationSet.setDuration(500);
        //设置动画结束之后是否保持动画的目标状态
        animationSet.setFillAfter(true);
        //设置动画结束之后是否保持动画开始时的状态
        animationSet.setFillBefore(false);
        reBottom.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                reBottom.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void setDebugText(final String text) {
        if (!IS_DEBUG) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSmallAge.setText(text);
            }
        });
    }


    @Override
    protected void onDestroy() {
        if (mCamera != null) {

            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        if (mediaManager != null) {
            mediaManager.destory();
        }
        super.onDestroy();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }


    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
