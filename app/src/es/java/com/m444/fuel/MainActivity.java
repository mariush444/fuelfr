package com.m444.fuel;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // --- UI Components ---
    private Spinner fuelSpinner;
    private CheckBox doBlackCheckbox;
    private TextView statusText;
    private Button actionButton; // Download
    private Button openButton;   // Transfer
    private LinearLayout legendContainer;
    private LinearLayout legendRows;
    private File lastGeneratedFile;

    // --- Data & Thresholds ---
    private double lastPmin, lastPmax, lastP0_5, lastP5_10, lastP11_20, lastP21_30, lastP31_40, lastP41_80, lastP81_90;
    private boolean lastDoBlack;
    private String lastFuelName;

    // --- Colors ---
    private static final int COLOR_BLUE = Color.parseColor("#007bff");
    private static final int COLOR_GRAY = Color.parseColor("#cccccc");
    private static final int COLOR_STATUS_OK = Color.parseColor("#d4edda");
    private static final int COLOR_TEXT_OK = Color.parseColor("#155724");
    private static final int COLOR_STATUS_ERR = Color.parseColor("#f8d7da");
    private static final int COLOR_TEXT_ERR = Color.parseColor("#721c24");
    private static final int COLOR_STATUS_PROC = Color.parseColor("#fff3cd");
    private static final int COLOR_TEXT_PROC = Color.parseColor("#856404");

    // Fuel Map
    private final Map<Integer, String> fuelMap = new HashMap<Integer, String>() {{
        put(1, "Adblue");
        put(2, "Biodiesel");
        put(3, "Bioetanol");
        put(4, "Biogas Natural Comprimido");
        put(5, "Biogas Natural Licuado");
        put(6, "Diésel Renovable");
        put(7, "Gas Natural Comprimido");
        put(8, "Gas Natural Licuado");
        put(9, "Gases licuados del petróleo");
        put(10, "Gasoleo A");
        put(11, "Gasoleo B");
        put(12, "Gasoleo Premium");
        put(13, "Gasolina 95 E10");
        put(14, "Gasolina 95 E25");
        put(15, "Gasolina 95 E5");
        put(16, "Gasolina 95 E5 Premium");
        put(17, "Gasolina 95 E85");
        put(18, "Gasolina 98 E10");
        put(19, "Gasolina 98 E5");
        put(20, "Gasolina Renovable");
        put(21, "Hidrogeno");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- SCROLL VIEW (Fix 3) ---
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(Color.parseColor("#f4f4f4"));

        // Main Container inside ScrollView
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        // 1. Header Row
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Title
        TextView titleText = new TextView(this);
        titleText.setText("⛽ 🇪🇸 - colored prices");
        titleText.setTextSize(22);
        titleText.setTextColor(Color.BLACK);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        // Title takes up available space to push button to right, roughly
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleText.setLayoutParams(titleParams);

        // Close Button
        Button closeBtn = new Button(this);
        closeBtn.setText("☒");
        closeBtn.setTextSize(20);
        closeBtn.setBackgroundColor(Color.TRANSPARENT);
        closeBtn.setTextColor(Color.GRAY);
        closeBtn.setPadding(0,0,0,0);
        closeBtn.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        closeBtn.setLayoutParams(closeParams);

        headerLayout.addView(titleText);
        headerLayout.addView(closeBtn);
        mainLayout.addView(headerLayout);

        // 2. Fuel Spinner
        TextView label = new TextView(this);
        label.setText("Select Fuel Type:");
        label.setTextSize(16);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setPadding(0, 20, 0, 5);
        mainLayout.addView(label);

        fuelSpinner = new Spinner(this);
        // FIX 3: Light gray background for the Spinner field
        fuelSpinner.setBackgroundColor(Color.parseColor("#bcbcbc")); 
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (Map.Entry<Integer, String> entry : fuelMap.entrySet()) {
            adapter.add(entry.getKey() + ": " + entry.getValue());
        }
        fuelSpinner.setAdapter(adapter);
        // Default to Gasoleo Premium (12)
        for(int i=0; i<adapter.getCount(); i++) {
            if(adapter.getItem(i).startsWith("12:")) {
                fuelSpinner.setSelection(i);
                break;
            }
        }
        mainLayout.addView(fuelSpinner);

        // 3. Spacer View (Fix 1)
        View spacer1 = new View(this);
        spacer1.setMinimumHeight(30); // 30px empty space
        mainLayout.addView(spacer1);

        // 4. Checkbox
        doBlackCheckbox = new CheckBox(this);
        doBlackCheckbox.setText("Include stations without this fuel");
        doBlackCheckbox.setTextSize(16);
        doBlackCheckbox.setPadding(0, 10, 0, 10);
        mainLayout.addView(doBlackCheckbox);

        // 5. Status Text
        statusText = new TextView(this);
        statusText.setPadding(20, 20, 20, 20);
        statusText.setText("Ready");
        statusText.setGravity(Gravity.CENTER);
        statusText.setVisibility(View.GONE); // Hidden initially
        // Add margin top/bottom
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 20, 0, 20);
        statusText.setLayoutParams(statusParams);
        mainLayout.addView(statusText);

        // 6. Buttons Container
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(buttonsLayout);

        // Download Button
        actionButton = new Button(this);
        actionButton.setText("Download");
        setButtonActive(actionButton, true);
        // Add Margin to separate buttons (Fix 2)
        LinearLayout.LayoutParams actionBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionBtnParams.setMargins(0, 0, 0, 20); // 20px bottom margin
        actionButton.setLayoutParams(actionBtnParams);
        buttonsLayout.addView(actionButton);

        // Transfer Button
        openButton = new Button(this);
        openButton.setText("Transfer to OsmAnd");
        openButton.setVisibility(View.GONE);
        setButtonActive(openButton, true);
        buttonsLayout.addView(openButton);

        // 7. Legend Container
        legendContainer = new LinearLayout(this);
        legendContainer.setOrientation(LinearLayout.VERTICAL);
        legendContainer.setPadding(20, 20, 20, 20);
        legendContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        legendContainer.setBackgroundColor(Color.parseColor("#fafafa"));
        legendContainer.setVisibility(View.GONE); // Hidden initially
        LinearLayout.LayoutParams legendParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        legendParams.setMargins(0, 40, 0, 40); // Space above and below legend
        legendContainer.setLayoutParams(legendParams);

        TextView legendTitle = new TextView(this);
        legendTitle.setText("Price Color Legend:");
        legendTitle.setTextSize(16);
        legendTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        legendTitle.setGravity(Gravity.CENTER);
        legendContainer.addView(legendTitle);

        legendRows = new LinearLayout(this);
        legendRows.setOrientation(LinearLayout.VERTICAL);
        legendContainer.addView(legendRows);

        mainLayout.addView(legendContainer);

        // Listeners
        actionButton.setOnClickListener(v -> startProcess());
        openButton.setOnClickListener(v -> openInNavi(lastGeneratedFile));
    }

    // --- Helper: Button Styling ---
    private void setButtonActive(Button btn, boolean isActive) {
        if (isActive) {
            btn.setBackgroundColor(COLOR_BLUE);
            btn.setTextColor(Color.WHITE);
            btn.setEnabled(true);
        } else {
            btn.setBackgroundColor(COLOR_GRAY);
            btn.setTextColor(Color.parseColor("#666666"));
            btn.setEnabled(false);
        }
    }

    // --- Helper: Status Styling ---
    private void setStatus(String text, String type) {
        statusText.setText(text);
        statusText.setVisibility(View.VISIBLE);
        int bg = Color.TRANSPARENT;
        int txt = Color.BLACK;

        if ("process".equals(type)) {
            bg = COLOR_STATUS_PROC; txt = COLOR_TEXT_PROC;
        } else if ("ok".equals(type)) {
            bg = COLOR_STATUS_OK; txt = COLOR_TEXT_OK;
        } else if ("err".equals(type)) {
            bg = COLOR_STATUS_ERR; txt = COLOR_TEXT_ERR;
        } else {
            statusText.setVisibility(View.GONE);
            return;
        }

        statusText.setBackgroundColor(bg);
        statusText.setTextColor(txt);
    }

    // --- Logic ---
    private void startProcess() {
        // Reset UI
        setButtonActive(actionButton, false); // Gray
        openButton.setVisibility(View.GONE);
        legendContainer.setVisibility(View.GONE);
        setStatus("Loading data...", "process");

        // Run in background
        new Thread(() -> {
            try {
                // 1. Download JSON
                String jsonString = downloadJson();
                if (jsonString == null) {
                    runOnUiThread(() -> {
                        setStatus("Error: Download failed.", "err");
                        setButtonActive(actionButton, true); // Blue
                    });
                    return;
                }

                // 2. Parse and Process
                runOnUiThread(() -> setStatus("Generating GPX...", "process"));
                File resultFile = processJsonAndSave(jsonString);

                // 3. Finish
                if (resultFile != null) {
                    lastGeneratedFile = resultFile;
                    runOnUiThread(() -> {
                        setStatus("GPX ready.", "ok");
                        setButtonActive(actionButton, true); // Blue
                        openButton.setVisibility(View.VISIBLE);
                        renderLegend(); // Draw the legend
                    });
                } else {
                    runOnUiThread(() -> {
                        setStatus("Error: Processing failed.", "err");
                        setButtonActive(actionButton, true); // Blue
                    });
                }

            } catch (Exception e) {
                Log.e("FuelApp", "Error", e);
                runOnUiThread(() -> {
                    setStatus("Error: " + e.getMessage(), "err");
                    setButtonActive(actionButton, true);
                });
            }
        }).start();
    }

    // FIX: Added retry logic to handle "Unable to resolve host" (DNS not ready on cold start)
    private String downloadJson() throws IOException {
        File dir = new File(getExternalFilesDir(null), "gpx");
        if (!dir.exists()) dir.mkdirs();
        File jsonFile = new File(dir, "es_instantane.json");
        long sixHours = 6 * 60 * 60 * 1000;

        if (jsonFile.exists()) {
            long age = System.currentTimeMillis() - jsonFile.lastModified();
            if (age < sixHours) {
                return readFile(jsonFile);
            }
        }

        int maxRetries = 3;
        IOException lastException = null;

        // Retry loop
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(jsonFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                    // Download successful, read and return
                    return readFile(jsonFile);
                } else {
                    // Server error (404, 500)
                    throw new IOException("HTTP " + conn.getResponseCode());
                }

            } catch (IOException e) {
                Log.e("FuelApp", "Download attempt " + (attempt + 1) + " failed", e);
                lastException = e;
                
                // Wait 1 second before retrying (unless last attempt)
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(1000); 
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Download interrupted");
                    }
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        // If loop finishes, all attempts failed
        if (lastException != null) {
            throw lastException;
        }
        return null;
    }

    private String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private File processJsonAndSave(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray stations = root.getJSONArray("ListaEESSPrecio");
            String dataDate = root.optString("Fecha", "");

            String selected = (String) fuelSpinner.getSelectedItem();
            int fuelId = Integer.parseInt(selected.split(":")[0]);
            String fuelName = fuelMap.get(fuelId);
            String targetKey = "Precio " + fuelName;
            boolean DOblack = doBlackCheckbox.isChecked();

            // Store for Legend
            lastFuelName = fuelName;
            lastDoBlack = DOblack;

            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            int count = 0;

            for (int i = 0; i < stations.length(); i++) {
                JSONObject s = stations.getJSONObject(i);
                double p = parsePrice(s.optString(targetKey, ""));
                if (p > 0) {
                    if (p < min) min = p;
                    if (p > max) max = p;
                    count++;
                }
            }

            if (count == 0) return null;

            double range = max - min;
            
            // Calculate Thresholds
            lastPmin = min;
            lastPmax = max;
            lastP0_5   = round(min + range * 0.05);
            lastP5_10  = round(min + range * 0.1);
            lastP11_20 = round(min + range * 0.2);
            lastP21_30 = round(min + range * 0.3);
            lastP31_40 = round(min + range * 0.4);
            lastP41_80 = round(min + range * 0.8);
            lastP81_90 = round(min + range * 0.9);

            // Build GPX
            StringBuilder gpx = new StringBuilder();
            gpx.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
            gpx.append("<gpx version=\"1.1\" creator=\"AI+Mariush444\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:osmand=\"https://osmand.net\">\n");
            gpx.append("<metadata><name>fuel ES</name><desc>https://mariush444.github.io/Osmand-tools/</desc><author><name>AI+m444</name></author></metadata>\n");

            for (int i = 0; i < stations.length(); i++) {
                JSONObject s = stations.getJSONObject(i);
                double priceVal = parsePrice(s.optString(targetKey, ""));
                boolean isAbsent = (priceVal == 0);

                if (!DOblack && isAbsent) continue;

                String lat = s.optString("Latitud", "0").replace(",", ".");
                String lon = s.optString("Longitud (WGS84)", "0").replace(",", ".");
                String name = isAbsent ? ("no " + fuelName) : (fuelName + " " + priceVal);

                String color = "000001";
                if (!isAbsent) {
                    if (priceVal <= lastP0_5) color = "10c0f0";
                    else if (priceVal <= lastP5_10) color = "00842b";
                    else if (priceVal <= lastP11_20) color = "88e030";
                    else if (priceVal <= lastP21_30) color = "eecc22";
                    else if (priceVal <= lastP31_40) color = "ff8500";
                    else if (priceVal <= lastP41_80) color = "d00d0d";
                    else if (priceVal <= lastP81_90) color = "a71de1";
                    else color = "1010a0";
                }

                StringBuilder desc = new StringBuilder();
                for (Map.Entry<Integer, String> entry : fuelMap.entrySet()) {
                    if (entry.getKey() == fuelId) continue;
                    double otherPrice = parsePrice(s.optString("Precio " + entry.getValue(), ""));
                    if (otherPrice > 0) {
                        desc.append(entry.getValue()).append(": ").append(otherPrice).append("<br>");
                    }
                }

                gpx.append("<wpt lat=\"").append(lat).append("\" lon=\"").append(lon).append("\">");
                gpx.append("<name>").append(escapeXml(name)).append("</name>");
                
                // FIX 2: Use CDATA to allow <br> tags inside XML description
                gpx.append("<desc><![CDATA[").append(desc.toString()).append("]]></desc>");
                
                if (isAbsent) gpx.append("<type>Absence</type>");
                gpx.append("<extensions>");
                gpx.append("<osmand:color>#").append(color).append("</osmand:color>");
                gpx.append("<osmand:icon>fuel</osmand:icon>");
                gpx.append("<osmand:background>circle</osmand:background>");
                gpx.append("</extensions></wpt>\n");
            }
            gpx.append("</gpx>");

            String suffix = DOblack ? "full" : "only";
            String cleanName = fuelName.replace(" ", "_");
            String datePart = parseDate(dataDate);
            String filename = "ES-" + cleanName + "-" + suffix + "-" + datePart + ".gpx";

            File dir = new File(getExternalFilesDir(null), "gpx");
            if (!dir.exists()) dir.mkdirs();
            
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".gpx")) f.delete();
                }
            }

            File outFile = new File(dir, filename);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(gpx.toString().getBytes("UTF-8"));
            }

            return outFile;

        } catch (JSONException e) {
            Log.e("FuelApp", "JSON Parse", e);
            return null;
        } catch (IOException e) {
            Log.e("FuelApp", "IO Error", e);
            return null;
        }
    }

    // --- Legend Rendering ---
    private void renderLegend() {
        legendRows.removeAllViews();
        DecimalFormat df = new DecimalFormat("0.000");

        addLegendRow(df.format(lastPmin), df.format(lastP0_5), "#10c0f0", Color.BLACK, "light blue");
        addLegendRow(df.format(lastP0_5 + 0.001), df.format(lastP5_10), "#00842b", Color.WHITE, "dark green");
        addLegendRow(df.format(lastP5_10 + 0.001), df.format(lastP11_20), "#88e030", Color.BLACK, "bright green");
        addLegendRow(df.format(lastP11_20 + 0.001), df.format(lastP21_30), "#eecc22", Color.BLACK, "yellow");
        addLegendRow(df.format(lastP21_30 + 0.001), df.format(lastP31_40), "#ff8500", Color.WHITE, "orange");
        addLegendRow(df.format(lastP31_40 + 0.001), df.format(lastP41_80), "#d00d0d", Color.WHITE, "red");
        addLegendRow(df.format(lastP41_80 + 0.001), df.format(lastP81_90), "#a71de1", Color.WHITE, "violet");
        addLegendRow(df.format(lastP81_90 + 0.001), df.format(lastPmax), "#1010a0", Color.WHITE, "navy blue");

        if (lastDoBlack) {
            addLegendRow("Absence", "no " + lastFuelName, "#000000", Color.WHITE, "");
        }

        legendContainer.setVisibility(View.VISIBLE);
    }

    private void addLegendRow(String min, String max, String colorHex, int textColor, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundColor(Color.parseColor(colorHex));
        row.setPadding(10, 10, 10, 10);
        row.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 10);
        row.setLayoutParams(rowParams);

        TextView rangeText = new TextView(this);
        rangeText.setText(min + " - " + max);
        rangeText.setTextColor(textColor);
        rangeText.setTextSize(14);
        rangeText.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(rangeText);

        TextView space = new TextView(this);
        space.setText("  "); // Spacer
        space.setTextColor(textColor);
        row.addView(space);

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextColor(textColor);
        labelText.setTextSize(14);
        row.addView(labelText);

        legendRows.addView(row);
    }

    // --- Standard Helpers ---
    private double parsePrice(String val) {
        if (val == null || val.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(val.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double round(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String parseDate(String dateStr) {
        try {
            String[] parts = dateStr.split(" ")[0].split("/");
            if (parts.length == 3) {
                return parts[2].substring(2) + parts[1] + parts[0];
            }
        } catch (Exception e) { }
        return "000000";
    }

    private void openInNavi(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/gpx+xml");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, "Open GPX with...");
            startActivity(chooser);

        } catch (Exception e) {
            Toast.makeText(this, "No app found to open GPX", Toast.LENGTH_LONG).show();
        }
    }
}
