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
- Fechas: `fecha_creacion` se conserva y `fecha_modificacion` se actualiza al editar.

## Compilar y ejecutar

Requisitos: JDK 17+, Android SDK con la plataforma 36.

```bash
./gradlew assembleDebug      # APK en app/build/outputs/apk/debug/
./gradlew testDebugUnitTest  # tests (JVM + Robolectric)
./gradlew lintDebug          # análisis estático
```

## Tests

61 tests:

- **data/** (25): DAOs Room (CRUD, cascadas, duplicados, fechas), garantía de que
  producción no consulta en el hilo principal y entrega asíncrona del repositorio.
- **util/** (17): filtro de búsqueda, parseo/orden de fechas y diff de etiquetas.
- **UI/** (18): `MainActivity` (navegación, FAB, atrás, ámbito, long-press) y las
  Activities de edición/visualización.
- Ejemplo de plantilla (1).

## Notas

- `minSdk 21`, `targetSdk 36`.
- Paquete `com.example.notas`; `app_name` "Nevernote".
