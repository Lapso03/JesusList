# GD Progress Tracker

Web app para seguir el progreso de **GdLali**, **Bimba666** y **Lapso** en las listas
Classic (Demonlist) y Platformer, con puntuación calculada con la misma fórmula del Excel:

```
puntos(nivel) = 364.28 * e^(-0.04 * posición)
```

La puntuación de cada usuario es la suma de puntos de los niveles que tiene al 100%
(en Platformer, completado = 100%, no completado = 0%).

## Stack

Spring Boot 3.3.4 + Java 17 + Thymeleaf + Spring Data JPA + H2 (por defecto) / MySQL (opcional).

## Arrancar en local

Requiere Java 17 y Maven instalados (los mismos que usaste para EventApp):

```bash
mvn spring-boot:run
```

La primera vez que arranca, carga automáticamente los 89 niveles de Classic y los 24
de Platformer junto con el progreso original de los tres, extraídos del Excel.
Se abre en **http://localhost:8085**.

Los datos se guardan en `./data/gdtracker.mv.db` (fichero H2 local, ignorado por Git).

## Usar MySQL en vez de H2 (como en EventApp)

```bash
docker run -d --name gdtracker-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=gdtracker mysql:8
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Sincronización con AREDL / GDDL

El servicio `LevelSyncService` actualiza cada noche (04:00) la posición AREDL y la
dificultad/tier de GDDL de cada nivel de Classic, usando las mismas llamadas que
tenías en el Apps Script.

**Las API keys nunca van en el código.** Antes de arrancar:

```bash
export AREDL_API_KEY="tu_clave_aredl"
export GDDL_API_KEY="tu_clave_gddl"
```

Si no las defines, la app funciona igual pero sin sincronización dinámica (se
queda con la dificultad estática importada del Excel).

> Nota: tu token de AREDL es un JWT con fecha de caducidad (`exp`). Cuando caduque
> tendrás que generar uno nuevo y actualizar la variable de entorno.

## Cómo se editó el progreso

- Arriba a la derecha eliges quién eres (sin contraseña, es solo para vosotros tres).
- Solo puedes editar tu propia columna; las de tus amigos se ven en modo lectura.
- En Classic el progreso es un porcentaje (0-100%); en Platformer es un botón de
  completado/no completado.

## Subir a GitHub

```bash
git init
git add .
git commit -m "Proyecto inicial: entidades, progreso, ranking y sync AREDL/GDDL"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/gd-progress-tracker.git
git push -u origin main
```

El `.gitignore` ya excluye el fichero de base de datos local y cualquier
`application-local.yml` con claves reales, así que puedes subir el repo sin miedo.
