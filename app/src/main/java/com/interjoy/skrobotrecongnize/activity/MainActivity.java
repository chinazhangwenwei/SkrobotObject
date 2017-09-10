package com.interjoy.skrobotrecongnize.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.AssetManager;
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
import com.interjoy.skfacesdk.ImageCallback;
import com.interjoy.skfacesdk.SKFaceSDK;
import com.interjoy.skrobotrecongnize.R;
import com.interjoy.skrobotrecongnize.bean.FaceBean;
import com.interjoy.skrobotrecongnize.bean.PersonRelation;
import com.interjoy.skrobotrecongnize.media.MediaManager;
import com.interjoy.skrobotrecongnize.util.ToastUtil;
import com.interjoy.skrobotrecongnize.util.Utils;
import com.interjoy.skrobotrecongnize.widget.CTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.interjoy.skrobotrecongnize.activity.LaunchActivity.hScreen;
import static com.interjoy.skrobotrecongnize.activity.LaunchActivity.wScreen;
import static com.interjoy.skrobotrecongnize.util.Utils.IS_DEBUG;
import static com.interjoy.skrobotrecongnize.util.Utils.readDataFile;

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


    private Timer mTimer;
    private SKFaceSDK mSKFaceSDK;
    private AssetManager assetManager;
    private MediaManager mediaManager;
    private ExecutorService threadPool;


    public int RADIUS = 60;
    public int DIAMETER = RADIUS << 1;
    public final static int TEXT_SIZE = 26;
    public static int TEXT_PADDING = 10;
    public final static int GIRL_TYPE = 1;
    public final static int BOY_TYPE = 2;
    private FaceBean currentFaceBean;
    private FaceBean tempFaceBean;
    private Semaphore lock;
    public Typeface TEXT_TYPE;
    private volatile int currentIndex;
    //    private int tempIndex;
    private String sayVoice[];
    private String sayChangeVoice[];
    private String sayNoPersonVoice[];
    private String sayThreeVoice[];
    private String sayFourVoice[];
    private List<PersonRelation> sayTwoPerson;
    private volatile String sayTwoPersonContent;
    private TextView tvSmallAge;


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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }

        setContentView(R.layout.activity_main);

        initSKSdk();


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        delayedHide(100);
    }

    private void initData() {
        sayVoice = getResources().getStringArray(R.array.say_one_person_voices);
        sayChangeVoice = getResources().getStringArray(R.array.say_person_change_voices);
        sayNoPersonVoice = getResources().getStringArray(R.array.say_no_person_voices);
        sayThreeVoice = getResources().getStringArray(R.array.say_three_person_voices);
        sayFourVoice = getResources().getStringArray(R.array.say_four_person_voices);
        sayTwoPerson = readDataFile(MainActivity.this);

    }

    private void initListener() {
        findViewById(R.id.iv_close).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
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

        //TODO:创建用于保存人脸照片和必要配置文件的路径
        String dirPath = Utils.createFilePath();
        if (TextUtils.isEmpty(dirPath)) {
            ToastUtil.showToast("Create Face file in SdCard failed!");
            finish();
            return;
        }
        if (!Utils.copyDataM(MainActivity.this, dirPath)) {
            ToastUtil.showToast("Create Model file in SdCard failed!");
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
        mSKFaceSDK = new SKFaceSDK();


        mSKFaceSDK.NativeCreateObject(dirPath);
        threadPool = Executors.newFixedThreadPool(1);
        RADIUS = (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP,
                RADIUS, getResources().getDisplayMetrics());
        DIAMETER = RADIUS << 1;
        TEXT_PADDING = (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP,
                10, getResources().getDisplayMetrics());
        assetManager = getAssets();
        mediaManager = new MediaManager();
        mediaManager.setMediaListener(new MediaManager.MediaListener() {
            @Override
            public void startPlay() {
                isPlaying = true;

                startAnimLeft();
            }

            @Override
            public void endPlay() {
                isPlaying = false;
                closeAnimRight();

            }

            @Override
            public void errorPlay() {
                isPlaying = false;
            }
        });

        mTimer = new Timer(true);
        mTimer.schedule(new MyTimeTask(), 200, 100);
        lock = new Semaphore(1, true);

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

    /**
     * 初始化view
     */
    public void initView() {
        try {
            TEXT_TYPE = Typeface.createFromAsset(getAssets(), "katong.ttf");
        } catch (Exception e) {
            Log.d(TAGS, "加载第三方字体失败。");
            TEXT_TYPE = null;
        }

        tvContent = (TextView) findViewById(R.id.tv_content);
        if (TEXT_TYPE != null) {
            tvContent.setTypeface(TEXT_TYPE);
        }
        tvSmallAge = (TextView) findViewById(R.id.small_age);

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
        final SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
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

    //TODO:该方法在surfaceView中调用

    private void startCamera(SurfaceHolder holder) {
        if (mCamera == null) {
            int mCameraCount = Camera.getNumberOfCameras();
            int curCameraIndex;
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
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);// 设置显示面板控制器
                mCamera.setPreviewCallback(new MyPreviewCallBack());// 设置预览回调函数
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class MyPreviewCallBack implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            {
                if (flag) {//TODO 等待收到返回值后再发下一张图片
                    flag = false;
                    if (mCamera == null) {
                        return;
                    }
                    Camera.Size size = mCamera.getParameters().getPreviewSize();
                    //TODO:上传图像数据到SDK,参数依次是Camera采集的：YUV数据，宽，高，识别结果的回调
                    mSKFaceSDK.pictureRecognition(data, size.width, size.height, new ImageCallback() {
                        @Override
                        public void recognitionInfo(String s) {
                            threadPool.execute(new MyTask(s));
                        }
                    });
                }
            }
        }
    }

    /**
     * 处理数据任务类//在子线程操作数据
     */
    class MyTask implements Runnable {
        private String content;

        public MyTask(String content) {
            this.content = content;
        }

        private void sleepThread() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (null != content) {
                FaceBean result = JSON.parseObject(content, FaceBean.class);
                if (result.getFaces() != null && result.getFaces().size() > 0) {
                    try {
                        lock.acquire();
                        currentFaceBean = result;
                        isHaveResult = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.release();
                    }
                    List<FaceBean.FacesEntity> faces = result.getFaces();
                    int ages = faces.get(0).getAge();
                    if (ages > 0) {
                        StringBuilder content = new StringBuilder();
                        content.append(result.getFaces().size());
                        content.append("个人");
                        startAnimString(-1, ages, content.toString());
                    }


                    for (FaceBean.FacesEntity face : faces) {
                        int age = face.getAge();

                        if (age > 0 && age < 5) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_0);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_0, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_0, age, null);
                                    }
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_0);

//                                    startAnimString(R.string.boy_decorate_0, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_0, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_0, age, null);
                                    }
                                    sleepThread();
                                    break;
                            }
                        } else if (age >= 5 && age < 12) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_1);

