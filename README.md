# FieldSync

**FieldSync** es una plataforma móvil *offline-first* y motor de sincronización transaccional para operaciones en terreno, desarrollada en Kotlin para Android junto a un backend ligero en Node.js. Diseñada específicamente para resolver los cuellos de botella de distribuidoras, centros logísticos, cuadrillas de mantenimiento y empresas de servicios técnicos, *FieldSync* reemplaza las planillas manuales en papel y las transcripciones propensas a errores por un flujo de trabajo digital resiliente que opera con total normalidad en zonas sin conectividad.

El sistema garantiza persistencia transaccional local mediante SQLite, encolamiento en segundo plano gobernado por `WorkManager` y sincronización bidireccional idempotente en cuanto el dispositivo recupera señal de red, protegiendo la integridad de cada registro antes de consolidarlo en el servidor central.

---

## 📸 Capturas de Pantalla

<table align="center" style="border-collapse: collapse; border: 1px solid #333;">
  <tr>
    <td width="50%" align="center" style="padding: 6px; border: 1px solid #333;">
      <img src="screenshot.png" alt="Screenshot 1" width="100%">
    </td>
    <td width="50%" align="center" style="padding: 6px; border: 1px solid #333;">
      <img src="screenshot2.png" alt="Screenshot 2" width="100%">
    </td>
  </tr>
  <tr>
    <td width="50%" align="center" style="padding: 6px; border: 1px solid #333;">
      <img src="screenshot3.png" alt="Screenshot 3" width="100%">
    </td>
    <td width="50%" align="center" style="padding: 6px; border: 1px solid #333;">
      <img src="screenshot4.png" alt="Screenshot 4" width="100%">
    </td>
  </tr>
</table>

## ✨ Características Principales

* **Arquitectura Offline-First Nativa:** Toda operación (pedido, inventario, auditoría, incidencia) se escribe de manera inmediata y atómica en la base de datos SQLite local del dispositivo. La aplicación jamás bloquea la interfaz de usuario ni falla ante cortes de red.

* **Sincronización Automática con WorkManager:** Encolamiento de tareas periódicas y reactivas sujetas a restricciones de conectividad (`NetworkType.CONNECTED`). Al detectar red, el motor despacha los lotes pendientes de forma transparente en segundo plano.

* **Transaccionalidad e Idempotencia:** Identificadores únicos basados en UUID generados en el cliente evitan duplicados en el servidor central. El motor marca localmente los registros confirmados como sincronizados únicamente tras recibir la validación formal del backend.

* **Forzado Manual de Sincronización:** Botón de despacho inmediato para operadores que finalizan su jornada y requieren transferir datos al instante antes de cerrar turno o cambiar de zona.

* **Auditoría de Registros en Pantalla:** Consola visual incorporada que detalla el identificador, tipo de carga y estado de sincronización (`[PENDING]` vs `[SYNCED]`) en tiempo real.

* **Interoperabilidad Corporativa (REST API):** El servidor central expone puntos de acceso livianos en Node.js y Express con almacenamiento SQL, permitiendo su fácil integración con sistemas ERP, depósitos o paneles de control ya existentes.

* **Eficiencia Energética y de Datos:** El proceso de sincronización procesa únicamente deltas de datos (registros con bandera `is_synced = 0`), minimizando el uso de batería y el consumo de datos móviles en planes limitados de flotas corporativas.

---

## 🛡️ Confiabilidad y Resiliencia Empresarial

* **Cero Pérdida de Datos en Terreno:** La persistencia local sobrevive reinicios imprevistos de la batería, cierres forzados de la aplicación o apagones en el dispositivo de campo.
* **Tolerancia a Conexiones Intermitentes:** Los reintentos automáticos aplican lógica de espera exponencial gestionada por el runtime del sistema operativo.
* **Procesamiento de Lotes Compacto:** Payload serializado en JSON nativo sin cabeceras superfluas para garantizar transferencias rápidas incluso sobre redes 2G/3G inestables.

---

## ⚙️ ¿Qué Hace? (Módulos Disponibles)

1. **Captura de Transacciones en Campo (`MainActivity.kt`):** Formulario ergonómico optimizado para ingreso rápido de pedidos, control de stock o relevamiento de daños con tipificación estructurada.

2. **Capa de Persistencia Local (`DatabaseHelper.kt`):** Motor SQLite que encapsula transacciones seguras (`beginTransaction` / `endTransaction`), serialización a objetos JSON y control de flags de entrega.

3. **Cliente de Integración de Red (`ApiService.kt`):** Módulo de bajo nivel basado en `HttpURLConnection` que despacha solicitudes autenticables, valida códigos de estado HTTP y extrae listas de identificadores procesados.

4. **Orquestador en Segundo Plano (`SyncWorker.kt`):** Tarea asíncrona desacoplada del ciclo de vida de la interfaz de usuario que garantiza el cumplimiento de las políticas de despacho según las condiciones del hardware.

5. **Servidor Receptor Central (`server.js`):** API REST transaccional que recibe lotes, actualiza el estado consolidado mediante sentencias preparadas y expone consultas de inspección.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje Móvil:** Kotlin 2.0.
* **Persistencia Cliente:** SQLite nativo (`SQLiteOpenHelper`) sin overhead de dependencias pesadas.
* **Planificación de Tareas:** Android Jetpack `WorkManager`.
* **Capa de Red Móvil:** HTTP nativo con serialización JSON estándar.
* **Backend:** Node.js, Express y SQLite3.
* **Compatibilidad Android:** Android 8.0 (API 26) hasta Android 14+ (API 34).
* **Entorno de Desarrollo:** Android Studio & Visual Studio Code.

---

## 🚀 Instalación y Uso

1. Descarga el APK firmado desde la sección **Releases** (Lanzamientos) [**aquí**](../../releases).
2. Habilita la opción "Instalar desde fuentes desconocidas" en tu navegador o administrador de archivos y, a continuación, abre el APK para instalarlo.

## 👨‍💻 Autor

Desarrollado por **Yuri Alexander Pagel Krüger**
