# Proguard rules for Money Manager

# Keep Room database and entities
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep data models
-keep class com.example.moneymanager.data.model.** { *; }

# KotlinX Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Compose
-keep class androidx.compose.** { *; }

# Apache POI (XLSX import) — desktop classes not on Android
-dontwarn aQute.bnd.annotation.spi.ServiceConsumer
-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn javax.xml.crypto.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.maven.**
-dontwarn org.apache.tools.ant.**
-dontwarn org.bouncycastle.**
-dontwarn org.ietf.jgss.**
-dontwarn org.osgi.framework.**
-dontwarn com.github.javaparser.**

# POI + XMLBeans + StAX/Woodstox use reflection/service-loading heavily — keep everything
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class com.ctc.wstx.** { *; }
-keep class javax.xml.stream.** { *; }
-keep class org.codehaus.stax2.** { *; }
-keep class org.openxmlformats.schemas.** { *; }
-keepclassmembers class org.apache.poi.** { *; }

# R8-generated suppressions: desktop/optional deps referenced by kept POI classes but absent on Android
-dontwarn com.ctc.wstx.shaded.**
-dontwarn com.sun.org.apache.xml.internal.**
-dontwarn de.rototor.pdfbox.graphics2d.**
-dontwarn javax.imageio.**
-dontwarn javax.swing.**
-dontwarn org.apache.jcp.xml.dsig.**
-dontwarn org.apache.pdfbox.**
-dontwarn org.apache.xml.security.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.w3c.dom.events.**
-dontwarn org.w3c.dom.svg.**
-dontwarn org.w3c.dom.traversal.**
