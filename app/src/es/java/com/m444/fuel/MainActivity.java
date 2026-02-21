package com.m444.fuel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.loadUrl("file:///android_asset/index.html");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void openLastFile(String filename) {
            File dir = new File(getExternalFilesDir(null), "gpx");
            File file = new File(dir, filename);
            openInNavi(file);
        }

        @JavascriptInterface
        public void close() {
            runOnUiThread(() -> finish());
        }

        // ES Specific: Download JSON in background thread to avoid NetworkOnMainThreadException
        @JavascriptInterface
        public String getFuelJSON() {
            final String[] result = {null};
            final Exception[] exception = {null};
            // Use CountDownLatch to wait for the background thread to finish
            final CountDownLatch latch = new CountDownLatch(1);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        File dir = new File(getExternalFilesDir(null), "gpx");
                        if (!dir.exists()) dir.mkdirs();

                        File jsonFile = new File(dir, "es_instantane.json");
                        long sixHours = 6 * 60 * 60 * 1000;

                        boolean needDownload = true;

                        if (jsonFile.exists()) {
                            long age = System.currentTimeMillis() - jsonFile.lastModified();
                            if (age < sixHours) {
                                needDownload = false;
                            }
                        }

                        if (needDownload) {
                            // URL for ES Government Data
                            URL url = new URL("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.connect();

                            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                try (FileOutputStream out = new FileOutputStream(jsonFile);
                                     InputStream in = conn.getInputStream()) {
                                    
                                    byte[] buffer = new byte[4096];
                                    int len;
                                    while ((len = in.read(buffer)) > 0) {
                                        out.write(buffer, 0, len);
                                    }
                                }
                            } else {
                                throw new IOException("HTTP Error: " + conn.getResponseCode());
                            }
                        }
                        
                        // Read file content
                        result[0] = new String(Files.readAllBytes(jsonFile.toPath()), "UTF-8");

                    } catch (Exception e) {
                        Log.e("FuelAppES", "JSON download error", e);
                        exception[0] = e;
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();

            try {
                // Wait for the thread to finish (max 10 seconds to avoid UI freeze forever)
                latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exception[0] = e;
            }

            if (exception[0] != null) {
                return null; 
            }
            return result[0];
        }

        @android.webkit.JavascriptInterface
        public void saveFile(String filename, String content) {
            try {
                File dir = new File(getExternalFilesDir(null), "gpx");
                if (!dir.exists()) dir.mkdirs();

                // Delete old GPX files
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && f.getName().endsWith(".gpx")) {
                            f.delete();
                        }
                    }
                }

                File file = new File(dir, filename);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(content.getBytes("UTF-8"));
                }
                
                String absolutePath = file.getAbsolutePath();
                Log.d("FuelAppES", "File saved to: " + absolutePath);
                Toast.makeText(mContext, "Saved to:\n" + absolutePath, Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Log.e("FuelAppES", "Error saving file", e);
                Toast.makeText(mContext, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private void openInNavi(File file) {
            try {
                Uri uri = FileProvider.getUriForFile(
                        mContext,
                        mContext.getPackageName() + ".fileprovider",
                        file
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/gpx+xml");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent chooser = Intent.createChooser(intent, "Open GPX with...");
                mContext.startActivity(chooser);

            } catch (Exception e) {
                Log.e("FuelAppES", "Error opening GPX", e);
                Toast.makeText(mContext, "No app found to open GPX", Toast.LENGTH_LONG).show();
            }
        }
    }
}
