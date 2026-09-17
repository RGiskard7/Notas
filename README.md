# Nevernote

Aplicación Android de notas personales: notas organizadas en libretas y etiquetadas,
con búsqueda y ordenación. Proyecto original de 2020, migrado y modernizado.

## Funcionalidad

- **Notas**: crear, ver, editar y eliminar. Título, contenido, fecha de creación y de
  modificación.
- **Libretas**: agrupan notas. Existe una libreta `Default` protegida; al eliminar una
  libreta sus notas se mueven a `Default`.
- **Etiquetas**: relación N:M con las notas.
- **Búsqueda** por título (respeta el ámbito: todas / libreta / etiqueta) y **ordenación**
  por fecha, título o número de notas.
- Editor con inserción de viñetas.

## Arquitectura

```
com.example.notas
├── MainActivity            Drawer + fragments (notas / libretas / etiquetas)
├── EditNotaActivity        Crear y editar notas
├── ViewNotaActivity        Ver, editar y eliminar una nota
├── EditLibretaActivity     Crear y editar libretas
├── UI/
│   ├── List*Fragment       Listados (RecyclerView)
│   ├── *Adapter            Adapters con ViewHolder
│   └── *ViewModel          ViewModel + LiveData por pantalla
├── util/                   Lógica pura testeable (filtro, fechas, selección)
└── data/
    ├── NotasRepository     Acceso asíncrono a datos (executor + main handler)
    ├── I*DAO / FactoryDAO  Contratos y factoría
    ├── Nota / Libreta / Etiqueta  Modelo de dominio
    └── room/               Room: entidades, DAOs, base de datos y mappers
```

- Persistencia con **Room** (claves foráneas con `ON DELETE CASCADE` activadas).
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

76 tests:

- **data/** (30): DAOs Room (CRUD, cascadas, duplicados, recuentos, fechas), garantía de
  que producción no consulta en el hilo principal y entrega asíncrona del repositorio.
- **util/** (24): filtro de búsqueda, formato de fechas, diff de etiquetas y viñetas.
- **UI/** (21): `MainActivity` (navegación, FAB, atrás, ámbito, long-press), las
  Activities de edición/visualización y la persistencia de la búsqueda.
- Ejemplo de plantilla (1).

## Notas

- `minSdk 21`, `targetSdk 36`; `versionCode 2`, `versionName 2.0`.
- Paquete `com.example.notas`; `app_name` "Nevernote".
- Migración de Room 1 -> 2 (fechas de texto a epoch) con test de migración.
