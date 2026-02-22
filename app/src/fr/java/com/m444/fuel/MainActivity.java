package com.m444.fuel;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    // --- UI Components ---
    private Spinner fuelSpinner;
    private CheckBox doBlackCheckbox;
    private TextView statusText;
    private Button actionButton;
    private Button openButton;
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

    // French Fuel Map
    private final Map<Integer, String> fuelMap = new HashMap<Integer, String>() {{
        put(1, "Gazole");
        put(5, "E10");
        put(2, "SP95");
        put(6, "SP98");
        put(3, "E85");
        put(4, "GPLc");
    }};

    // Service Emojis Map
    private final Map<String, String> serviceEmojis = new HashMap<String, String>() {{
        put("Bar", "🍴");
        put("Restauration sur place", "🍴");
        put("Boutique alimentaire", "🛒");
        put("DAB (Distributeur automatique de billets)", "🏧");
        put("Douches", "🚿");
        put("Espace bébé", "🚼");
        put("Relais colis", "🎁");
        put("Services réparation / entretien", "🛠️");
        put("Station de gonflage", "🔧");
        put("Toilettes publiques", "🚻");
        put("Wifi", "📶");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- SCROLL VIEW ---
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(Color.parseColor("#f4f4f4"));

        // Main Container
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

        TextView titleText = new TextView(this);
        titleText.setText("⛽ 🇨🇵 - colored prices");
        titleText.setTextSize(22);
        titleText.setTextColor(Color.BLACK);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleText.setLayoutParams(titleParams);

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
        fuelSpinner.setSelection(0); // Default to Gazole
        mainLayout.addView(fuelSpinner);

        // 3. Spacer
        View spacer1 = new View(this);
        spacer1.setMinimumHeight(30);
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
        statusText.setVisibility(View.GONE);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 20, 0, 20);
        statusText.setLayoutParams(statusParams);
        mainLayout.addView(statusText);

        // 6. Buttons
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(buttonsLayout);

        actionButton = new Button(this);
        actionButton.setText("Download");
        setButtonActive(actionButton, true);
        LinearLayout.LayoutParams actionBtnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionBtnParams.setMargins(0, 0, 0, 20);
        actionButton.setLayoutParams(actionBtnParams);
        buttonsLayout.addView(actionButton);

        openButton = new Button(this);
        openButton.setText("Transfer to OsmAnd");
        openButton.setVisibility(View.GONE);
        setButtonActive(openButton, true);
        buttonsLayout.addView(openButton);

        // 7. Legend
        legendContainer = new LinearLayout(this);
        legendContainer.setOrientation(LinearLayout.VERTICAL);
        legendContainer.setPadding(20, 20, 20, 20);
        legendContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        legendContainer.setBackgroundColor(Color.parseColor("#fafafa"));
        legendContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams legendParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        legendParams.setMargins(0, 40, 0, 40);
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

        actionButton.setOnClickListener(v -> startProcess());
        openButton.setOnClickListener(v -> openInNavi(lastGeneratedFile));
    }

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

    private void startProcess() {
        setButtonActive(actionButton, false);
        openButton.setVisibility(View.GONE);
        legendContainer.setVisibility(View.GONE);
        setStatus("Loading data...", "process");

        new Thread(() -> {
            try {
                // 1. Download and Unzip
                File xmlFile = downloadAndUnzipXml();
                if (xmlFile == null) {
                    runOnUiThread(() -> {
                        setStatus("Error: Download failed.", "err");
                        setButtonActive(actionButton, true);
                    });
                    return;
                }

                // 2. Process and Save
                runOnUiThread(() -> setStatus("Processing XML...", "process"));
                File resultFile = processXmlAndSave(xmlFile);

                if (resultFile != null) {
                    lastGeneratedFile = resultFile;
                    runOnUiThread(() -> {
                        setStatus("GPX ready.", "ok");
                        setButtonActive(actionButton, true);
                        openButton.setVisibility(View.VISIBLE);
                        renderLegend();
                    });
                } else {
                    runOnUiThread(() -> {
                        setStatus("Error: No data found.", "err");
                        setButtonActive(actionButton, true);
                    });
                }

            } catch (Exception e) {
                Log.e("FuelAppFR", "Error", e);
                runOnUiThread(() -> {
                    setStatus("Error: " + e.getMessage(), "err");
                    setButtonActive(actionButton, true);
                });
            }
        }).start();
    }

    // FIX: Added retry logic to handle "Unable to resolve host" (DNS not ready on cold start)
    private File downloadAndUnzipXml() throws IOException {
        File dir = new File(getExternalFilesDir(null), "gpx");
        if (!dir.exists()) dir.mkdirs();

        // We will always save/use this specific filename
        File xmlFile = new File(dir, "instantane.xml");
        File zipFile = new File(dir, "instantane.zip");

        // Check cache (6 hours)
        long sixHours = 6 * 60 * 60 * 1000;
        if (xmlFile.exists()) {
            long age = System.currentTimeMillis() - xmlFile.lastModified();
            if (age < sixHours) {
                return xmlFile; // Use the cached file
            }
        }

        int maxRetries = 3;
        IOException lastException = null;

        // Retry loop: attempts 3 times if a network error occurs
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://donnees.roulez-eco.fr/opendata/instantane");
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Save ZIP
                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(zipFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }

                    // Unzip: Look for the entry containing 'instantane.xml' inside the zip
                    // The entry name is usually 'PrixCarburants_instantane.xml'
                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (entry.getName().contains("instantane.xml")) {
                                // Extract and save it as 'instantane.xml'
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
                    return xmlFile; // Success!
                } else {
                    // If server returns 404 or 500, we don't retry
                    throw new IOException("HTTP Error: " + conn.getResponseCode());
                }

            } catch (IOException e) {
                Log.e("FuelAppFR", "Download attempt " + (attempt + 1) + " failed", e);
                lastException = e;
                
                // If it wasn't the last attempt, wait 1 second before retrying
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

        // If all retries failed, throw the last caught exception
        if (lastException != null) {
            throw lastException;
        }
        return null;
    }

    private File processXmlAndSave(File xmlFile) {
        try {
            String selected = (String) fuelSpinner.getSelectedItem();
            int fuelId = Integer.parseInt(selected.split(":")[0]);
            String fuelName = fuelMap.get(fuelId);
            boolean DOblack = doBlackCheckbox.isChecked();
            
            lastFuelName = fuelName;
            lastDoBlack = DOblack;

            // Parse XML
            List<Station> stations = parseXML(xmlFile, fuelId);

            if (stations.isEmpty()) return null;

            // Calculate Ranges
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            String latestDate = "00000000";

            for (Station s : stations) {
                if (s.price > 0) {
                    if (s.price < min) min = s.price;
                    if (s.price > max) max = s.price;
                    if (s.date.compareTo(latestDate) > 0) latestDate = s.date;
                }
            }

            if (min == Double.MAX_VALUE) return null; // No prices found

            double range = max - min;
            lastPmin = min;
            lastPmax = max;
            lastP0_5   = min + range * 0.05;
            lastP5_10  = min + range * 0.10;
            lastP11_20 = min + range * 0.20;
            lastP21_30 = min + range * 0.30;
            lastP31_40 = min + range * 0.40;
            lastP41_80 = min + range * 0.80;
            lastP81_90 = min + range * 0.90;

            // Build GPX
            StringBuilder gpx = new StringBuilder();
            gpx.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
            gpx.append("<gpx version=\"1.1\" creator=\"AI+Mariush444\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:osmand=\"https://osmand.net\">\n");
            gpx.append("<metadata><name>fuel FR</name><desc>https://mariush444.github.io/Osmand-tools/</desc><author><name>AI+m444</name></author></metadata>\n");

            DecimalFormat df3 = new DecimalFormat("0.000");
            DecimalFormat df5 = new DecimalFormat("0.00000");

            for (Station s : stations) {
                // Check if we should include this station
                boolean hasPrice = (s.price > 0);
                if (!DOblack && !hasPrice) continue;

                // Determine Color
                String color = "000001"; // Default/Absence
                if (hasPrice) {
                    if (s.price <= lastP0_5) color = "10c0f0";
                    else if (s.price <= lastP5_10) color = "00842b";
                    else if (s.price <= lastP11_20) color = "88e030";
                    else if (s.price <= lastP21_30) color = "eecc22";
                    else if (s.price <= lastP31_40) color = "ff8500";
                    else if (s.price <= lastP41_80) color = "d00d0d";
                    else if (s.price <= lastP81_90) color = "a71de1";
                    else color = "1010a0";
                }

                // Name: Fuel Price + Date + Services (Emojis)
                StringBuilder nameBuilder = new StringBuilder();
                if (hasPrice) {
                    String shortDate = s.date.substring(5, 10).replace("-", ""); 
                    nameBuilder.append(fuelName).append(" ").append(df3.format(s.price)).append(" ").append(shortDate);
                } else {
                    nameBuilder.append("no ").append(fuelName);
                }
                
                // FIX 1: Add Service Emojis
                for (String emoji : s.services) {
                    nameBuilder.append(" ").append(emoji);
                }

                // Description: All fuels
                StringBuilder descBuilder = new StringBuilder();
                for (PriceInfo p : s.allPrices) {
                    String pDate = p.maj.substring(5, 10).replace("-", "");
                    descBuilder.append(p.nom).append(" ").append(df3.format(p.valeur)).append("€ ").append(pDate).append("<br>");
                }

                gpx.append("<wpt lat=\"").append(df5.format(s.lat)).append("\" lon=\"").append(df5.format(s.lon)).append("\">");
                gpx.append("<name>").append(escapeXml(nameBuilder.toString())).append("</name>");
                gpx.append("<desc><![CDATA[").append(descBuilder.toString()).append("]]></desc>");
                
                if (!hasPrice) gpx.append("<type>Absence</type>");
                
                gpx.append("<extensions>");
                gpx.append("<osmand:color>#").append(color).append("</osmand:color>");
                gpx.append("<osmand:icon>fuel</osmand:icon>");
                gpx.append("<osmand:background>circle</osmand:background>");
                gpx.append("</extensions></wpt>\n");
            }
            gpx.append("</gpx>");

            // Filename: FR-Gazole-full-240326.gpx
            String datePart = latestDate.substring(2, 4) + latestDate.substring(5, 7) + latestDate.substring(8, 10);
            String suffix = DOblack ? "full" : "only";
            String filename = "FR-" + fuelName + "-" + suffix + "-" + datePart + ".gpx";

            // Save
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

        } catch (Exception e) {
            Log.e("FuelAppFR", "Processing Error", e);
            return null;
        }
    }

    // --- XML Parsing Helper ---
    private List<Station> parseXML(File file, int targetFuelId) throws XmlPullParserException, IOException {
        List<Station> stations = new ArrayList<>();
        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "ISO-8859-1"));
        
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(reader);

        Station currentStation = null;
        String currentTag = ""; // FIX 1: Track current tag to handle text content correctly
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName();
                currentTag = name; // Set current tag

                if ("pdv".equals(name)) {
                    currentStation = new Station();
                    String latStr = parser.getAttributeValue(null, "latitude");
                    String lonStr = parser.getAttributeValue(null, "longitude");
                    if (latStr != null) currentStation.lat = Double.parseDouble(latStr) / 100000.0;
                    if (lonStr != null) currentStation.lon = Double.parseDouble(lonStr) / 100000.0;
                } else if (currentStation != null && "prix".equals(name)) {
                    String id = parser.getAttributeValue(null, "id");
                    String nom = parser.getAttributeValue(null, "nom");
                    String valeur = parser.getAttributeValue(null, "valeur");
                    String maj = parser.getAttributeValue(null, "maj");

                    if (id != null && valeur != null) {
                        int pId = Integer.parseInt(id);
                        double pVal = Double.parseDouble(valeur);
                        currentStation.allPrices.add(new PriceInfo(nom, pVal, maj));

                        if (pId == targetFuelId) {
                            currentStation.price = pVal;
                            currentStation.date = maj;
                        }
                    }
                }
            } else if (eventType == XmlPullParser.TEXT) {
                // FIX 1: If we are inside a <service> tag, read the text
                if ("service".equals(currentTag) && currentStation != null) {
                    String serviceText = parser.getText().trim();
                    if (serviceEmojis.containsKey(serviceText)) {
                        currentStation.services.add(serviceEmojis.get(serviceText));
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if ("pdv".equals(parser.getName()) && currentStation != null) {
                    stations.add(currentStation);
                    currentStation = null;
                }
                currentTag = ""; // Reset tag
            }
            eventType = parser.next();
        }
        return stations;
    }

    // --- Data Classes ---
    private static class Station {
        double lat = 0;
        double lon = 0;
        double price = 0; 
        String date = ""; 
        List<String> services = new ArrayList<>();
        List<PriceInfo> allPrices = new ArrayList<>();
    }

    private static class PriceInfo {
        String nom;
        double valeur;
        String maj;
        PriceInfo(String nom, double valeur, String maj) {
            this.nom = nom;
            this.valeur = valeur;
            this.maj = maj;
        }
    }

    // --- Legend & Utils ---
    private void renderLegend() {
        legendRows.removeAllViews();
        DecimalFormat df = new DecimalFormat("0.000");

        addLegendRow(df.format(lastPmin), df.format(lastP0_5), "#10c0f0", Color.BLACK, "Light Blue");
        addLegendRow(df.format(lastP0_5 + 0.001), df.format(lastP5_10), "#00842b", Color.WHITE, "Dark Green");
        addLegendRow(df.format(lastP5_10 + 0.001), df.format(lastP11_20), "#88e030", Color.BLACK, "Bright Green");
        addLegendRow(df.format(lastP11_20 + 0.001), df.format(lastP21_30), "#eecc22", Color.BLACK, "Yellow");
        addLegendRow(df.format(lastP21_30 + 0.001), df.format(lastP31_40), "#ff8500", Color.WHITE, "Orange");
        addLegendRow(df.format(lastP31_40 + 0.001), df.format(lastP41_80), "#d00d0d", Color.WHITE, "Red");
        addLegendRow(df.format(lastP41_80 + 0.001), df.format(lastP81_90), "#a71de1", Color.WHITE, "Violet");
        addLegendRow(df.format(lastP81_90 + 0.001), df.format(lastPmax), "#1010a0", Color.WHITE, "Navy Blue");

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
        space.setText("  ");
        space.setTextColor(textColor);
        row.addView(space);

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextColor(textColor);
        labelText.setTextSize(14);
        row.addView(labelText);

        legendRows.addView(row);
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
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
