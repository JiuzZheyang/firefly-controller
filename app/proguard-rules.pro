# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.firefly.controller.data.model.** { *; }
