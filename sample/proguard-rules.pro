-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keepresourcexmlelements manifest/application/meta-data@name=io.fabric.ApiKey
-printmapping mapping.txt
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep public class com.google.ads.** {
   public *;
}
-keep class com.google.analytics.** { *; }