# <p align="center"> ![image](https://github.com/mariush444/fuelfr/blob/main/app/src/main/res/mipmap-xxxhdpi/m444_fuel.png) <br> Fuel🇫🇷GPX </p>

## Fuel prices in France exported as colored GPX for OsmAnd
![Platform](https://img.shields.io/badge/platform-Android-green)
![Min SDK](https://img.shields.io/badge/minSdk-21-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)
![osmand](https://img.shields.io/badge/osmand-add_on-orange?logo=osmand)
![osmand](https://img.shields.io/badge/layer-GPX-navy?logo=osmand)
![GitHub Tag](https://img.shields.io/github/v/tag/mariush444/fuelfr)

Fuel🇫🇷GPX is an **Osmand add-on** Android app that downloads official French fuel price data from **donnees.roulez-eco.fr** and generates a **GPX file** for use with **OsmAnd**. The GPX file visualizes fuel prices with **color-coded icons**, representing the price range from the cheapest to the most expensive. The app supports various fuel types, including **Gazole**, **SP95**, **SP98**, **E85**, **E10**, and **GPLc**.

This app is entirely open-source and respects user privacy. All processing is done locally, and no personal data is collected. There are no ads, tracking, or analytics. Internet access is only required to fetch fuel price data. Ideal for travelers and drivers looking to find fuel prices in France on their route.

---

## 🎨 Color Logic

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

## ⚙️ Functionality

### 1️⃣ Black Icons (Fuel Not Sold)

- Displayed in OsmAnd as a separate category: `"Absence"`
- Can be disabled in OsmAnd view
- Can be excluded from the generated GPX file  
  (option: *"Include stations without this fuel"*)

This reduces file size if disabled.

---

### 2️⃣ Supported Fuel Types

The application supports the following fuel types (as defined in official data):

- Gazole
- SP95
- SP98
- E85
- E10
- GPLc

The user selects which fuel type should determine the color ordering.

---

### 3️⃣ Waypoint Name Contains

Each gas station waypoint includes:

- Fuel price (selected type)
- Date of price update
- Selected services (represented using UTF icons)

---

### 4️⃣ Waypoint Description Contains

- All available fuel prices
- Dates of each price update
- Only fuels sold at that station

---

## 📦 Output

The generated GPX file can be directly imported into:

OsmAnd

and used as a visual fuel price layer.

---

## 🔐 Privacy

Fuel🇫🇷GPX respects user privacy.

- The app does **not** collect personal data.
- The app does **not** require user accounts.
- The app does **not** use analytics.
- The app does **not** contain advertisements.
- The app does **not** track user location.

The application only:

- Downloads public fuel price data from:
  https://donnees.roulez-eco.fr
- Generates a GPX file locally on the device.

All processing is done locally on the device.
No data is sent to any third-party servers.

Internet permission is used **only** to download official open data.

---

## 🛠 Build Instructions

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
./gradlew assembleDebug
```

APK will be located at:

```
app/build/outputs/apk/debug/
```

### Build Release APK

```bash
./gradlew assembleRelease
```

APK will be located at:

```
app/build/outputs/apk/release/
```

### Notes

- Make sure `JAVA_HOME` is correctly set.
- Make sure Android SDK is installed and `local.properties` contains:

```
sdk.dir=/path/to/your/android/sdk
```
---
## Screenshots
![Screenshot1a](https://github.com/user-attachments/assets/be94c5ad-5c86-41c7-8c2e-b696c639cc90)
![Screenshot2a](https://github.com/user-attachments/assets/2e4a3818-e233-4306-9211-7720b9553b46)
![Screenshot3a](https://github.com/user-attachments/assets/e2e78b69-6405-4292-b1f7-a7c3dff173e6)
![Screenshot4a](https://github.com/user-attachments/assets/0e19a228-fcec-451a-a152-0a40222878ed)
![Screenshot5a](https://github.com/user-attachments/assets/e9592f23-9c8b-4bfc-be7a-f4500bacfb45)
![Screenshot6a](https://github.com/user-attachments/assets/d15e118c-b727-4a7f-b3a9-0645f3203852)


