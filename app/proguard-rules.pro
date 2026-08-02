# URIKAI release R8 / ProGuard

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations

# SharedPreferences で Enum.name を保存しているため
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Compose / Kotlin
-dontwarn kotlin.**
-dontwarn kotlinx.**
