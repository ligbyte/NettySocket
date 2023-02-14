package com.uuzuche.lib_zxing.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.uuzuche.lib_zxing.R;

/**
 * Initial the camera
 * <p>
 * 默认的二维码扫描Activity
 */
public abstract class CaptureActivity extends AppCompatActivity {
    private static final int CODE_PERMISSION = 100;
    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            onSuccess(result);

            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            onFail();

            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            CaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不显示标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 不显示系统状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.camera);

        checkPermission(); // 权限校验
    }

    /**
     * 权限校验
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // 不需权限检验
            intFragment();
            return;
        }

//        // 权限校验
//        String[] permissionArr = new String[]{Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,};
//        List<String> permList = new ArrayList<>();//存储未申请的权限
//        for (String permission : permissionArr) {
//            int checkSelfPermission = ContextCompat.checkSelfPermission(this, permission);
//            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {//未申请
//                permList.add(permission);
//            }
//        }
//
//        if (permList.isEmpty()) { // 权限都申请了
//            intFragment();
//        } else { // 申请权限
//            ActivityCompat.requestPermissions(this, permList.toArray(new String[permList.size()]), CODE_PERMISSION);
//        }

        // 权限校验
        int checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (checkSelfPermission == PackageManager.PERMISSION_DENIED) { // 未申请
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CODE_PERMISSION);
        } else { // 权限已申请
            intFragment();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_PERMISSION) {
            for (int i = 0, size = grantResults.length; i < size; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) { // 权限未授予
                    Toast.makeText(this, "照相机权限未允许，无法正常使用该功能", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            // 所有权限已授予
            intFragment();
        }
    }

    /**
     * 加载扫描的fragment
     */
    private void intFragment() {
        CaptureFragment captureFragment = new CaptureFragment();
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commit();
        captureFragment.setCameraInitCallBack(new CaptureFragment.CameraInitCallBack() {
            @Override
            public void callBack(Exception e) {
                if (e == null) {

                } else {
                    if (e.toString().equals("java.lang.RuntimeException: Fail to connect to camera service")) {

                        Toast.makeText(CaptureActivity.this, "照相机权限未允许，无法正常使用该功能", Toast.LENGTH_SHORT).show();

                        //将页面销毁

                        finish();

                    }
                }
            }
        });
    }

    /**
     * 二维码解析成功
     *
     * @param result 解析结果
     */
    protected abstract void onSuccess(String result);

    /**
     * 二维码解析失败
     */
    protected void onFail() {
        Toast.makeText(this, "解析二维码失败", Toast.LENGTH_SHORT).show();
    }
}