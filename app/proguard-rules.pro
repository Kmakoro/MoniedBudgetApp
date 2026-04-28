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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Keep Room entities
-keep class com.monied.budgetapp.data.model.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }
