package com.fx.android.watercamerademo.ScreenAdapter;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.fx.android.watercamerademo.R;

public class NotchScreenActivity extends AppCompatActivity {

    private Boolean isNotch = false;// 是否为刘海屏
    private int type;
    RelativeLayout mRlall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_notch_screen);

        mRlall = findViewById(R.id.rl_all);

        String deviceBrand = NotchPhoneUtils.getDeviceBrand(); //获取手机厂商

        if ("vivo".equals(deviceBrand)) {
            isNotch = NotchPhoneUtils.HasNotchVivo(NotchScreenActivity.this);
            type = 1;
        } else if ("HUAWEI".equals(deviceBrand)) {
            isNotch = NotchPhoneUtils.hasNotchAtHuawei(NotchScreenActivity.this);
            type = 2;
        } else if ("OPPO".equals(deviceBrand)) {
            isNotch = NotchPhoneUtils.HasNotchOPPO(NotchScreenActivity.this);
            type = 3;
        }
        NotchPhoneUtils.onConfigurationChanged(NotchScreenActivity.this, isNotch, type, mRlall);
    }

    //屏幕方向发生改变的回调方法
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        NotchPhoneUtils.onConfigurationChanged(NotchScreenActivity.this, isNotch, type, mRlall);
        super.onConfigurationChanged(newConfig);
    }

}
