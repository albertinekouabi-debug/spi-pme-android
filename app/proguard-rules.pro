# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.spipme.app.**$$serializer { *; }
-keepclassmembers class com.spipme.app.** { *** Companion; }
-keepclasseswithmembers class com.spipme.app.** { kotlinx.serialization.KSerializer serializer(...); }
