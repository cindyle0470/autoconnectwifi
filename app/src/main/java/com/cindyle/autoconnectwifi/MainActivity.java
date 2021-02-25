package com.cindyle.autoconnectwifi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;
    private IntentIntegrator intentIntegrator;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private WebView webView;
    private int netId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        qrCodeScanOnClick();

        Button show = findViewById(R.id.btnShow);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView = findViewById(R.id.web);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setBuiltInZoomControls(true);
                webView.loadUrl("https://google.com.tw");
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                });

                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {

                    }

                    @Override
                    public void onLoadResource(WebView view, String url) {

                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (url.equals(url)) {

                        }
                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                    }
                });
            }
        });
    }

    private void qrCodeScanOnClick() {
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentIntegrator = new IntentIntegrator(MainActivity.this);
                // 鎖定直立掃描，即使手機橫放亦不會變橫向
                intentIntegrator.setOrientationLocked(false);
                // 是否開啟聲音（掃完後會 B 一聲），實測 HTC & 三星 note 9，預設皆為 true，掃完會 B
                intentIntegrator.setBeepEnabled(false);
                // 開啟掃描後跳到自定義的 activity，沒有這行會跳到 zxing 預設的掃描畫面
                intentIntegrator.setCaptureActivity(CaptureActivity.class);
                // 可選擇前置或後置攝影鏡頭，0 為後置，1 為前置，預設為後置
                intentIntegrator.setCameraId(0);

                intentIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // result.getContents -> 取得之掃描結果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            Log.i("TAG", result.getContents());

            if (result.getContents().equals("**")) {
                Log.i("TAG", "YES!");
                connectedToWifi();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void connectedToWifi() {
        // android 10 以上
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setSsid("SSID")
                    .setWpa2Passphrase("password")
                    .build();

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier)
                    .build();

            connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            networkCallback = new ConnectivityManager.NetworkCallback() {

                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "連線失敗", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            };

            connectivityManager.requestNetwork(request, networkCallback);


        } else {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            WifiConfiguration wifiConfiguration = createWifiInfo("AskeyHL_4F", "80944e718a", 2);


            // 添加網路
            netId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.enableNetwork(netId, true);

        }}

    private WifiConfiguration createWifiInfo(String ssid, String password, int type) {
        // 初始化 WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        // 指定對應的 SSID
        config.SSID = "\"" + ssid + "\"";

        // 如果之前有類似的配置，就清除
        WifiConfiguration tempConfig = isExsits(ssid);
        if (tempConfig != null)  {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        switch (type) {
            // 0：不需要密碼
            case WIFICIPHER_NOPASS:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;

            // 1：以 WEP 加密的場景
            case WIFICIPHER_WEP:
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;

                break;

            // 2：以 WPA 加密的場景
            case WIFICIPHER_WPA:
                config.preSharedKey = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;

                break;
        }

        return config;
    }

    private WifiConfiguration isExsits(String ssid) {
        @SuppressLint("MissingPermission") List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration configA : configs) {
            if (configA.SSID.equals("\"" + ssid +"\"")) {
                return configA;
            }
        }
        return null;
    }
}