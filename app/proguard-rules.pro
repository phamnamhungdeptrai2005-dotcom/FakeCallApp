# Keep Gson model classes
-keepclassmembers class com.fakecall.app.Contact {
    <fields>;
    <init>(...);
}
-keep class com.google.gson.** { *; }
