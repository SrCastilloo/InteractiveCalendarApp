# Calendario Interactivo

Primera version Android en Kotlin + Jetpack Compose para un calendario compartido con usuarios autorizados, perfiles, eventos visibles para todos y recordatorios.

## Incluye

- Pantalla inicial con animacion y texto "Creado por Daniel Castillo".
- Registro, inicio de sesion y recuperacion de contrasena con Firebase Auth.
- Perfil editable: foto por URL, nombre, correo y nueva contrasena.
- Calendario mensual interactivo en primer plano.
- Eventos de uno o varios dias con autor, fecha de publicacion implicita, nombre y foto.
- Firestore en tiempo real para que todos los usuarios autenticados consulten eventos.
- Recordatorios locales persistentes: 15 dias antes y cada dia del evento.
- Firebase Cloud Messaging integrado para tokens y recepcion de pushes.
- Crashlytics y Analytics conectados.

## Puesta en marcha Firebase

1. Crea un proyecto Firebase en plan Spark.
2. Anade una app Android con paquete `com.danielcastillo.calendariointeractivo`.
3. Descarga el `google-services.json` real y reemplaza `app/google-services.json`.
4. Activa Authentication con proveedor Email/Password.
5. Crea Cloud Firestore.
6. Publica las reglas de `firebase/firestore.rules`.
7. En Android Studio, abre esta carpeta y sincroniza Gradle.

## Nota sobre notificaciones y Spark

La app usa WorkManager para programar recordatorios locales en cada dispositivo tras sincronizar eventos desde Firestore. Tambien registra tokens FCM y recibe mensajes push.

Para enviar automaticamente FCM a todos los dispositivos desde servidor en fechas futuras normalmente se necesita backend programado, por ejemplo Cloud Functions o Cloud Run. En el plan Spark esta primera version queda preparada en cliente sin depender de ese backend.

## Build

Requisitos recomendados:

- Android Studio actual con JDK 17+.
- Android SDK 37.
- Gradle 9.4.1 si se usa wrapper moderno.

Comando:

```powershell
.\gradlew.bat :app:assembleDebug
```

El `google-services.json` incluido es una plantilla. Reemplazalo por el real antes de probar autenticacion, Firestore, FCM, Crashlytics y Analytics.
