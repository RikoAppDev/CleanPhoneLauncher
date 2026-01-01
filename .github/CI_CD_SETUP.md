# CI/CD Setup Guide

## Overview

This project uses GitHub Actions for automated CI/CD with the following features:

- ✅ **Automatic versioning** based on commit messages (Semantic Versioning)
- ✅ **Automatic builds** on every push to main
- ✅ **Automatic GitHub releases** with tags matching app version
- ✅ **APK and AAB artifacts** included in releases
- 🔮 **Play Store deployment** (ready to enable)

## How It Works

### Automatic Versioning

The version is calculated automatically based on:

1. **Latest Git tag** (e.g., `v1.2.3`)
2. **Commit message prefixes** (Conventional Commits):
   - `fix:` → Patch bump (1.2.3 → 1.2.4)
   - `feat:` → Minor bump (1.2.3 → 1.3.0)
   - `BREAKING CHANGE` or `feat!:` or `fix!:` → Major bump (1.2.3 → 2.0.0)

### Version Code Calculation

For Play Store compatibility, version code is calculated as:
```
VERSION_CODE = MAJOR * 10000 + MINOR * 100 + PATCH
```

Example: Version 1.2.3 → Version Code 10203

### Workflows

#### 1. CI Workflow (`ci.yml`)
- **Triggers:** Every push and PR to main/develop
- **Jobs:**
  - Lint check
  - Unit tests
  - Build verification

#### 2. Release Workflow (`release.yml`)
- **Triggers:** 
  - Push to main branch
  - Manual workflow dispatch
- **Jobs:**
  - Version calculation
  - Build release APK & AAB
  - Create GitHub release with tag

## Commit Message Examples

```bash
# Patch release (1.0.0 → 1.0.1)
git commit -m "fix: resolve crash on startup"

# Minor release (1.0.0 → 1.1.0)
git commit -m "feat: add dark mode support"

# Major release (1.0.0 → 2.0.0)
git commit -m "feat!: redesign entire UI"
# or
git commit -m "fix: update API

BREAKING CHANGE: New API requires authentication"
```

## Manual Version Bump

You can manually trigger a release with a specific version bump:

1. Go to **Actions** tab in GitHub
2. Select **Build & Release** workflow
3. Click **Run workflow**
4. Choose version bump type (patch/minor/major)

---

## Setting Up Release Signing

### Step 1: Generate a Keystore

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

### Step 2: Encode Keystore for GitHub Secrets

```bash
base64 -i release-keystore.jks > keystore-base64.txt
```

### Step 3: Add GitHub Secrets

Go to **Settings → Secrets and variables → Actions** and add:

| Secret Name | Description |
|-------------|-------------|
| `KEYSTORE_BASE64` | Content of keystore-base64.txt |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias (e.g., "release") |
| `KEY_PASSWORD` | Key password |

### Step 4: Update build.gradle.kts

Add signing config in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of config
        }
    }
}
```

### Step 5: Uncomment Signing in Workflow

In `.github/workflows/release.yml`, uncomment the keystore decoding step.

---

## Setting Up Play Store Deployment

### Prerequisites

1. **Google Play Console account** with your app already created
2. **Service account** with Play Store API access

### Step 1: Create Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable **Google Play Android Developer API**
4. Go to **IAM & Admin → Service Accounts**
5. Create a new service account
6. Create and download JSON key

### Step 2: Grant Play Console Access

1. Go to [Google Play Console](https://play.google.com/console)
2. Go to **Users and permissions → Invite new users**
3. Add the service account email (from JSON key)
4. Grant permissions:
   - View app information
   - Create, edit, and delete draft apps
   - Release apps to testing tracks
   - Manage production releases

### Step 3: Add GitHub Secret

Add this secret in GitHub:

| Secret Name | Description |
|-------------|-------------|
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | Full content of the service account JSON file |

### Step 4: Enable Play Store Deployment

Uncomment the `deploy-play-store` job in `.github/workflows/release.yml`.

### Deployment Tracks

You can deploy to different tracks:
- `internal` - Internal testing (fastest)
- `alpha` - Closed testing
- `beta` - Open testing
- `production` - Public release

---

## Environment Variables

The build system uses these environment variables:

| Variable | Description |
|----------|-------------|
| `VERSION_NAME` | Semantic version (e.g., "1.2.3") |
| `VERSION_CODE` | Play Store version code (integer) |

These are automatically set by the CI/CD pipeline and used in `build.gradle.kts`.

---

## Accessing Version in App

The version info is available in your app via BuildConfig:

```kotlin
// In your Kotlin code
val versionName = BuildConfig.VERSION_NAME_FULL  // e.g., "1.2.3"
val versionCode = BuildConfig.VERSION_CODE_INT   // e.g., 10203
```

---

## Troubleshooting

### Build fails with signing error
- Ensure all signing secrets are correctly set
- Verify keystore was encoded properly with base64

### Version not incrementing
- Ensure commits follow conventional commit format
- Check that tags exist: `git tag -l`

### Play Store upload fails
- Verify service account has correct permissions
- Ensure app is already created in Play Console
- Check that version code is higher than any existing version

---

## File Structure

```
.github/
└── workflows/
    ├── ci.yml           # Continuous Integration
    └── release.yml      # Build & Release
```

