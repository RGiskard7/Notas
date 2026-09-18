# Nevernote

Aplicación Android de notas personales: notas organizadas en libretas y etiquetadas,
con búsqueda y ordenación. Proyecto original de 2020, migrado y modernizado.

## Funcionalidad

- **Notas**: crear, ver, editar y eliminar. Título, contenido, fecha de creación y de
  modificación.
- **Libretas**: agrupan notas. Existe una libreta `Default` protegida; al eliminar una
  libreta sus notas se mueven a `Default`.
- **Etiquetas**: relación N:M con las notas.
- **Búsqueda** por título y contenido (índice FTS4), respetando el ámbito
  (todas / libreta / etiqueta), y **ordenación** por fecha, título o número de notas.
- Editor con el patrón de las apps de notas actuales: título y cuerpo sin cajas, la
  libreta/etiquetas como *chips* y una barra de formato con negrita, cursiva, tachado,
  código, encabezado, cita, viñetas, lista numerada, casilla, enlace y separador; en la
  vista, las tareas se pueden marcar directamente.
- **Libretas y etiquetas** con la misma experiencia: listados con icono y un diálogo
  Material común para crearlas o renombrarlas (avisa si el título ya existe).
- **Exportar e importar** notas en **Markdown** (ficheros `.md`).
- **Papelera**: las notas se borran de forma lógica, se pueden **restaurar** (o deshacer
  al momento) y borrar definitivamente.
- **Adjuntos**: se pueden añadir **imágenes** a una nota; se copian al almacenamiento
  interno y se ven en la pantalla de la nota.
- **Recordatorios**: aviso por fecha y hora con notificación; se reprograman al
  reiniciar el dispositivo.
- **Bloqueo con PIN**: pantalla de bloqueo al abrir la app (el PIN se guarda con sal y
  PBKDF2). Es un bloqueo de acceso, **no** cifra la base de datos.
- **Interfaz**: tema Material con modo claro/oscuro (elegible en **Ajustes**), paleta
  propia e iconos vectoriales consistentes.
- **Ajustes y Acerca de**: en el menú lateral, selector de tema (sistema/claro/oscuro) y
  pantalla con la versión y la autoría (Eduardo Díaz Sánchez).

## Arquitectura

```
com.example.notas
├── MainActivity            Drawer + fragments (notas / libretas / etiquetas)
├── EditNotaActivity        Crear y editar notas
├── ViewNotaActivity        Ver, editar y eliminar una nota
├── UI/
│   ├── List*Fragment       Listados (RecyclerView)
│   ├── *Adapter            Adapters con ViewHolder
│   ├── *ViewModel          ViewModel + LiveData por pantalla
│   └── DialogoNombre       Diálogo de nombre (crear y renombrar libretas/etiquetas)
├── util/                   Lógica pura testeable (filtro, fechas, selección)
└── data/
    ├── NotasRepository     Acceso asíncrono a datos (executor + main handler)
    ├── I*DAO / FactoryDAO  Contratos y factoría
    ├── Nota / Libreta / Etiqueta  Modelo de dominio
    └── room/               Room: entidades, DAOs, base de datos y mappers
```

- Persistencia con **Room** (claves foráneas con `ON DELETE CASCADE` activadas) y un
  índice **FTS4** para buscar en el contenido de las notas.
- Lecturas/escrituras **fuera del hilo principal** vía `NotasRepository`; la UI observa
  `LiveData`. La base de datos **no** permite consultas en el hilo principal.
- Las fechas se guardan como **epoch** (milisegundos) y se formatean al mostrarlas.
  `fecha_creacion` se conserva y `fecha_modificacion` se actualiza al editar.
- Vistas con **ViewBinding**; la búsqueda se conserva con `SavedStateHandle`.

## Compilar y ejecutar

Requisitos: JDK 17+, Android SDK con la plataforma 36.

```bash
./gradlew assembleDebug      # APK en app/build/outputs/apk/debug/
./gradlew testDebugUnitTest  # tests (JVM + Robolectric)
./gradlew lintDebug          # análisis estático
```

## Tests

162 tests:

- **data/** (52): DAOs Room (CRUD, papelera, adjuntos, recordatorios, cascadas,
  duplicados, recuentos, fechas, búsqueda FTS), migraciones 1->2 a 5->6, garantía de
  que producción no consulta en el hilo principal, entrega asíncrona del repositorio y
  creación/renombrado de libretas sin títulos repetidos.
- **util/** (54): filtro por título, consulta FTS, Markdown, formato de nota (tareas,
  negrita/cursiva/tachado/código, encabezados, citas), adjuntos, fechas, diff de
  etiquetas y viñetas.
- **recordatorios/** (1): programación y cancelación de la alarma.
- **seguridad/** (8): PIN (guardar, comprobar, cambiar, quitar) y pantalla de bloqueo.
- **ajustes/** (6): selector de tema y pantalla "Acerca de".
- **UI/** (40): `MainActivity` (navegación, FAB, atrás, ámbito, papelera, estado vacío),
  renderizado de notas, vista previa con formato, barra de formato del editor, diálogo
  de nombre, Activities de edición/visualización y persistencia de la búsqueda.
- Ejemplo de plantilla (1).

## Notas

- `minSdk 21`, `targetSdk 36`; `versionCode 2`, `versionName 2.0`.
- Paquete `com.example.notas`; `app_name` "Nevernote".
- Migraciones de Room 1->2 (fechas a epoch), 2->3 (índice FTS), 3->4 (papelera),
  4->5 (adjuntos) y 5->6 (recordatorios), con tests.
- El bloqueo con PIN no cifra los datos: la base de datos sigue sin cifrar.
