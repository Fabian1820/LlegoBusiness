# Apollo GraphQL — keep generated models and cache key resolvers
-keep class com.apollographql.apollo.** { *; }
-keep class com.llego.multiplatform.graphql.** { *; }
-keepnames class com.apollographql.apollo.cache.normalized.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Compose — compiler handles most, but keep entry points
-keep class androidx.compose.** { *; }
-keep class com.llego.app.** { *; }

# Kotlin reflection used by some libraries
-keep class kotlin.Metadata { *; }
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations

# OkHttp (used by Ktor on Android)
-dontwarn okhttp3.**
-dontwarn okio.**
