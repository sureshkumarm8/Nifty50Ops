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

# General rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepnames class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    <fields>;
    <methods>;
}
-keepclassmembers class **$*COROUTINE$* { *; }


# Retrofit, OkHttp, and Gson
-keep class retrofit2.** { *; }
-keepclassmembers interface retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# If you use R8 full mode, you might need this for OkHttp:
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomOpenHelper { *; }
-keepclassmembers class androidx.room.RoomDatabase {
    public static final int MAX_BIND_PARAMETER_CNT;
}
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
    protected <init>(...);
    public static ** Companion; # For Kotlin companion objects with database builder methods
}
-keepclassmembers class * extends androidx.room.Entity { *; }
-keepclassmembers class * extends androidx.room.Dao { *; }
-keepclassmembers class * extends androidx.room.TypeConverter { *; }


# Hilt
-keep class dagger.hilt.android.internal.** { *; }
-keep class *_*HiltModules_* { *; }
-keep class *_*HiltComponents_* { *; }
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.DefineComponent class *
-keep @dagger.hilt.EntryPoint class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @javax.inject.Inject class *
-keep @javax.inject.Singleton class *
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}
-keepclassmembers @dagger.hilt.android.AndroidEntryPoint class * {
    @javax.inject.Inject <fields>;
}
-keepclassmembers @dagger.hilt.EntryPoint class * {
    @javax.inject.Inject <fields>;
}
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}

# Firebase (General - some services might need more specific rules)
-keep class com.google.firebase.** { *; }
-keepnames class com.google.android.gms.measurement.AppMeasurement { *; }
-keepnames class com.google.android.gms.measurement.AppMeasurement$* { *; }
-keepnames class com.google.firebase.analytics.FirebaseAnalytics { *; }
-keepnames class com.google.firebase.analytics.FirebaseAnalytics$* { *; }

# Keep custom model classes (data classes, etc.) if they are serialized/deserialized
# Replace com.example.nifty50ops.model.** with your actual model package
-keep class com.example.nifty50ops.model.** { *; }
-keepclassmembers class com.example.nifty50ops.model.** { *; }

# Keep any classes used with reflection (e.g., for CSV parsing or other dynamic instantiation)
# Example: -keep class com.example.myproject.MyReflectedClass { *; }

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Application class
-keep class com.example.nifty50ops.MyApplication { *; } # Replace with your Application class if you have one

# Keep Activities
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.FragmentActivity
-keep public class * extends androidx.activity.ComponentActivity

# Keep Fragments
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# Keep Services
-keep public class * extends android.app.Service

# Keep BroadcastReceivers
-keep public class * extends android.content.BroadcastReceiver

# Keep ContentProviders
-keep public class * extends android.content.ContentProvider

# Keep Parcelables
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# For Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <fields>;
}
-keepclassmembers class **$serializer {
    <methods>;
}
-keepclassmembers class * implements androidx.compose.runtime.Composer {
    <methods>;
}
-keepclassmembers class * implements androidx.compose.runtime.Composition {
    <methods>;
}
-keepclassmembernames class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Apache POI (if used directly, these might be needed)
# -dontwarn org.apache.poi.**
# -keep class org.apache.poi.** { *; }
# -keep interface org.apache.poi.** { *; }

# OpenCSV (if used directly)
# -keep class com.opencsv.** { *; }
# -keep interface com.opencsv.** { *; }

# Make sure to test your release build thoroughly after enabling ProGuard/R8.
# Add more specific rules if you encounter any issues.
