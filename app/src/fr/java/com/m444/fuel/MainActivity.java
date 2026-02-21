package com.m444.fuel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import java.net.*;
import java.util.zip.*;
import java.nio.file.Files;
import android.webkit.JavascriptInterface;
import java.io.InputStream;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Add the JS interface
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.loadUrl("file:///android_asset/index.html");

/*    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
*/

	getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (webView.canGoBack()) {
				webView.goBack();
			} else {
				setEnabled(false); // prevent infinite loop
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
			// code to finish the activity/webview
			runOnUiThread(() -> finish());
		}
        
        @JavascriptInterface
		public String getFuelXML() {
			try {
				File dir = new File(getExternalFilesDir(null), "gpx");
				if (!dir.exists()) dir.mkdirs();

				File xmlFile = new File(dir, "instantane.xml");
				long sixHours = 6 * 60 * 60 * 1000;

				boolean needDownload = true;

				if (xmlFile.exists()) {
					long age = System.currentTimeMillis() - xmlFile.lastModified();
					if (age < sixHours) {
						needDownload = false;
					}
				}

        if (needDownload) {

            // 1️ Download ZIP
            // URL url = new URL("https://github.com/mariush444/Osmand-tools/raw/refs/heads/main/instantane.zip");
            URL url = new URL("https://donnees.roulez-eco.fr/opendata/instantane");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            File zipFile = new File(dir, "instantane.zip");

            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(zipFile)) {

                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            // 2️ Unzip only XML
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {

                    if (entry.getName().contains("PrixCarburants_instantane.xml")) {

                        try (FileOutputStream fos = new FileOutputStream(xmlFile)) {

                            byte[] buffer = new byte[4096];
                            int len;

                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        break;
                    }
                }
            }
        }

        // 3️ Return XML as String
        return new String(Files.readAllBytes(xmlFile.toPath()), "ISO-8859-1");

    } catch (Exception e) {
        Log.e("FuelApp", "XML error", e);
        return null;
    }
}

        
        
 /*       
		@JavascriptInterface
		public boolean isZipFresh(String fileName) {
			File dir = new File(getExternalFilesDir(null), "gpx");
			File file = new File(dir, fileName);

			if (!file.exists()) return false;

			long age = System.currentTimeMillis() - file.lastModified();
			long sixHours = 6 * 60 * 60 * 1000;

			return age < sixHours;
		}
		
		@JavascriptInterface
		public String getZipPath(String fileName) {
			File dir = new File(getExternalFilesDir(null), "gpx");
			return new File(dir, fileName).getAbsolutePath();
		}
*/
        @android.webkit.JavascriptInterface
        public void saveFile(String filename, String content) {
            try {
                // 1. Create GPX folder inside app external files
                File dir = new File(getExternalFilesDir(null), "gpx");
                if (!dir.exists()) dir.mkdirs();

                // 2. Delete old files
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && f.getName().endsWith(".gpx")) {
                            f.delete();
                        }
                    }
                }

                // 3. Save new file
                File file = new File(dir, filename);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(content.getBytes("UTF-8"));
                }

                Toast.makeText(mContext, "Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                // 4. Open in navigator
                // openInNavi(file);

            } catch (IOException e) {
                Log.e("FuelApp", "Error saving file", e);
                Toast.makeText(mContext, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
		/*
        private void openInNavi(File file) {
            try {
                Uri uri = FileProvider.getUriForFile(
                        mContext,
                        mContext.getPackageName() + ".fileprovider",
                        file
                );

					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("application/gpx+xml");
					intent.putExtra(Intent.EXTRA_STREAM, uri);
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "No app found to open GPX", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Log.e("FuelApp", "Error opening GPX", e);
                Toast.makeText(mContext, "Failed to open GPX: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } */
        
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

				// Create chooser to ensure user can pick OsmAnd+ or Nightly
				Intent chooser = Intent.createChooser(intent, "Open GPX with...");
				
				// REMOVED the 'if (resolveActivity...)' check.
				// On LineageOS, this check fails due to visibility rules even if apps exist.
				// We rely on the try-catch block for actual errors.
				
				mContext.startActivity(chooser);

			} catch (Exception e) {
				Log.e("FuelApp", "Error opening GPX", e);
				Toast.makeText(mContext, "No app found to open GPX", Toast.LENGTH_LONG).show();
			}
		}
        
        
    }
}
