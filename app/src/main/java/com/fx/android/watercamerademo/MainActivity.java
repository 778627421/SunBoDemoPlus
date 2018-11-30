package com.fx.android.watercamerademo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fx.android.watercamerademo.Camera.OrdinaryCameraPreviewActivity;
import com.fx.android.watercamerademo.ScreenAdapter.NotchScreenActivity;

public class MainActivity extends AppCompatActivity {

    private Button mBtnCamera,mBtnScreenShot,mNotchScrren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//使用相机拍照
        mBtnCamera=(Button)findViewById(R.id.btn_camera);
//使用截图拍照
        mBtnScreenShot=(Button)findViewById(R.id.btn_sreenshot);
//Android P以前刘海屏的适配方案，在手机里请打开全屏运行此应用
        mNotchScrren=(Button)findViewById(R.id.btn_NotchAdapter);


        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>23){
                    if(checkPermission(MainActivity.this))
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },1);
                }
                else{
                    startActivityForResult(
                            new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class).
                                    putExtra("CameraModel",0),301);
                }
            }
        });

        mBtnScreenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>23){
                    if(checkPermission(MainActivity.this))
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },2);
                }  else{
                    startActivityForResult(
                            new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class).
                                    putExtra("CameraModel",1),302);
                }

            }
        });


        mNotchScrren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NotchScreenActivity.class));
            }
        });

    }


    private boolean checkPermission(Activity activity) {
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startActivityForResult(
                            new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class).
                                    putExtra("CameraModel",0),301);
                }
                else
                    Toast.makeText(MainActivity.this,"用户否定了这个权限",Toast.LENGTH_LONG).show();
                break;
            case 2:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startActivityForResult(
                            new Intent(MainActivity.this, OrdinaryCameraPreviewActivity.class).
                                    putExtra("CameraModel",1),302);
                }
                else
                    Toast.makeText(MainActivity.this,"用户否定了这个权限",Toast.LENGTH_LONG).show();
                break;

            default:
        }
    }


}
