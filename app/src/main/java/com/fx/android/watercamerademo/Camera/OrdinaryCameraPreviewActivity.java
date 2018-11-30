package com.fx.android.watercamerademo.Camera;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.fx.android.watercamerademo.R;
import com.fx.android.watercamerademo.utils.CameraUtils;
import com.fx.android.watercamerademo.utils.FSCameraUtils;
import com.fx.android.watercamerademo.utils.FSScreen;
import com.fx.android.watercamerademo.utils.ImageUtil;
import com.fx.android.watercamerademo.utils.ViewUtils;
import com.fx.android.watercamerademo.utils.WaterMaskVO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class OrdinaryCameraPreviewActivity extends AppCompatActivity
        implements View.OnTouchListener, View.OnClickListener, CaptureSensorsObserver.RefocuseListener, ViewUtils.OnTakePicture {

    public static final String TAG = OrdinaryCameraPreviewActivity.class.getSimpleName();
    //相机Id -1不可用，0后置相机，1前置相机
    private int cameraId = -1;
    private int displayOrientation = -1;
    //相机是否预览
    private boolean isPreview = false;
    //闪光灯是否打开，默认关闭
    private Boolean isOpenFlash=false;
    //是否转换前后相机
    private boolean useFrontFacingCamera = false;
    //拍照咔嚓声
    int soundID = 0;
    //相机方向控制
    private int orientationTag;
    final int ORIGIN = 0;
    final int LEFT = 1;
    final int RIGHT = 2;
    final int TOP = 3;
    private int _rotation = 90;
    private int _rotationfront = -90;

    private OrdinaryCameraPreviewActivity.CaptureOrientationEventListener mCaptureOrientationEventListener;
    //相机框的监听
    private OrdinaryCameraPreviewActivity.MySurfaceTextureListener listener;
    //拍照图片路径列表
    private final ArrayList<String> pathList = new ArrayList<>();
    // 屏幕的宽
    public static int ScreenW;
    // 屏幕的高
    public static int ScreenH;
    //屏幕实际高
    private static  int RealScreenH;
    //屏幕方向观察者
    CaptureSensorsObserver mCaptureSensorsObserver;
    //默认屏幕方向为竖直
    private int orientation = Configuration.ORIENTATION_PORTRAIT;
    //默认点击时间
    protected long mClickTime = 0l;
    // 添加触摸
    private float oldDist = 1f;
    //默认焦点距离
    private int threshold;
    //截图key
    public final static String Screenshot_IMAGE_KEY = "screenshot_image_key";
    //截图位置
    private final static String Screenshot_IMAGE_GRAVITY="screenshot_image_gravity";
    //相机key
    public final static String CAMERA_IMAGE_KEY = "camera_image_key";
    //水印信息
    private final static String CAMERA_IMAGE_WATERMASK ="camera_image_waterlist";
    //屏幕方向
    private final static String PHONE_SCREEN_ORIENTATION ="phone_screen_orientation";
    //相机摄像头方向
    private final static String CAMERA_IMAGE_FRONTANDBACK="camera_device_cameraid";

    public static final String kPhotoPath = "path";

    //Activity 给fragment传值
    private  int CameraModel=1;
    //相机的回调
    //按快门的
    private Camera.ShutterCallback shutterCallback;
    //未经加工的
    private Camera.PictureCallback rawCallback;
    //压缩后的
    private Camera.PictureCallback pictureCallBack;


    //声音池
    private SoundPool mSoundPoll;
    //相机
    private Camera mCamera;
    //自动聚焦
    private Camera.AutoFocusCallback focusCallback;
    //大相机框
    private TextureView textureView;
    //预览框
    private SurfaceTexture mSurfaceTexture;

    //闪光灯
    private ImageView btn_openFlash;
    private TextView tv_name,tv_time,tv_location;

    private String[] flashMedols = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF};
    private int[] modelResId = {R.drawable.flashlight_auto_icn, R.drawable.flashlight_on_icn, R.drawable.flashlight_off_icn};
    int modelIndex = 2;
    //    Camera.Parameters.FLASH_MODE_ON 拍照时打开闪光灯
