# Medly

Medly es una aplicación Android nativa para controlar tratamientos, existencias y tomas diarias. Está pensada para uso personal y no sustituye el consejo de un profesional sanitario.

## Funcionalidades

- **Hoy:** lista de tratamientos y check-in diario. Cada check-in descuenta la dosis y abre una caja automáticamente cuando faltan unidades sueltas.
- **Inventario:** alta, edición y eliminación de medicamentos con nombre, formato, dosis, frecuencia, unidades por caja, cajas cerradas, unidades sueltas y límite mensual.
- Cálculo de días restantes y estado (`Bien`, `Bajo`, `Crítico`, `Agotado`), con advertencia visual al quedar siete días o menos.
- Persistencia local con Room, arquitectura MVVM, `Flow`/`StateFlow` y navegación Compose Material 3.
- Logo vectorial minimalista incluido en `app/src/main/res/drawable/ic_medly_logo.xml`.

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

## Arquitectura

`data/` contiene la entidad Room, DAO, base de datos, repositorio y la lógica de consumo. `ui/` contiene el `ViewModel` y el tema Material 3. `MainActivity` expone las pantallas `Hoy` e `Inventario` mediante `NavigationBar`.

## Modelo de datos

Cada medicamento guarda nombre, formato, dosis, tomas al día, unidades por caja, cajas cerradas, unidades sueltas, límite mensual, consumo del mes y la fecha del último check-in. El stock nunca baja de cero: si no hay suficientes unidades para una toma o se ha alcanzado el límite mensual, el check-in queda desactivado.

## Limitaciones

- Los datos se almacenan únicamente en el dispositivo y no hay sincronización entre teléfonos.
- El límite mensual se registra sobre el mes en curso, pero el reinicio automático por cambio de mes todavía debe añadirse.
- Las alertas son visuales dentro de la aplicación; no se programa una notificación del sistema.
