
# Glide specific rules #

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

#JSOUP
-keep public class org.jsoup.** {
public *;
}

#gpuimage
-dontwarn jp.co.cyberagent.android.gpuimage.**

#kotlin
-dontwarn kotlin.**

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-dontoptimize
-dontpreverify


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class com.android.vending.licensing.ILicensingService


-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
