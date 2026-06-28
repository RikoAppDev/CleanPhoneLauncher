# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# Keep line numbers for readable crash reports (paired with Crashlytics mapping upload).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Enum constant names are persisted to SharedPreferences via .name and read back via
# entries.firstOrNull { it.name == ... }, so they must keep their original names.
-keepclassmembers enum dev.rikoapp.cleanphonelauncher.** { *; }