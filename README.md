# GD Progress Tracker

Web app para seguir el progreso de diferentes niveles Demon en Geometry Dash de **GdLali**, **Bimba666** y **Lapso03** en las listas
Classic (Demonlist) y Platformer, con puntuación calculada con la siguiente fórmula:

```
puntos(nivel) = 364.28 * e^(-0.04 * posición)
```

La puntuación de cada usuario es la suma de puntos de los niveles que tiene al 100%
(en Platformer, completado = 100%, no completado = 0%).

## Stack

Spring Boot 3.3.4 + Java 17 + Thymeleaf + Spring Data JPA + H2 (por defecto) / PostgreSQL.

## Arrancar en local

Requiere Java 17 y Maven instalados:

```bash
mvn spring-boot:run
```

La primera vez que arranca, carga automáticamente los 89 niveles de Classic y los 24
de Platformer junto con el progreso original de los tres en el momento que lo hice, se pueden actualizar los progresos más tarde.
Se abre en **http://localhost:8085**.

Los datos se guardan en `./data/gdtracker.mv.db` (fichero H2 local, ignorado por Git).

## Sincronización con AREDL / GDDL

El servicio `LevelSyncService` actualiza cada noche (04:00) la posición AREDL y la
dificultad/tier de GDDL de cada nivel de Classic.

**Añade las API keys. Antes de arrancar:**

```bash
export AREDL_API_KEY="tu_clave_aredl"
export GDDL_API_KEY="tu_clave_gddl"
```

Si no las defines, la app funciona igual pero sin sincronización dinámica.

> Nota: tu token de AREDL es un JWT con fecha de caducidad (`exp`). Cuando caduque
> tendrás que generar uno nuevo y actualizar la variable de entorno.

## Cómo editar el progreso

- Arriba a la derecha eliges quién eres.
- Solo puedes editar tu propia columna; las del resto se ven en modo lectura.
- En Classic el progreso es un porcentaje (0-100%); en Platformer es un botón de
  completado/no completado.