# Outlier - Sample Space: Find the Outliers

Outlier is a fully offline, Android-only social deduction game inspired by undercover-style party games.

- FOSS under MIT license
- No backend or cloud services
- All gameplay data stays local on-device
- APKs distributed through GitHub Actions and GitHub Releases

## Download APK

- Latest signed APK (stable link):
  - `https://github.com/suprxsidh/outlier/releases/latest/download/outlier-latest-release.apk`
- Release page:
  - `https://github.com/suprxsidh/outlier/releases/latest`

## Features (MVP)

- Single-device pass-and-play gameplay
- Defaults to `7` players, `2` undercovers, `1` Mr White
- Configurable player count (`4-15`)
- Configurable undercover and Mr White counts
- Randomized secret pick order with guaranteed non-Mr-White first picker
- Post-elimination announcement shows only eliminated role + remaining role counts
- Mr White gets one guess when eliminated
- 650 family-friendly word pairs across multiple categories
- Local-only game state during play

## Asset attributions

- Asset source inventory: `docs/legal/ASSET_SOURCES.md`
- Third-party notices: `docs/legal/THIRD_PARTY_NOTICES.md`
- Third-party licenses: `licenses/third_party/`

## Stack

- Kotlin + Jetpack Compose
- Android Gradle Plugin 8.5
- minSdk 26 / targetSdk 34

## Local development

### 1) Prerequisites

- Java 17
- Android SDK with:
  - platform-tools
  - platforms;android-34
  - build-tools;34.0.0

### 2) Build and test

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Debug APK output:

- `app/build/outputs/apk/debug/app-debug.apk`
- Release APK output (local unsigned):
  - `app/build/outputs/apk/release/app-release-unsigned.apk`

### 3) Optional: set local SDK env vars

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="/opt/homebrew/share/android-commandlinetools"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
```

### 4) Install debug APK on a connected device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## GitHub APK distribution

- CI workflow (`.github/workflows/android-ci.yml`)
  - runs unit tests
  - builds debug APK
  - uploads debug APK artifact

- Release workflow (`.github/workflows/android-release.yml`)
  - triggers on tags like `v1.0.0`
  - builds signed release APK
  - attaches APK and SHA256 to GitHub Release

## Generate signing keystore (free)

Run locally once:

```bash
keytool -genkeypair -v \
  -keystore outlier-release.jks \
  -alias outlier \
  -keyalg RSA -keysize 4096 \
  -validity 36500
```

Encode keystore for GitHub secret:

```bash
base64 -i outlier-release.jks | pbcopy
```

Add these repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

### Alternative base64 command (Linux/GitHub runner style)

```bash
base64 outlier-release.jks | tr -d '\n' | pbcopy
```

## Release command

```bash
git tag -a v1.0.0 -m "Outlier v1.0.0"
git push origin v1.0.0
```

The release workflow will produce:

- `outlier-v1.0.0-release.apk`
- `outlier-v1.0.0-release.apk.sha256`