//    Camera.Parameters.FLASH_MODE_OFF 拍照时始终关闭闪光灯
//    Camera.Parameters.FLASH_MODE_AUTO 系统决定是否开启闪光灯（推荐使用）
//    Camera.Parameters.FLASH_MODE_TORCH 手电筒模式 一直开着闪光灯。
    //转换摄像头
    private ImageView btn_swichCamera;
    //包含文字的layout
    private LinearLayout ll_waterRootLayout;
    //取消按钮
    private TextView btn_cancle;
    //拍照按钮
    private ImageView btn_handle;
    //底部linearLayout
    private LinearLayout bottomLayout;
    //预览尺寸
    private Camera.Size mSize;
    //重拍返回得不getActivity解决方案

    private int screenHeight;

    private  float CameraRatio;
    private  float cameraH;  //相机高
    private  float cameraW;  //相机宽
    //底部ll_bottom的Y坐标
    private int y;
    //水印布局，渐变色布局
    private LinearLayout mContainerLayout;

    //截图水印传递
    private List<WaterMaskVO> waterMaskVOS=new ArrayList<>();
    //图片路径和水印
    //图片路径
    private String photoPath = "";
    //聚焦
    private View focuseView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        //注意requestWindowFeature方法必须要放在setContentView方法的前面
        setContentView(R.layout.activity_ordinary_camera_preview);
        //保存相机状态，方便用户下次使用
        cameraId=0;
        photoPath=ViewUtils.getPhotoFilename();
        initScreenSize();
        initView();
        initWaterList();
        initTakePicSound();
        initEvent();
        CameraModel=getIntent().getIntExtra("CameraModel",1);
        if(CameraModel==0)//0长按拍照,1点击截图
        {
            CameraListener();
        }
    }


    //初始化水印信息方法
    private void initWaterList() {
        if (WaterMaskVO.getOutdoorWaterMask() != null && WaterMaskVO.getOutdoorWaterMask() .size() != 0) {
                    waterMaskVOS=WaterMaskVO.getOutdoorWaterMask();
        }
    }


    //初始化拍照声音
    private void initTakePicSound() {
        mSoundPoll =  new SoundPool(100, AudioManager.STREAM_MUSIC,0);
        soundID = mSoundPoll.load(this,R.raw.camera_click_short,0);
    }




    //获取屏幕的宽，高
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ScreenW = metrics.widthPixels;
        ScreenH = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        RealScreenH=metrics.heightPixels;
    }

    private void initView() {
        //大相机框
        textureView = (TextureView)findViewById(R.id.textureView);
        //闪光灯
        btn_openFlash = (ImageView)findViewById(R.id.openFlash);
        //转换摄像头
        btn_swichCamera=(ImageView)findViewById(R.id.swichCamera);
        //聚焦
        focuseView = findViewById(R.id.viewFocuse);
        //包含文字的layout
        ll_waterRootLayout=(LinearLayout)findViewById(R.id.waterRootLayout);
        //取消按钮
        btn_cancle=(TextView)findViewById(R.id.btnCancel);
        //拍照按钮
        btn_handle=(ImageView)findViewById(R.id.handle);
        //底部linearlayout
        bottomLayout=(LinearLayout)findViewById(R.id.botomLayout);
        //水印name
        tv_name=(TextView)findViewById(R.id.tv_water_name);
        tv_time=(TextView)findViewById(R.id.tv_water_time);
        tv_location=(TextView)findViewById(R.id.tv_water_location);
    }




    private  void initEvent(){
        //相机框的监听
        listener=new OrdinaryCameraPreviewActivity.MySurfaceTextureListener();
        textureView.setSurfaceTextureListener(listener);
        //屏幕旋转的监听
        mCaptureOrientationEventListener = new OrdinaryCameraPreviewActivity.CaptureOrientationEventListener(this);
        //使用观察者模式观察屏幕方向的改变
        mCaptureSensorsObserver = new CaptureSensorsObserver(this);
        //获取屏幕焦点
        mCaptureSensorsObserver.setRefocuseListener(this);
        //变焦监听
        textureView.setOnTouchListener(this);
        btn_openFlash.setOnClickListener(this);
        btn_swichCamera.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        btn_handle.setOnClickListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        focusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean successed, final Camera ca) {
                btn_openFlash.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCamera != null) {
                            mCamera.cancelAutoFocus();
                        }
                    }
                }, 1000);

            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
        if(mCaptureOrientationEventListener!=null){
            mCaptureOrientationEventListener.disable();
        }
        if(mCaptureSensorsObserver!=null){
            mCaptureSensorsObserver.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCaptureOrientationEventListener!=null){
            mCaptureOrientationEventListener.enable();
        }
        if(mCaptureSensorsObserver!=null){
            mCaptureSensorsObserver.start();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientation = newConfig.orientation;
        stopCamera();
        onCreate(null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCamera();
        if (mCaptureOrientationEventListener != null) {
            mCaptureOrientationEventListener.disable();
            mCaptureOrientationEventListener = null;
        }

        if (null != mCaptureSensorsObserver) {
            mCaptureSensorsObserver.setRefocuseListener(null);
            mCaptureSensorsObserver = null;
        }
    }


    private void CameraListener() {
        //按下快门回调
        shutterCallback=new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                shootSound();
            }
        };
        //照片未压缩处理回调
        rawCallback=new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
            }
        };
        //照片经压缩后回调
        pictureCallBack = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                Log.i("xxx",data.length +"");
                // data为完整数据
                File file =ViewUtils.getPhotoPath(photoPath);
                // 使用流进行读写
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        fos.write(data);
                        // 关闭流
                        fos.close();
                        // 查看图片
                        Intent intent = new Intent(OrdinaryCameraPreviewActivity.this, OrdinaryHandlePicActivity.class);
                        // 传递路径
                        intent.putExtra(CAMERA_IMAGE_KEY, file.getAbsolutePath());
                        intent.putExtra(CAMERA_IMAGE_WATERMASK,(Serializable)waterMaskVOS);
                        intent.putExtra(PHONE_SCREEN_ORIENTATION,orientationTag);
                        intent.putExtra(CAMERA_IMAGE_FRONTANDBACK,cameraId);
                        startActivityForResult(intent,0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mCamera != null) {
            if (event.getPointerCount() == 1) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(isCanClick()){
                            needFocuse();
                        }
                        break;
                }
            }else{
                //多点触控，目的是变焦
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    //多点触控按下
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //计算两个手指的距离
                        oldDist = getFingerSpacing(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float newDist = getFingerSpacing(event);
                        float delta = newDist - oldDist;
                        if (Math.abs(delta) < threshold) {
                            break;
                        }
                        if (newDist > oldDist) {
                            CameraUtils.handleZoom(true, mCamera);
                        } else if (newDist < oldDist) {
                            CameraUtils.handleZoom(false, mCamera);
                        }
                        oldDist = newDist;
                        break;
                }
            }
        }
        return true;
    }

    /**
     * 计算两手指间距
     *
     * @param event
     * @return
     */
    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public boolean isCanClick() {
        long time = System.currentTimeMillis();
        if (Math.abs((time - mClickTime)) < 1000) {
            return false;
        } else {
            mClickTime = time;
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.openFlash) {
            if (CameraModel==1) {
                if (!isOpenFlash) {
                    isOpenFlash = true;
                    openFlash();
                } else {
                    isOpenFlash = false;
                    closeFlash();
                }
                btn_openFlash.setImageResource(isOpenFlash ? R.drawable.flashlight_on_icn
                        : R.drawable.flashlight_off_icn);
            } else { //相机
                btn_openFlash.setImageResource(openORCloseLight());
            }

        } else if (i == R.id.swichCamera) {
            useFrontFacingCamera = !useFrontFacingCamera;
            if (cameraId == 0)
                btn_openFlash.setVisibility(View.GONE);
            else
                btn_openFlash.setVisibility(View.VISIBLE);
            resetCamera();
            initCamera();
        } else if (i == R.id.btnCancel) {
            finish();

        } else if (i == R.id.handle) {
            if (CameraModel==0) {
                mCamera.takePicture(shutterCallback, rawCallback, pictureCallBack);
            } else {
                //shootSound();
                ViewUtils.drawToJieTu(this, textureView,photoPath,orientationTag,waterMaskVOS ,this);
                Intent intent = new Intent(this, OrdinaryHandlePicActivity.class);
                if((int)(CameraRatio*ScreenW)<ScreenH) //判断一下重叠的话，让图片据上//默认是居中
                    intent.putExtra(Screenshot_IMAGE_GRAVITY,"top");

                intent.putExtra(Screenshot_IMAGE_KEY, pathList.get(0));//图片url
                intent.putExtra(PHONE_SCREEN_ORIENTATION,orientationTag);//拍摄方向，竖直在下个页面做判断
                startActivityForResult(intent,0);
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if(resultCode==RESULT_OK){
                Intent it = new Intent();
                it.putExtra(kPhotoPath, data.getStringExtra(kPhotoPath));//图片的路径
                setResult(RESULT_OK, it);
                finish();
            }
        }
    }

    //截图手电筒开
    public void openFlash() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        }
    }
    //截图手电筒关
    public void closeFlash() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        }
    }

    //拍照闪光灯
    private int openORCloseLight() {
        int rec=0;
        modelIndex++;
        if(modelIndex>=flashMedols.length){
            modelIndex=0;
        }
        Camera.Parameters params = mCamera.getParameters();
        List<String> flashmodels = params.getSupportedFlashModes();
        if (flashmodels.contains(flashMedols[modelIndex])) {
        params.setFlashMode(flashMedols[modelIndex]);
        rec = modelResId[modelIndex];
        }
        mCamera.setParameters(params);
        return rec;
    }

    //聚焦
    @Override
    public void needFocuse() {
        try {
            if(mCamera!=null){
                mCamera.cancelAutoFocus();
                mCamera.autoFocus(focusCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (View.INVISIBLE == focuseView.getVisibility() && isPreview) {
            focuseView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void takePicture(Bitmap bitmap, String imagePath) {
        if(pathList.size()>0)
            pathList.clear();
        pathList.add(imagePath);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(checkNavigationBarShow(this,getWindow()))
                ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ScreenW, ScreenH, Gravity.CENTER));
            else
                ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,RealScreenH , Gravity.CENTER));
            setMargins(ll_waterRootLayout, 0, 0, 0, ImageUtil.dp2px(this,96));
            ll_waterRootLayout.setVisibility(View.VISIBLE);
        }

    }


    //判断底部虚拟导航栏是否显示
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public  boolean checkNavigationBarShow(@NonNull Context context, @NonNull Window window) {
        boolean show;
        Display display = window.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);

        View decorView = window.getDecorView();
        Configuration conf = context.getResources().getConfiguration();
        if (Configuration.ORIENTATION_LANDSCAPE == conf.orientation) {
            View contentView = decorView.findViewById(android.R.id.content);
            show = (point.x != contentView.getWidth());
        } else {
            Rect rect = new Rect();
            decorView.getWindowVisibleDisplayFrame(rect);
            show = (rect.bottom != point.y);
        }
        return show;
    }

    private class CaptureOrientationEventListener extends OrientationEventListener {
        public CaptureOrientationEventListener(Context context) {
            super(context);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            if (null == mCamera)
                return;
            if (orientation == ORIENTATION_UNKNOWN)
                return;
            if (isPreview) {
//                FCLog.i(CustomCameraEventLog.CAMERA_EVENT, "是否开启旋转:"+flag+",手机屏幕角度为" + orientation);
//                //只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) { //0度
                    orientationTag = 0;
                    rotateText(ORIGIN);
                } else if (orientation > 80 && orientation < 100) { //90度
                    orientationTag = 90;
                    rotateText(RIGHT);
                } else if (orientation > 170 && orientation < 190) { //180度
                    orientationTag = 180;
                    rotateText(TOP);
                } else if (orientation > 260 && orientation < 280) { //270度
                    orientationTag = 270;
                    rotateText(LEFT);
                } else {
                    return;
                }
            }
            orientation = (orientation + 45) / 90 * 90;
            if (android.os.Build.VERSION.SDK_INT <= 8) {
                _rotation = (90 + orientation) % 360;
                return;
            }
            try {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);
                Log.d(TAG, "CameraInfo角度为" + info.orientation);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    _rotationfront = (info.orientation - orientation + 360) % 360;
                    Log.d(TAG, "前置照片角度为" + _rotationfront + ";"
                            + "CameraInfo角度为" + info.orientation);
                } else { // back-facing camera
                    _rotation = (info.orientation + orientation) % 360;
                    Log.d(TAG, "后置照片角度为" + _rotation + ";"
                            + "CameraInfo角度为" + info.orientation);
                }
            }catch (Exception e){
            }

        }
    }


    /**
     *
     * @param orientation
     */
    private void rotateText(int orientation) {

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        Log.i("density",density+"");

        //获取底部botomLayout框的 Y 坐标
        int []location=new int[2];
        bottomLayout.getLocationOnScreen(location);
        y=location[1];//获取当前位置的y

        switch (orientation) {
            case ORIGIN://下
                ll_waterRootLayout.setRotation(0);
                if( (int)(CameraRatio*ScreenW)+ ImageUtil.dp2px(this,50) > y ) {//有重叠
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,y-ImageUtil.dp2px(this,50) , Gravity.TOP));
                    setMargins(ll_waterRootLayout, 0, ImageUtil.dp2px(this,50), 0, 0);
                }
                else{
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,(int)(CameraRatio*ScreenW) , Gravity.CENTER));
                }
                break;
            case RIGHT://右
                ll_waterRootLayout.setRotation(-90);
                if((int)(CameraRatio*ScreenW)+ImageUtil.dp2px(this,50) > y ) {//有重叠，左右，高与宽发生了互换
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(y-ImageUtil.dp2px(this,50),ScreenW , Gravity.CENTER));
                    setMargins(ll_waterRootLayout,0,0,0,23*(int) density);
                }
                else{ //没有重叠
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams((int)(CameraRatio*ScreenW),ScreenW , Gravity.CENTER));
                }
                break;
            case LEFT://左
                ll_waterRootLayout.setRotation(90);
                if( (int)(CameraRatio*ScreenW)+ImageUtil.dp2px(this,50) > y ) {//有重叠
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(y-ImageUtil.dp2px(this,50),ScreenW , Gravity.CENTER));
                    setMargins(ll_waterRootLayout,0,0,0,23*(int) density);
                }
                else{
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams((int)(CameraRatio*ScreenW),ScreenW , Gravity.CENTER));
                }
                break;
            case TOP://上
                ll_waterRootLayout.setRotation(180);
                if( (int)(CameraRatio*ScreenW)+ImageUtil.dp2px(this,50) > y) {//有重叠
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,y-ImageUtil.dp2px(this,50) , Gravity.TOP));
                    setMargins(ll_waterRootLayout,0,ImageUtil.dp2px(this,50),0,0);
                }
                else{
                    ll_waterRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,(int)(CameraRatio*ScreenW) , Gravity.CENTER));
                }

                break;
        }
    }


    public static void setMargins (View v, int l, int t, int r, int b) {

        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    private final class MySurfaceTextureListener implements
            TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            initCamera();

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            stopCamera();
            return true;
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    public void initCamera() {

        if (!isPreview && null != mSurfaceTexture) {
            //默认一个焦点距离，10dp
            threshold = ImageUtil.dp2px(this, 10f);
            cameraId = getCameraId();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                setCameraDisplayOrientation();
            }
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if(CameraModel==1) {//截图预览模式

            btn_openFlash.setVisibility(View.GONE);

            if (cameraId == 0)
                btn_openFlash.setVisibility(View.VISIBLE);

            mSize = CameraUtils.getPreviewSize(parameters,ScreenH,ScreenW);
            parameters.setPreviewSize(mSize.width,mSize.height);
            Log.i("preview_camera",mSize.width+","+mSize.height);
            //根据分辨率适配initTextureViewSize
            initTextureViewSize();

        } else if (CameraModel == 0) {//拍照预览模式

            btn_openFlash.setVisibility(View.GONE);

            List<Camera.Size> presizes = parameters.getSupportedPreviewSizes();
            for (int i = 0; i < presizes.size(); i++)
                Log.i("preview-w-h", presizes.get(i).width + "," + presizes.get(i).height);

            List<Camera.Size> picsizes = parameters.getSupportedPictureSizes();
            for (int i = 0; i < picsizes.size(); i++)//w比h大。android拍照默认是横屏
                Log.i("picture-w-h", "w:" + picsizes.get(i).width + "h:" + picsizes.get(i).height);


            //相同的比率，用注意宽高的问题
            double ASPECT_PREVIEW = 0.1;
            mSize = FSCameraUtils.getOptimalPreviewSize(presizes, ScreenH,ScreenW, ASPECT_PREVIEW);
            parameters.setPreviewSize(mSize.width, mSize.height);
            Log.i("preview_camera",mSize.width+","+mSize.height);
            //根据分辨率适配initTextureViewSize
            initTextureViewSize();

            if (cameraId == 0) {//后置
                btn_openFlash.setVisibility(View.VISIBLE);
                parameters.setFlashMode(flashMedols[modelIndex]);
                btn_openFlash.setImageResource(modelResId[modelIndex]);
            }

            //基于预览取分辨率，保证成图和预览图相同
            if (getPictureSize(picsizes, mSize.width,mSize.height)) {
                parameters.setPictureSize(mSize.width,mSize.height);
                Log.i("pictrue_camera",mSize.width+","+mSize.height);
            }
            else {
                double ASPECT_TOLERANCE = 0.1;
                Camera.Size optimalSize = FSCameraUtils.getOptimalPreviewSize(presizes, FSScreen.getScreenHeight(this), FSScreen.getScreenWidth(this), ASPECT_TOLERANCE);
                optimalSize = FSCameraUtils.getOptimalPreviewSize(picsizes, optimalSize.width, optimalSize.height, ASPECT_TOLERANCE);//比值1.77
                parameters.setPictureSize(optimalSize.width, optimalSize.height);
                Log.i("pictrue_camera",optimalSize.width+","+optimalSize.height);
            }
        }
        try {
            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        isPreview = true;
        focusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success,  Camera ca) {
                focuseView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(mCamera!=null){
                                mCamera.cancelAutoFocus();
                            }
                            if(mCaptureSensorsObserver!=null){
                                mCaptureSensorsObserver.stop();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                },1000);
                focuseView.setVisibility(View.INVISIBLE);
            }
        };
        }

    private void initTextureViewSize() {

        cameraH =mSize.height;
        cameraW=mSize.width;
        CameraRatio=cameraW/cameraH;
        if((int)(CameraRatio*ScreenW)>=ScreenH)
            textureView.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,(int)(CameraRatio*ScreenW), Gravity.CENTER));
        else
            textureView.setLayoutParams(new FrameLayout.LayoutParams(ScreenW,(int)(CameraRatio*ScreenW), Gravity.TOP));
    }


    public boolean getPictureSize(List<Camera.Size> s, int w, int h) {
        boolean isexist=false;
            for (int i = 0; i < s.size(); i++) {
                if (s.get(i).height == h && s.get(i).width == w) {
                    isexist=true;
                    break;
                }
            }
           return isexist;
    }


    public int getCameraId() {
        if (cameraId == -1) {
            initCameraId();
        }
        return (cameraId);
    }

    private void initCameraId() {
        int count = Camera.getNumberOfCameras();
        int result = -1;
        if (count > 0) {
            result = 0; // if we have a camera, default to this one
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < count; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK
                        && !useFrontFacingCamera()) {
                    result = i;
                    break;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                        && useFrontFacingCamera()) {
                    result = i;
                    break;
                }
            }
        }
        cameraId = result;
    }

    protected boolean useFrontFacingCamera() {
        return (useFrontFacingCamera);
    }

    //重置一下相机
    private void resetCamera() {
        if (isPreview) {
            isPreview = false;
            cameraId = -1;
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    // 停止相机
    private void stopCamera(){
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                isPreview = false;
            }
        }
    }

    //设置相机方向
    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        DisplayMetrics dm = new DisplayMetrics();

        Camera.getCameraInfo(cameraId, info);
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (info.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (info.orientation - degrees + 360) % 360;
        }
        if(mCamera!=null){
            mCamera.setDisplayOrientation(displayOrientation);
        }

    }
    public void shootSound()
    {
        mSoundPoll.play(soundID,0.3f,0.3f,0,0,1);
    }

}
