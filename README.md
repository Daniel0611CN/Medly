# Medly

Medly es una aplicación Android nativa para controlar tratamientos, existencias y tomas diarias. Está pensada para uso personal y no sustituye el consejo de un profesional sanitario.

## Funcionalidades

- **Hoy:** lista de tratamientos y check-in diario. Cada check-in descuenta la dosis y abre una caja automáticamente cuando faltan unidades sueltas.
- **Inventario:** alta, edición y eliminación de medicamentos con nombre, formato, dosis, frecuencia, unidades por caja, cajas cerradas, unidades sueltas y límite mensual.
- Cálculo de días restantes y estado (`Bien`, `Bajo`, `Crítico`, `Agotado`), con advertencia visual al quedar siete días o menos.
- Persistencia local con Room, arquitectura MVVM, `Flow`/`StateFlow` y navegación Compose Material 3.
- Logo vectorial minimalista incluido en `app/src/main/res/drawable/ic_medly_logo.xml`.
- Widget de pantalla de inicio para teléfonos y tablets, adaptable horizontal y verticalmente, con las tomas y el stock más urgente.
- Modo claro y modo oscuro, con detección inicial del tema del sistema y selector persistente desde la barra superior.

## Ejecutar

Requiere Android Studio reciente, JDK 21 (o el JDK 17 configurado por Android Studio) y Android SDK 35. El proyecto no necesita backend ni conexión de red en ejecución.

```bash
./gradlew test
./gradlew assembleDebug
```

En Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

El APK generado queda en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo, copia ese archivo al teléfono, habilita temporalmente **Instalar aplicaciones desconocidas** para el gestor de archivos y ábrelo. También puedes arrastrarlo a un emulador desde Android Studio.

## Descargar únicamente el APK

Las personas que solo quieran instalar la aplicación **no necesitan Android Studio ni conocimientos de programación**. Cuando haya una versión publicada, deben seguir estos pasos:

**[Descargar Medly v1.1.0 (APK)](https://github.com/Daniel0611CN/Medly/releases/download/v1.1.0/Medly-v1.1.0.apk)**

1. Abrir la sección [Releases de Medly](https://github.com/Daniel0611CN/Medly/releases).
2. Entrar en la versión más reciente.
3. Descargar el archivo `.apk` adjunto, por ejemplo `Medly-release.apk`.
4. Abrir el archivo desde la barra de descargas o desde la carpeta `Descargas` del móvil.
5. Si Android lo solicita, activar temporalmente **Permitir instalar aplicaciones desconocidas** para el navegador o gestor de archivos que se esté usando.
6. Volver al instalador, pulsar **Instalar** y abrir Medly.

La aplicación se descarga desde GitHub como un archivo APK independiente; no es necesario clonar el repositorio ni instalar Gradle. Si Android muestra un aviso de seguridad, comprueba que el APK procede de la sección oficial de [Releases](https://github.com/Daniel0611CN/Medly/releases).

El repositorio incluye un workflow que compila y publica automáticamente el APK cuando se crea una etiqueta `v*` (por ejemplo, `v1.0.0`). Si no aparece todavía una versión en [Releases](https://github.com/Daniel0611CN/Medly/releases), revisa la ejecución de **Actions > Build and publish APK**: la Release se crea cuando termina correctamente la compilación. El APK `debug` generado por este primer workflow sirve para pruebas; para una distribución pública posterior se recomienda sustituirlo por un APK `release` firmado.

## Generar el APK desde Android Studio

Esta opción es para quien quiera compilar la aplicación manualmente:

1. Descargar el proyecto desde GitHub con **Code > Download ZIP**, o clonarlo.
2. Abrir la carpeta en Android Studio.
3. Instalar, si Android Studio lo solicita, Android SDK 35 y aceptar la sincronización de Gradle.
4. Esperar a que termine **Gradle Sync**.
5. Seleccionar **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
6. Pulsar la notificación **Locate** o abrir `app/build/outputs/apk/debug/app-debug.apk`.
7. Copiar ese APK al teléfono e instalarlo siguiendo los pasos anteriores.

También puede generarse desde una terminal:

```powershell
.\gradlew.bat assembleDebug
```

El APK estará en `app/build/outputs/apk/debug/app-debug.apk`.

## Arquitectura

`data/` contiene la entidad Room, DAO, base de datos, repositorio y la lógica de consumo. `ui/` contiene el `ViewModel` y el tema Material 3. `MainActivity` expone las pantallas `Hoy` e `Inventario` mediante `NavigationBar`.

## Widget de medicamentos

Después de instalar Medly, mantén pulsado un espacio vacío de la pantalla de inicio y elige **Widgets > Medly**. Puedes cambiar su tamaño para usarlo en un teléfono o tablet. El widget muestra hasta tres tratamientos ordenados por días restantes, el estado del stock y un acceso directo a la aplicación. Se actualiza tras añadir, editar, eliminar o registrar una toma, y Android también puede refrescarlo periódicamente.

El widget requiere Android 8.0 (API 26) o posterior, igual que la aplicación.

## Modelo de datos

Cada medicamento guarda nombre, formato, dosis, tomas al día, unidades por caja, cajas cerradas, unidades sueltas, límite mensual, consumo del mes y la fecha del último check-in. El stock nunca baja de cero: si no hay suficientes unidades para una toma o se ha alcanzado el límite mensual, el check-in queda desactivado.

## Limitaciones

- Los datos se almacenan únicamente en el dispositivo y no hay sincronización entre teléfonos.
- El límite mensual se registra sobre el mes en curso, pero el reinicio automático por cambio de mes todavía debe añadirse.
- Las alertas son visuales dentro de la aplicación; no se programa una notificación del sistema.
