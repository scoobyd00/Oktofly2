# OktoFly Android App

A native Android app for the [oktofly.com](https://oktofly.com) drone weather forecast service by Airdata UAV.

## Features

- 📍 **Auto-location** — detects your GPS location and loads the forecast for your area
- 🌤️ **Full oktofly forecast** — hourly Blue/Orange/Red fly ratings, wind at altitude, satellites, Kp index, precipitation probability
- 🌙 **Dark drone theme** — navy/blue UI matching the oktofly aesthetic
- 🔄 **Refresh button** — re-fetch location and reload forecast
- 🔒 **Permission handling** — graceful location permission request flow with rationale

## Data displayed (from oktofly.com)

| Data | Description |
|------|-------------|
| **Fly Status** | Blue (safe) / Orange (elevated risk) / Red (high risk) per hour |
| **Wind & Gusts** | Speed at different altitudes using AI model trained on millions of flights |
| **Satellites (Sats)** | Forecasted GPS/GLONASS satellite count at your location & altitude |
| **Kp Index** | Geomagnetic storm index (0–9). Above 4 may affect GPS |
| **Precip Probability** | Chance of rain, snow, hail or sleet |
| **Pilots Nearby / Planned** | Other pilots currently using oktofly in your area |

## Setup

### Requirements
- Android Studio Hedgehog or newer
- Android SDK 26+
- Google Play Services (for location)

### Steps

1. **Clone / extract** this project folder
2. Open in **Android Studio**: File → Open → select `OktoFlyApp/`
3. Let Gradle sync
4. Connect an Android device or start an emulator
5. Press **Run** (▶)

### Permissions
The app requests `ACCESS_FINE_LOCATION` to load the forecast for your exact location. This is used only to build the oktofly.com URL — no data is stored or transmitted elsewhere.

## Architecture

```
MainActivity.kt         — Activity entry point
OktoFlyApp.kt          — Main composable (permission flow, location, WebView)
ui/theme/Theme.kt      — Material3 dark color scheme
```

The app uses a **WebView** to render oktofly.com because the site has no public API — all forecast data is rendered via JavaScript. The WebView is configured with:
- JavaScript enabled
- Geolocation allowed (so oktofly can also detect location if needed)
- A modern Chrome mobile user-agent for best compatibility
- Navigation restricted to oktofly.com / airdata.com domains

## Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose + Material3 | UI |
| Accompanist Permissions | Runtime permission handling |
| Google Play Services Location | GPS location |
| AndroidX WebKit | Modern WebView |

## Build

```bash
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```
