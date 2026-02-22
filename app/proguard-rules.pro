-keepattributes *Annotation*
-keepattributes JavascriptInterface
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Keep JavascriptInterface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView related classes from being obfuscated to avoid reflection issues
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}
# -keepclassmembers class com.m444.fuel.MainActivity { *;}
#-keep class android.webkit.** { *; }
#-keep class android.net.http.** { *; }
