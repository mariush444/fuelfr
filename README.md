# <p align="center"> ![image](https://github.com/mariush444/fuelfr/blob/main/app/src/main/res/mipmap-xxxhdpi/m444_fuel.png) <br>Fuel🇫🇷GPX · Fuel🇪🇸GPX </p>

<!-- ### <p align="center">Android · OsmAnd add-on · GPX fuel price layer</p -->

## <p align="center">Fuel prices exported as color-coded icons GPX for OsmAnd</p>
<div align="center">
  
![Platform](https://img.shields.io/badge/platform-Android-green)
![Min SDK](https://img.shields.io/badge/minSdk-21-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)
![osmand](https://img.shields.io/badge/osmand-add_on-orange?logo=osmand)
![osmand](https://img.shields.io/badge/layer-GPX-navy?logo=osmand)
![GitHub Tag](https://img.shields.io/github/v/tag/mariush444/fuelfr)

</div>

Fuel🇫🇷GPX and Fuel🇪🇸GPX are open-source **OsmAnd add-on** Android applications.
They download official public fuel price datasets and generate a **GPX layer** compatible with **OsmAnd**.
Fuel prices are visualized using **color-coded icons**, dynamically calculated between the minimum and maximum price of the selected fuel type.
<br><br>
Both applications are entirely open-source and respect user privacy.<br>
All processing is performed locally on the device.<br>
No analytics, tracking, or advertisements are included.
<br><br>
Designed for drivers and travelers who want a simple and transparent way to visualize fuel prices directly in OsmAnd.

---

## Color Logic

For the selected fuel type, prices are divided dynamically between:

`min price → max price`

Colors are assigned as follows:

- ![](https://img.shields.io/badge/Light%20Blue-10c0f0) – cheapest (min → min + 5%)
- ![](https://img.shields.io/badge/Dark%20Green-00842b) – 5% → 10%
- ![](https://img.shields.io/badge/Light%20Green-88e030) – 10% → 20%
- ![](https://img.shields.io/badge/Yellow.-eecc22) – 20% → 30%
- ![](https://img.shields.io/badge/Orange-orange) – 30% → 70%
- ![](https://img.shields.io/badge/Red-ff0000) – 70% → 80%
- ![](https://img.shields.io/badge/Violet-8A2BE2) – 80% → 90%
- ![](https://img.shields.io/badge/Dark%20Blue-navy) – most expensive (90% → max)

If a station **does not sell** the selected fuel type:

- ![](https://img.shields.io/badge/Black-black) = fuel not available

---

## Functionality

### 1. Black Icons (Fuel Not Sold)

- Displayed in OsmAnd as a separate category: `"Absence"`
- Can be disabled in OsmAnd view
- Can be excluded from the generated GPX file  
  (option: *"Include stations without this fuel"*)

This reduces file size if disabled.

---

### 2. Supported Fuel Types

The application supports the following fuel types (as defined in official data):

<div align="center">

<table style="width:100%; max-width:850px;">
<tr>
<th align="left" width="22%">🇫🇷 France</th>
<td>
Gazole · SP95 · SP98 · E85 · E10 · GPLc
</td>
</tr>
<tr><td colspan="2" style="height:12px;"></td></tr>
<tr>
<th align="left">🇪🇸 Spain</th>
<td>
<b>Conventional</b><br>
Gasoleo A · Gasoleo B · Gasoleo Premium ·  
Gasolina 95 E5 · Gasolina 95 E10 · Gasolina 95 E25 ·  
Gasolina 95 E5 Premium · Gasolina 95 E85 ·  
Gasolina 98 E5 · Gasolina 98 E10<br><br>
<b>Renewable</b><br>
Biodiesel · Bioetanol · Diésel Renovable · Gasolina Renovable<br><br>
<b>Gas & Alternative</b><br>
Gas Natural Comprimido · Gas Natural Licuado ·  
Biogas Natural Comprimido · Biogas Natural Licuado ·  
Gases licuados del petróleo · Hidrogeno · Adblue
</td>
</tr>
</table>

</div>

The user selects which fuel type should determine the color ordering.

---

### 3. Waypoint Name Contains

Each gas station waypoint includes:

<div align="center">

<table style="width:95%; table-layout:fixed; font-size:13px;">
<tr>
<th style="width:15%; text-align:left;">🇫🇷 France</th>
<td style="word-wrap:break-word;">
Fuel price (selected type) • Date of price update • Selected services (represented using UTF icons)
</td>
</tr>
<tr>
<th style="text-align:left;">🇪🇸 Spain</th>
<td style="word-wrap:break-word;">
Name of selected type of fuel • Fuel price
</td>
</tr>
</table>

</div>


---

### 4. Waypoint Description Contains

- All available fuel prices
- Dates of each price update (France only)
- Only fuels sold at that station

---

## Output

The generated GPX file can be directly imported into:

OsmAnd

and used as a visual fuel price layer.

---

## Privacy

Both apps: Fuel🇫🇷GPX and Fuel🇪🇸GPX respects user privacy.

- The app does **not** collect personal data.
- The app does **not** require user accounts.
- The app does **not** use analytics.
- The app does **not** contain advertisements.
- The app does **not** track user location.

The application only:

- Downloads public fuel price data from:<br>

  France  
  https://donnees.roulez-eco.fr/opendata/instantane
  
  Spain  
  https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/

- Generates a GPX file locally on the device.

All processing is done locally on the device.
No data is sent to any third-party servers.

Internet permission is used **only** to download official open data.

---

## Build Instructions

### Requirements

- Linux (recommended)
- Java 17
- Android SDK
- Gradle Wrapper (included)

### Clone the repository

```bash
git clone https://github.com/mariush444/fuelfr.git
cd fuelfr
```

### Build Debug APK

```bash
./gradlew assembleESDebug
./gradlew assembleFRDebug
```

APK will be located at:

```
app/build/outputs/apk/es/debug/
app/build/outputs/apk/fr/debug/
```

### Build Release APK

```bash
./gradlew assembleESRelease
./gradlew assembleFRRelease
```

APK will be located at:

```
app/build/outputs/apk/es/release/
app/build/outputs/apk/fr/release/
```

### Notes

- Make sure `JAVA_HOME` is correctly set.
- Make sure Android SDK is installed and `local.properties` contains:

```
sdk.dir=/path/to/your/android/sdk
```
---
## Screenshots

<div align="center">

<table>
<tr>
<td align="center">
<img src="https://github.com/user-attachments/assets/be94c5ad-5c86-41c7-8c2e-b696c639cc90" width="220">
</td>
<td align="center">
<img src="https://github.com/user-attachments/assets/2e4a3818-e233-4306-9211-7720b9553b46" width="220">
</td>
<td align="center">
<img src="https://github.com/user-attachments/assets/e2e78b69-6405-4292-b1f7-a7c3dff173e6" width="220">
</td>
</tr>

<tr>
<td align="center">
<img src="https://github.com/user-attachments/assets/0e19a228-fcec-451a-a152-0a40222878ed" width="220">
</td>
<td align="center">
<img src="https://github.com/user-attachments/assets/e9592f23-9c8b-4bfc-be7a-f4500bacfb45" width="220">
</td>
<td align="center">
<img src="https://github.com/user-attachments/assets/d15e118c-b727-4a7f-b3a9-0645f3203852" width="220">
</td>
</tr>
</table>

</div>
