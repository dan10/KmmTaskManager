# Flutter engine / embedding
-keep class io.flutter.** { *; }
-dontwarn io.flutter.**

# Gson (if used by plugins)
-keep class com.google.gson.** { *; }

# Keep Semantics identifier strings reachable
# (string constants are kept; this is just to be conservative with framework usage)
-keep class androidx.core.view.accessibility.** { *; }
-dontwarn androidx.core.view.accessibility.**


