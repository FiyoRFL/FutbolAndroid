# Fútbol — FIYO RFL (Android / Android TV)

Proyecto Android adaptado desde el paquete webOS `futbol.ipk`.

## Compatibilidad

- Android 6.0 o superior (API 23+).
- Android TV / Google TV con control remoto.
- Celulares y tablets Android con pantalla táctil.
- La interfaz cambia de distribución según el ancho de pantalla.

## Qué se conservó y adaptó

- Interfaz y colores originales de Fútbol.
- Consulta de categorías, partidos y canales desde el servicio configurado en la app original.
- Filtros de eventos y canales argentinos y orden de canales.
- Caché local de la última lista y actualización automática cada 15 minutos.
- Navegación por flechas y OK, toque en pantalla y zapping arriba/abajo.
- Reproducción nativa mediante AndroidX Media3 / ExoPlayer, para HLS (`m3u8`) y MPEG-TS (`ts`).
- Botón Atrás para volver a la lista.

## Generar el APK

El entorno donde se preparó este proyecto no tiene Android SDK ni acceso a los repositorios de Google, así que no fue posible compilar ni probar un APK aquí. El proyecto incluye un flujo de GitHub Actions para generar el APK en un runner con Android SDK.

### Opción A — GitHub Actions (más sencilla)

1. Creá un repositorio privado en GitHub.
2. Subí el contenido de esta carpeta al repositorio (no subas credenciales a un repositorio público).
3. Entrá en **Actions**.
4. Elegí **Build Fútbol APK** y tocá **Run workflow**.
5. Cuando termine, abrí la ejecución y descargá el artefacto **Futbol-FIYO-RFL-APK**.
6. Dentro está `app-debug.apk`, listo para instalar en Android.

### Opción B — Android Studio

1. Instalá Android Studio y Android SDK Platform 35.
2. Instalá JDK 17 y Gradle 8.9.
3. Abrí esta carpeta como proyecto en Android Studio.
4. Ejecutá `gradle assembleDebug` desde la terminal integrada.
5. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Importante

- El APK debug sirve para instalar y probar; Android puede pedir autorización para instalar apps de esta fuente.
- La reproducción real depende de que el servicio IPTV, la cuenta y cada enlace sigan activos. El proyecto no puede garantizar la disponibilidad de canales externos.
- La configuración de acceso al servicio está heredada de la IPK original y queda embebida en el código de la app. Mantené este proyecto en un repositorio privado y no compartas públicamente el APK o el código si no querés exponer esos datos.
- La app original no incluía los archivos de las fuentes Barlow Condensed aunque el HTML las referenciaba; Android usará fuentes de respaldo. Se puede incorporar la tipografía original si se facilitan sus archivos `.woff2`.
- Esta versión todavía necesita una prueba en un celular y en un Android TV reales, especialmente para validar el proveedor de streaming y sus formatos.
