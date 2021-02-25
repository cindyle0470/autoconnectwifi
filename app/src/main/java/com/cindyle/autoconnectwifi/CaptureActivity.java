package com.cindyle.autoconnectwifi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class CaptureActivity extends AppCompatActivity {
    private CaptureManager capture;   // 管理擷取畫面
    private DecoratedBarcodeView barcodeScannerView;    // 裝飾整個掃描畫面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);


        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);

        // 設定擷取管理者
        capture = new CaptureManager(this, barcodeScannerView);
        // 設定拒絕危險權限提示訊息
        capture.setShowMissingCameraPermissionDialog(true, "You need Camera Permission to scan QR Code");
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();    // 開始解析
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}