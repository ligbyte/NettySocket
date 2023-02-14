package com.uuzuche.lib_zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huawei.hms.hmsscankit.OnLightVisibleCallBack;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.uuzuche.lib_zxing.R;

import java.io.IOException;

public abstract class DefinedActivity extends Activity {
    public static final String SCAN_RESULT = "scanResult";
    public static final int REQUEST_CODE_PHOTO = 0X1113;
    private static final String TAG = "DefinedActivity";
    final int SCAN_FRAME_SIZE = 220;
    int mScreenWidth;
    int mScreenHeight;
    private FrameLayout frameLayout;
    private RemoteView remoteView;
    private ImageView backBtn;
    private ImageView imgBtn;
    private ImageView flushBtn;
    private int[] img = {R.drawable.flashlight_on, R.drawable.flashlight_off};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_defined);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
        frameLayout = findViewById(R.id.rim);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        int scanFrameSize = (int) (SCAN_FRAME_SIZE * density);

        Rect rect = new Rect();
        rect.left = mScreenWidth / 2 - scanFrameSize / 2;
        rect.right = mScreenWidth / 2 + scanFrameSize / 2;
        rect.top = mScreenHeight / 2 - scanFrameSize / 2;
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2;


        remoteView = new RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build();
        flushBtn = findViewById(R.id.flush_btn);
        remoteView.setOnLightVisibleCallback(new OnLightVisibleCallBack() {
            @Override
            public void onVisibleChanged(boolean visible) {
                if (visible) {
                    flushBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        remoteView.setOnResultCallback(new OnResultCallback() {
            @Override
            public void onResult(HmsScan[] result) {
                if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
                    bundle.putString(CodeUtils.RESULT_STRING, result[0].originalValue);
                    onSuccess(result[0].originalValue);
                    setResult(RESULT_OK, intent);
                    DefinedActivity.this.finish();
                }
            }
        });

        remoteView.onCreate(savedInstanceState);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        frameLayout.addView(remoteView, params);
        setBackOperation();
        setPictureScanOperation();
        setFlashOperation();
    }

    private void setPictureScanOperation() {
        imgBtn = findViewById(R.id.img_btn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                DefinedActivity.this.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);

            }
        });
    }

    private void setFlashOperation() {
        flushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteView.getLightStatus()) {
                    remoteView.switchLight();
                    flushBtn.setImageResource(img[1]);
                } else {
                    remoteView.switchLight();
                    flushBtn.setImageResource(img[0]);
                }
            }
        });
    }

    private void setBackOperation() {
        backBtn = findViewById(R.id.back_img);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DefinedActivity.this.finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        remoteView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        remoteView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        remoteView.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteView.onStop();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                HmsScan[] hmsScans = ScanUtil.decodeWithBitmap(DefinedActivity.this, bitmap, new HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create());
                if (hmsScans != null && hmsScans.length > 0 && hmsScans[0] != null && !TextUtils.isEmpty(hmsScans[0].getOriginalValue())) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
                    bundle.putString(CodeUtils.RESULT_STRING, hmsScans[0].originalValue);
                    onSuccess(hmsScans[0].originalValue);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    DefinedActivity.this.finish();
                } else {
                    onFail();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