//                                    startAnimString(R.string.girl_decorate_1, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_1, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_1, age, null);
                                    }
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_1);
//                                    startAnimString(R.string.boy_decorate_1, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_1, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_1, age, null);
                                    }
                                    sleepThread();
                                    break;
                            }

                        } else if (age >= 12 && age < 19) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_2);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_2, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_2, age, null);
                                    }
//                                    startAnimString(R.string.girl_decorate_2, age, null);
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_2);

                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_2, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_2, age, null);
                                    }
//                                    startAnimString(R.string.boy_decorate_2, age, null);
                                    sleepThread();
                                    break;
                            }
                        } else if (age >= 19 && age < 31) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_3);

//                                    startAnimString(R.string.girl_decorate_3, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_3, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_3, age, null);
                                    }
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_3);

//                                    startAnimString(R.string.boy_decorate_3, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_3, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_3, age, null);
                                    }
                                    sleepThread();
                                    break;
                            }

                        } else if (age >= 31 && age < 43) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_4);

//                                    startAnimString(R.string.girl_decorate_4, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_4, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_4, age, null);
                                    }
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_4);

//                                    startAnimString(R.string.boy_decorate_4, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_4, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_4, age, null);
                                    }
                                    sleepThread();
                                    break;
                            }

                        } else if (age >= 43 && age < 65) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_5);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_5, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_5, age, null);
                                    }
