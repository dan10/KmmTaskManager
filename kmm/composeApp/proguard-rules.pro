# Optimize but don't obfuscate
-dontobfuscate

# Ignore duplicate classes from shared module (KMP has both Android and JVM targets)
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder

# Keep attributes for debugging and reflection
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Compose UI - manter apenas as classes necessárias
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }
-keep class androidx.compose.ui.platform.AndroidComposeViewAccessibilityDelegateCompat { *; }
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    *** getSemanticsOwner(...);
}
-dontwarn androidx.compose.**


# Kotlinx Serialization - required for JSON serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keep,includedescriptorclasses class com.danioliveira.taskmanager.**$$serializer { *; }
-keepclassmembers class com.danioliveira.taskmanager.** {
    *** Companion;
}
-keepclasseswithmembers class com.danioliveira.taskmanager.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep @kotlinx.serialization.Serializable class com.danioliveira.taskmanager.** { *; }

# Ktor client

# Kotlin metadata and reflection
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# AndroidX and Material3
-dontwarn androidx.**
-dontwarn com.google.android.material.**

# Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

