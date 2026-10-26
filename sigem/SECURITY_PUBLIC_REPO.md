# Nota de seguridad para publicación

Este repositorio es público. Antes de desplegar SIGEM fuera de un entorno local:

1. Configurar credenciales de PostgreSQL mediante `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.
2. Definir un `JWT_SECRET` largo, aleatorio y privado mediante variable de entorno. No reutilizar valores de ejemplo.
3. Revisar las cuentas de demostración documentadas y los inicializadores de datos; son únicamente para desarrollo/pruebas y no deben usarse en producción.
4. No subir archivos `.env` ni credenciales reales.
5. Revisar `docker-compose.yml` y cambiar cualquier contraseña predeterminada antes de exponer servicios.

Esta nota no sustituye una auditoría de seguridad del código.
