package com.interjoy.skrobotobject;

import android.app.Application;

/**
 * Title:
 * Description:
 * Company: 北京盛开互动科技有限公司
 * Tel: 010-62538800
 * Mail: support@interjoy.com.cn
 *
 * @author zhangwenwei
 * @date 2017/9/6
 */
public class MyApplication extends Application {

    public static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;

    }
}
