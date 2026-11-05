# SIGEM — Sistema Integrado de Gestión Empresarial

Esta versión deja integrados los 8 espacios funcionales previstos:

1. Administración
2. Marketing
3. Ventas / Comercial
4. Operaciones / Producción
5. Logística / Almacén
6. RRHH
7. Gerencia General
8. Portal Cliente

## Arranque rápido sin scripts de datos

### Opción recomendada: PostgreSQL con Docker

```bash
docker compose up -d postgres
```

Luego ejecutar Spring Boot:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080/login`.

La aplicación usa `ddl-auto=update` en desarrollo y `RoleBootstrap` crea automáticamente los datos base. No hay que ejecutar un SQL de INSERT para roles, cuentas, maquinaria o tarifas.

## Configuración

Copiar `.env.example` a las variables de entorno de tu equipo. Los valores por defecto están pensados para el PostgreSQL del `docker-compose.yml`.

Cloudinary es opcional para las funciones de subida de imágenes.

## Seguridad

- Contraseñas BCrypt.
- Roles por área.
- APIs protegidas por sesión o JWT.
- Portal Cliente separado de los paneles administrativos.
- Las cuentas demo se crean solo si no existen.

## Nota de producción

Para producción se recomienda cambiar `JPA_DDL_AUTO` a `validate`, usar migraciones versionadas y configurar secretos mediante variables de entorno/secret manager.


## Portal público

La aplicación incluye una portada pública en `/` con catálogo de maquinaria y tipos de servicio, además de accesos directos a `/login` y `/register`. La maquinaria admite `imageUrl` y los tipos de maquinaria/servicio también pueden guardar una URL de imagen para publicarla en el catálogo. Si no hay foto, se muestra un placeholder local para que el catálogo no quede vacío.


La aplicación incluye una portada pública en `/` con catálogo de maquinaria y tipos de servicio, además de accesos directos a `/login` y `/register`. La maquinaria admite `imageUrl` y los tipos de maquinaria/servicio también pueden guardar una URL de imagen para publicarla en el catálogo. Si no hay foto, se muestra un placeholder local para que el catálogo no quede vacío.

## Escenario demo reproducible

Al levantar una base `getion_sigem` nueva, SIGEM crea primero roles/cuentas base y luego ejecuta `DemoDataSeeder` para poblar un escenario de demostración relacionado. Incluye múltiples clientes con acceso al portal, maquinaria con imágenes externas, solicitudes, cotizaciones, contratos, servicios/asignaciones, empleados, proveedores, repuestos, almacenes, movimientos, inventario, mantenimientos y marketing.

Las cuentas adicionales de clientes se documentan en `docs/ACCESOS_DEMO.md`. El seeder es idempotente y no duplica el escenario cuando ya se encuentran los volúmenes mínimos cargados.
