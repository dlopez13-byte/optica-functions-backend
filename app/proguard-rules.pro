# ====== REGLAS DE OPTIMIZACIÓN DE PRODUCCIÓN - ÓPTICA "MIRADA Y ESTILO" ======

# 1. Atributos Generales de Preservación
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# 2. Modelos de Datos (GSON / Room)
# QA: No renombrar campos de modelos para que el parseo JSON no falle
-keep class com.example.optica.model.** { *; }
-keepclassmembers class com.example.optica.model.** { *; }

# 3. Retrofit y OkHttp
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# 4. Firebase (Auth, Firestore, Storage, Messaging, Analytics)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# 5. Glide (Carga de Imágenes)
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# 6. Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Dao

# 7. Clases de Seguridad y Utilidades (Ofuscación Selectiva)
# QA: Mantener nombres de métodos de utilidad crítica para evitar errores de reflexión
-keep class com.example.optica.utils.** { *; }

# 8. Shimmer y UI Components
-keep class com.facebook.shimmer.** { *; }
-keep class com.google.android.material.** { *; }