//                                    startAnimString(R.string.girl_decorate_5, age, null);
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_5);
//                                    startAnimString(R.string.boy_decorate_5, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_5, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_5, age, null);
                                    }
                                    sleepThread();
                                    break;
                            }

                        } else if (age >= 65) {
                            switch (face.getSex()) {
                                case GIRL_TYPE:
//                                    startAnimString(R.string.girl_name_6);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.girl_glass_decorate_6, age, null);
                                    } else {
                                        startAnimString(R.string.girl_decorate_6, age, null);
                                    }
//                                    startAnimString(R.string.girl_decorate_6, age, null);
                                    sleepThread();
                                    break;
                                case BOY_TYPE:
//                                    startAnimString(R.string.boy_name_6);
//                                    startAnimString(R.string.boy_decorate_6, age, null);
                                    if (face.getGlass() == 1 || face.getGlass() == 2) {
                                        startAnimString(R.string.boy_glass_decorate_6, age, null);
                                    } else {
                                        startAnimString(R.string.boy_decorate_6, age, null);
                                    }
                                    sleepThread();

                                    break;
                            }
                        }
                    }
                } else {
                    currentFaceBean = null;
                    isHaveResult = false;
                }
                flag = true;
            }
        }
    }


    private int widthQzone;
    private int heightQzone;

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

                if (checkViewCount() && !checkChange()) {
                    return;
                }
                final CTextView textView = new CTextView(MainActivity.this);
                textView.setAge(age);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DIAMETER, DIAMETER);
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
        return fmRoot.getChildCount() > 8;

    }

    private int time0 = 0;
    private int time1 = 0;
    private int time2 = 0;
    private int time3 = 0;


    private static final int T0 = 5000;//5秒,空闲时间；
    private static final int T1 = 1000;//1.5秒，检查结果成功；
    private static final int T2 = 3000;//3秒，播放后检查人脸没有变化；
    private static final int T3 = 2000;//3秒，检查一直不说话（人脸特征一直变化）；


    private volatile boolean isHaveResult = false;
    private volatile boolean isPlaying = false;
    private boolean isPlayEnd = false;
    private static String TAGS = "MyTimeTask";
    private int noPersonCount = 0;
    private int twoPersonCount = 0;
    private int changePersonCount = 0;

    class MyTimeTask extends TimerTask {
        @Override
        public void run() {
            time0 += 100;
            time1 += 100;
            time2 += 100;
            time3 += 100;
            if (isPlaying) {
                time0 = 0;
                time1 = 0;
                time2 = 0;
                time3 = 0;
                return;
            }
            checkViewCount();
            if (isHaveResult) {
                time0 = 0;
                if (checkChange()) {//检查是否变化1.5秒，发生变化从新计时，说明识别不稳定；
                    time1 = 0;
                    isPlayEnd = false;
                    if (time3 >= T3) {//检查一直移动，则播放不要移动等语音
                        time1 = 0;
                        time2 = 0;
                        time3 = 0;
                        currentIndex = (changePersonCount % 4) + 125;
                        mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                        Log.d(TAGS, "run: 不要动");
                    }
                } else {//没有发生变化持续1.5秒，播放识别结果语音；
                    if (isPlayEnd) {
                        if (time2 >= T2) {//安静三秒之后
                            time2 = 0;
                            time1 = 0;
                            time3 = 0;
                            int size = currentFaceBean.getFaces().size();
                            switch (size) {
                                case 1:
                                    currentIndex = judgeOneAge(currentFaceBean.getFaces().get(0));
                                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                                    break;
                                case 2:
                                    twoPersonCount++;
                                    PersonRelation personRelation = judgeTwoPerson(currentFaceBean);
                                    if (personRelation != null) {
                                        String mp3 = "109";
                                        if (personRelation != null) {
                                            int desSize = personRelation.describes.size();
                                            if (desSize == 1) {
                                                mp3 = personRelation.describes.get(0).songId;
                                                sayTwoPersonContent = personRelation.describes.get(0).describe;
                                            } else {
                                                int index = twoPersonCount % desSize;
                                                mp3 = personRelation.describes.get(index).songId;
                                                sayTwoPersonContent = personRelation.describes.get(index).describe;

                                            }
                                        }
                                        currentIndex = -1;
                                        mediaManager.playMusic(assetManager, mp3 + ".mp3");
                                    }
                                    break;
                                case 3:
                                    currentIndex = judgeThreeAge(currentFaceBean);
                                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                                    break;
                                case 4:
                                    currentIndex = judgeFourAge(currentFaceBean);
                                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                                    break;
                            }
                            Log.d(TAGS, "run: 重复播放识别结果");
                        }
                    } else {
                        if (time1 >= T1) {
                            time1 = 0;
                            time2 = 0;

                            isPlayEnd = true;
//                            startAnimLeft();

                            int size = currentFaceBean.getFaces().size();
                            Log.d(TAGS, "run:几个人 " + size);
                            switch (size) {
                                case 1:
                                    currentIndex = judgeOneAge(currentFaceBean.getFaces().get(0));
                                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                                    break;
                                case 2:

                                    twoPersonCount++;
                                    PersonRelation personRelation = judgeTwoPerson(currentFaceBean);
                                    if (personRelation != null) {
                                        String mp3 = "109";
                                        if (personRelation != null) {
                                            int desSize = personRelation.describes.size();
                                            if (desSize == 1) {
                                                mp3 = personRelation.describes.get(0).songId;
                                                sayTwoPersonContent = personRelation.describes.get(0).describe;
                                            } else {
                                                int index = twoPersonCount % desSize;
                                                mp3 = personRelation.describes.get(index).songId;
                                                sayTwoPersonContent = personRelation.describes.get(index).describe;
                                            }
                                        }
                                        currentIndex = -1;
                                        mediaManager.playMusic(assetManager, mp3 + ".mp3");
                                    }
                                    break;
                                case 3:
                                    currentIndex = judgeThreeAge(currentFaceBean);
                                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                                    break;
                                case 4:
                                    currentIndex = judgeFourAge(currentFaceBean);
                                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                                    break;
                            }
                            Log.d(TAGS, "run: 播放识别结果");
                        }
                    }
                }
            } else {
                time1 = 0;
                time2 = 0;
                time3 = 0;
                tempFaceBean = null;
                if (time0 >= T0) {
                    time0 = 0;
                    //播放吸引路人的音乐
                    isPlayEnd = false;

                    noPersonCount++;
                    currentIndex = (noPersonCount % 4) + 121;
//                    currentIndex = judgeOneAge(currentFaceBean.getFaces().get(0));
                    mediaManager.playMusic(assetManager, currentIndex + ".mp3");
                    Log.d(TAGS, "run: 一直没有识别结果");
                }
            }
        }

    }

    int count = 0;//计数，识别结果多次变化才算真正的变化。是一种策略

    private boolean checkChange() {
        if (!isHaveResult) {
            return false;
        }
        boolean isChange = false;
        if (tempFaceBean == null) {
            tempFaceBean = currentFaceBean;
            return isChange;
        }
        if (currentFaceBean.getFaces().size()
                != tempFaceBean.getFaces().size()) {
            count += 1;
            if (count == 2) {
                count = 0;
                tempFaceBean = currentFaceBean;
                isChange = true;
            }
        }
        return isChange;

    }

    private void startAnimLeft() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentIndex == -1) {
                    if (!TextUtils.isEmpty(sayTwoPersonContent)) {
                        tvContent.setText(sayTwoPersonContent);
                    }
                }
                if (currentIndex >= 1 && currentIndex <= 26) {
                    String content = sayVoice[currentIndex - 1];
                    tvContent.setText(content);
//                    currentIndex = -1;
                } else if (109 <= currentIndex && currentIndex < 117) {
                    String content = sayThreeVoice[currentIndex - 109];
                    tvContent.setText(content);
//                    currentIndex = -1;
                } else if (117 <= currentIndex && currentIndex <= 120) {
                    String content = sayFourVoice[currentIndex - 117];
                    tvContent.setText(content);
//                    currentIndex = -1;

                } else if (120 < currentIndex && currentIndex <= 124) {
                    String content = sayNoPersonVoice[currentIndex - 121];
                    tvContent.setText(content);
//                    currentIndex = -1;
                } else if (124 < currentIndex && currentIndex <= 128) {
                    String content = sayChangeVoice[currentIndex - 125];
                    tvContent.setText(content);
//                    currentIndex = -1;
                }


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

    //处理一个人判断逻辑

    int onPerson = 0;
    int threePerson = 0;

    private PersonRelation judgeTwoPerson(FaceBean face) {
        PersonRelation personRelation = null;
        if (face == null) {
            return null;
        }
        List<FaceBean.FacesEntity> faces = face.getFaces();
        if (faces.size() < 1) {
            return null;
        }
        FaceBean.FacesEntity face1 = faces.get(0);
        FaceBean.FacesEntity face2 = faces.get(1);
        int age1 = face1.getAge();
        int age2 = face2.getAge();
        int sex1 = face1.getSex();
        int sex2 = face2.getSex();
        int distanceAge = Math.abs(age1 - age2);
        if (age1 >= age2) {
            personRelation = checkTwoPersonRelation(distanceAge, face2, sex1, sex2);
            setDebugText("两个人 最大年龄：" + age1 + "最小年龄：" + age2);
        } else {
            personRelation = checkTwoPersonRelation(distanceAge, face1, sex1, sex2);
            setDebugText("两个人 最大年龄：" + age2 + "最小年龄：" + age1);
        }

        return personRelation;
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


    private PersonRelation checkTwoPersonRelation(int distanceAge, FaceBean.FacesEntity smallFace,
                                                  int sex1, int sex2) {
        short personType = 1;
        int smallAge = smallFace.getAge();
        switch (sex1) {
            case GIRL_TYPE:
                switch (sex2) {
                    case GIRL_TYPE:
                        personType = PersonRelation.GIRL_GIRL;
                        break;
                    case BOY_TYPE:
                        personType = PersonRelation.BOY_GIRL;
                        break;
                }
                break;
            case BOY_TYPE:
                switch (sex2) {
                    case GIRL_TYPE:
                        personType = PersonRelation.BOY_GIRL;
                        break;
                    case BOY_TYPE:
                        personType = PersonRelation.BOY_BOY;
                        break;
                }
                break;
        }
        List<PersonRelation> distancePersons = checkDistanceList(distanceAge);
        if (0 <= distanceAge && distanceAge <= 3) {
            if (smallAge <= 19) {
                personType = PersonRelation.ANY_RELATION;
            }
            for (PersonRelation person : distancePersons) {
                if (person.smallPersonMinAge <= smallAge &&
                        smallAge <= person.smallPersonMaxAge) {
                    if (person.relationType == personType) {
                        return person;
                    }
                }
            }

        } else if (11 <= distanceAge && distanceAge <= 20) {
            for (PersonRelation person : distancePersons) {
                if (person.smallPersonMinAge <= smallAge &&
                        smallAge <= person.smallPersonMaxAge) {
                    return person;
                }
            }
        }
        for (PersonRelation person : distancePersons) {
            if (person.smallPersonMinAge <= smallAge &&
                    smallAge <= person.smallPersonMaxAge) {
                if (person.relationType == personType) {
                    if (personType == PersonRelation.BOY_GIRL) {
                        if (person.smallAge == smallFace.getGlass()) {
                            return person;
                        }
                    } else {
                        return person;
                    }
                }
            }
        }

        return null;


    }

    private HashMap<String, List<PersonRelation>> distanceMapData;

    private List<PersonRelation> checkDistanceList(int distanceAge) {
        if (distanceMapData == null) {
            distanceMapData = new HashMap<>();
        }
        List<PersonRelation> tempPersons = distanceMapData.get(distanceAge + "");
        if (tempPersons != null) {
            return tempPersons;
        } else {
            tempPersons = new ArrayList<>();
        }


        if (0 <= distanceAge && distanceAge <= 3) {
            for (PersonRelation temp : sayTwoPerson) {
                if (temp.minAge == 0) {
                    tempPersons.add(temp);
                }
            }
        } else if (4 <= distanceAge && distanceAge <= 10) {
            for (PersonRelation temp : sayTwoPerson) {
                if (temp.minAge == 4) {
                    tempPersons.add(temp);
                }
            }

        } else if (11 <= distanceAge && distanceAge <= 20) {
            for (PersonRelation temp : sayTwoPerson) {
                if (temp.minAge == 11) {
                    tempPersons.add(temp);
                }
            }

        } else if (21 <= distanceAge && distanceAge <= 40) {
            for (PersonRelation temp : sayTwoPerson) {
                if (temp.minAge == 21) {
                    tempPersons.add(temp);
                }
            }

        } else if (41 <= distanceAge) {
            for (PersonRelation temp : sayTwoPerson) {
                if (temp.minAge == 41) {
                    tempPersons.add(temp);
                }
            }

        }
        distanceMapData.put(distanceAge + "", tempPersons);

        return tempPersons;
    }

    private int judgeFourAge(FaceBean face) {
        int result = -1;
        threePerson++;
        List<FaceBean.FacesEntity> faces = face.getFaces();
        int size = faces.size();
        int ages[] = new int[size];
        for (int i = 0; i < size; i++) {
            ages[i] = faces.get(i).getAge();
        }
        Arrays.sort(ages);
        int max = ages[size - 1];
        int min = ages[0];

        StringBuilder sb = new StringBuilder("多个人的年龄 最大年龄：");
        sb.append(max).append("最小年龄：").append(min);
        setDebugText(sb.toString());


//        Log.d(TAGS, "judgeThreeAge4: varance" + age1 + "age" + age2 + "age" + age3 + "age" + age4);
//        int mean = (age1 + age2 + age3 + age4) / 4;
//
//        int variance = (age1 - mean) * (age1 - mean) + (age2 - mean) * (age2 - mean)
//                + (age3 - mean) * (age3 - mean) + (age4 - mean) * (age4 - mean);
//        Log.d(TAGS, "judgeThreeAge4: varance" + variance);
//        variance = variance / 4;
//        //计算方差
//        Log.d(TAGS, "judgeThreeAge4: varance" + variance);
        if (max >= 28 && min <= 18) {
            //一家人
            result = threePerson % 5 + 112;

        } else {
            //很多人
            result = threePerson % 4 + 117;
        }

        return result;

    }


    private int judgeThreeAge(FaceBean face) {
        int result = -1;
        threePerson++;
        List<FaceBean.FacesEntity> faces = face.getFaces();
        int age1 = faces.get(0).getAge();
        int age2 = faces.get(1).getAge();
        int age3 = faces.get(2).getAge();
        int max = age2;
        int min = age2;
        if (age1 > age2) {
            max = age1;
        } else {
            min = age1;
        }
        if (max < age3) {
            max = age3;
        }
        if (min > age3) {
            min = age3;
        }
        StringBuilder sb = new StringBuilder("三个人的年龄 最大：");
        sb.append(max).append("最小年龄：").append(min).append("age1:").append(age1).append("age2:").
                append(age2).append("age3:").append(age3);
        setDebugText(sb.toString());
        if (max >= 28 && min <= 18) {
            //一家人
            result = threePerson % 5 + 112;

        } else if (max <= 20) {
            //朋友
            result = threePerson % 2;
            if (result == 0) {
                result = 109;
            } else {
                result = 111;
            }

        } else {
            //同事或朋友
            result = threePerson % 3 + 109;

        }


        return result;
    }

    private int judgeOneAge(FaceBean.FacesEntity face) {
        int result = -1;
        if (onPerson > 10000) {
            onPerson = 0;
        }
        onPerson++;
        int age = -1, sex = -1;
        try {
            lock.acquire();
            age = face.getAge();
            sex = face.getSex();
        } catch (InterruptedException e) {
            e.printStackTrace();
            age = -1;
        } finally {
            lock.release();
        }
        if (age == -1) {
            return result;
        }
        setDebugText("一个人年龄：" + age);
        switch (sex) {
            case BOY_TYPE:
                if (0 < age && age < 13) {
                    result = onPerson % 3 + 1;
                } else if (13 <= age && age < 19) {
                    result = onPerson % 3 + 4;
                } else if (19 <= age && age < 36) {
                    result = onPerson % 2 + 7;
                } else if (36 <= age && age < 55) {
                    result = onPerson % 3 + 9;
                } else if (56 <= age) {
                    result = onPerson % 2 + 12;
                }
                break;
            case GIRL_TYPE:
                if (0 < age && age < 13) {
                    result = onPerson % 3 + 14;
                } else if (13 <= age && age < 19) {
                    result = onPerson % 3 + 17;
                } else if (19 <= age && age < 36) {
                    result = onPerson % 3 + 20;
                } else if (36 <= age && age < 55) {
                    result = onPerson % 2 + 23;
                } else if (56 <= age) {
                    result = onPerson % 2 + 25;
                }
                break;
        }

        return result;
    }

    @Override
    protected void onDestroy() {
        if (mCamera != null) {
//            mCamera.stopPreview();
//            try {
//                mCamera.release();
//            }catch ()
            mCamera = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
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
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
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
