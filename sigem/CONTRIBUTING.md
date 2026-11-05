# Guía de colaboración

## Ramas del repositorio

- `SigemV1.1` y `SigemV1.2`: primeras versiones del proyecto.
- `SigemVs2.1`: versión 2 (rama por defecto).
- `SigemVs3`: versión 3, con el módulo de pagos y cobranzas, la importación desde Excel y los respaldos.
- `aidanvargas`: rama de trabajo de Abel Valerio Mamani Vargas.
- Cada integrante trabaja en su propia rama y luego integra sus cambios en la rama de la versión.

El proyecto Spring Boot está en la carpeta `sigem/` (allí está el `pom.xml`).

## Convención de commits

Usar mensajes como `feat: agrega ...`, `fix: corrige ...`, `docs: actualiza ...`, `test: agrega ...` y `refactor: ...`, con commits pequeños y descriptivos.

## Antes de publicar

- No subir `.env`, contraseñas reales, tokens, llaves privadas ni datos personales.
- Mantener los secretos en variables de entorno.
- Ejecutar las pruebas (`mvnw.cmd test` o `./mvnw test`) y verificar la configuración de PostgreSQL.
