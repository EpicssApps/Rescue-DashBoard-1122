# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-keep class com.google.re2j.** { *; }
-dontwarn com.google.re2j.**

# WebView and Chromium related rules to prevent crashes
-keep class android.webkit.** { *; }
-keep class org.chromium.** { *; }
-dontwarn org.chromium.**

# Google HTTP Client
-keep class com.google.http.** { *; }
-dontwarn com.google.http.**

# Google Auth
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.auth.**

# Apache HTTP (used by some Google API client internals)
-dontwarn org.apache.http.**
-dontwarn com.google.api.client.http.apache.**
