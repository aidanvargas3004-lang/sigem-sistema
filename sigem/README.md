# SIGEM — Sistema Integral de Gestión de Flotas y Maquinaria

Aplicación web de **TRANSUR S.A.C.** para gestionar de punta a punta el negocio de alquiler, venta y servicios con maquinaria pesada: clientes y marketing, solicitudes, cotizaciones, contratos, ventas y alquileres, programación y ejecución de servicios, asignación de unidades y operadores, mantenimiento preventivo/correctivo, almacén y compras, personal, ubicación GPS, notificaciones, reportes y auditoría.

| Capa | Tecnología |
|---|---|
| Lenguaje / framework | Java 21, Spring Boot 3.2.5 (Spring MVC, Spring Data JPA, Spring Security, Validation, Scheduling) |
| Vistas | Thymeleaf + Bootstrap 5 / Bootstrap Icons, Leaflet (mapa GPS) |
| Base de datos | PostgreSQL 16 (administrable con pgAdmin 4) |
| Acceso a datos | Repositorios JPA (DAO) y DAO JDBC para reportes y auditoría |
| Seguridad | Sesión con formulario + JWT para la API, BCrypt, roles por área, CSRF |
| Reportes | Apache POI (Excel) y OpenPDF (PDF) |
| Pruebas | JUnit 5, Mockito, AssertJ, Spring MockMvc, spring-security-test |
| IDE sugerido | Visual Studio Code con *Extension Pack for Java* y *Spring Boot Extension Pack* |

## 1. Puesta en marcha

### 1.1 Base de datos (pgAdmin 4)

1. En pgAdmin 4: *Servers → PostgreSQL → Databases → Create → Database…* con nombre `sigem_db`.
2. No hace falta ejecutar scripts: al arrancar, Hibernate crea/actualiza las tablas (`ddl-auto=update`), `RoleBootstrap` crea roles y cuentas y `DemoDataSeeder` / `DemoOperacionesSeeder` cargan un escenario demo completo.

Alternativa con Docker: `docker compose up -d postgres` (usuario y clave `postgres`).

### 1.2 Variables de entorno

Ver `.env.example`. Las principales:

| Variable | Valor por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/sigem_db` | Conexión JDBC |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `root112233` | Credenciales de PostgreSQL (cámbielas por las de su pgAdmin) |
| `JWT_SECRET` | clave de desarrollo | **Obligatorio cambiarla en producción** (32+ caracteres). La app avisa en el log si es débil |
| `CLOUDINARY_*` | vacío | Opcional, para subir imágenes |
| `PAGOS_*` | datos de ejemplo | Cuenta, CCI, Yape y Plin que el cliente ve en *Mis pagos* (reemplácelos por los reales) |

### 1.3 Ejecutar

```bash
# Linux / macOS
DB_PASSWORD=su_clave ./mvnw spring-boot:run
# Windows (PowerShell)
$env:DB_PASSWORD="su_clave"; .\mvnw.cmd spring-boot:run
```

En VS Code también puede usar *Spring Boot Dashboard → sigem → Run*. Abra `http://localhost:8080`.

### 1.4 Cuentas demo

| Rol | Usuario | Clave | Panel |
|---|---|---|---|
| Administrador | `admin` | `Admin123*` | `/dashboard`, usuarios, roles, auditoría |
| Gerencia | `gerente` | `Gerente123*` | `/gerencia`, reportes operativos |
| Ventas | `ventas` | `Ventas123*` | `/ventas` |
| Marketing | `marketing` | `Marketing123*` | `/marketing/dashboard` |
| Operaciones / Producción | `operaciones` | `Operaciones123*` | `/operaciones` |
| Logística / Almacén | `logistica` | `Logistica123*` | `/logistica` |
| RRHH | `rrhh` | `Rrhh123*` | `/rrhh` |
| Cliente | `cliente` | `Cliente123*` | `/cliente` (portal) |

Más cuentas de clientes en `docs/ACCESOS_DEMO.md`.

### 1.5 Pruebas

```bash
./mvnw test                                   # 45 pruebas unitarias (no necesitan BD)
DB_URL=jdbc:postgresql://localhost:5432/sigem_db DB_PASSWORD=su_clave ./mvnw test   # + 18 de integración (MockMvc + PostgreSQL)
```

## 2. Módulos y requerimientos

| Área | Rutas | Funcionalidad principal |
|---|---|---|
| Administración | `/dashboard`, `/seguridad/*` | Usuarios, roles, **bitácora de auditoría** con filtros y exportación (RF018), **respaldos y configuración de la BD** (`/seguridad/respaldos`) |
| Marketing | `/marketing/*` | Clientes, campañas, leads, oportunidades, reporte Excel/PDF |
| Ventas / Comercial | `/ventas/*` | Solicitudes, cotizaciones, contratos, ventas, alquileres, reporte de ventas |
| Pagos y cobranzas | `/ventas/pagos`, `/cliente/pagos` | **Registro de pagos** en efectivo, transferencia, depósito, Yape, Plin o tarjeta (POS); el cliente reporta sus pagos y Ventas los valida; saldo y estado de pago de cada contrato; recibo PDF; reporte Excel/PDF |
| Operaciones / Producción | `/operaciones/*` | Servicios, programación, maquinaria y tarifas, operadores, **asignación con validación de conflictos** (RF009), **ejecución con horómetro/km** (RF010), checklist e incidencias, **mantenimiento y planes preventivos** (RF012-RF013), **GPS** |
| Reportes operativos | `/reportes` | Servicios realizados, uso de flota, mantenimientos, incidencias, disponibilidad; Excel y PDF (RF016-RF017) |
| Logística / Almacén | `/logistica/*` | Almacenes, repuestos, stock, kardex de movimientos, órdenes de compra, recepción, inventario de maquinaria |
| RRHH | `/rrhh/*` | Empleados, documentación y licencias, vencimientos, historial de alertas |
| Gerencia | `/gerencia/*` | Indicadores, rentabilidad, operaciones, reportes ejecutivos |
| Portal cliente | `/cliente/*` | Solicitudes, cotizaciones, contratos, **pagos y recibos**, servicios del cliente |
| Transversal | `/notificaciones`, campana en todas las vistas | **Alertas automáticas** por rol (RF014) |
| API REST | `/auth/*`, `/api/*` | JWT; DTOs validados; errores JSON uniformes |

## 3. Estructura

```
src/main/java/com/transur/sigem
├── common        excepciones de negocio, manejador global de la API, exportadores de reportes, utilidades
├── security      autenticación (formulario + JWT), usuarios, roles, permisos
├── seguridad     vistas de administración de usuarios y roles
├── sistema       notificaciones, alertas automáticas, auditoría (listener + DAO JDBC), interceptor de vistas
├── cliente       clientes y portal del cliente
├── marketing     campañas, leads, oportunidades, reportes
├── comercial     solicitudes, cotizaciones, contratos, ventas, alquileres
├── pagos         pagos de contratos, validación, recibos PDF y reporte de cobranzas
├── maquinaria    tipos, unidades, tarifas
├── operaciones   servicios, asignaciones, ejecución, incidencias, GPS, reportes operativos (DAO JDBC)
├── mantenimiento órdenes de mantenimiento, repuestos usados, planes preventivos
├── logistica     almacenes, repuestos, movimientos, inventario
├── compras       proveedores, órdenes de compra, recepciones
├── activos       adquisiciones de maquinaria
├── rrhh          empleados y documentos
├── gerencia      tableros gerenciales
└── config        carga de datos base y demo
```

Cada módulo sigue la misma organización: `entity` (modelo JPA), `repository` (DAO), `dto` (Request/Response), `service` (reglas de negocio), `controller` (vistas MVC y API REST) y `templates/<área>` (vistas Thymeleaf).

## 4. Documentación técnica

- [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) — MVC, DTO, DAO, SOLID y otros patrones, por área.
- [docs/INVENTARIO_DOCUMENTACION.md](docs/INVENTARIO_DOCUMENTACION.md) — documentos que gestiona y emite cada área.
- [docs/SEGURIDAD.md](docs/SEGURIDAD.md) — controles de seguridad implementados y consideraciones por área.
- [docs/evidencias/](docs/evidencias/) — capturas de cada pantalla por rol, tomadas con la aplicación en ejecución sobre PostgreSQL.

## 5. Importación y exportación de ficheros

| Tipo | Dónde | Formato |
|---|---|---|
| Importar repuestos | Logística → Repuestos → *Importar repuestos desde Excel* (plantilla descargable) | .xlsx |
| Exportar reportes | `/reportes`, `/ventas/reportes`, `/ventas/pagos`, `/marketing/reportes`, `/seguridad/auditoria` | .xlsx / .pdf |
| Recibo de pago | Ventas → Pagos, Portal cliente → Mis pagos | .pdf |
| Respaldo de BD | Administración → Respaldos y BD | .sql |
| Subir imágenes / documentos | Maquinaria, incidencias, documentos de RRHH (Cloudinary) | imagen / PDF |

## 5.1 Respaldos de la base de datos

- **Automático:** cada día a las 02:00 (`BACKUP_CRON`) se genera `backups/sigem_db_AAAAMMDD_HHMMSS.sql`; se conservan los últimos 15 (`BACKUP_RETENER`).
- **A pedido:** Administración → *Respaldos y BD* → *Generar respaldo ahora*; desde ahí también se descarga cada archivo.
- **Herramienta:** usa `pg_dump` (lo busca en el PATH y en `C:\Program Files\PostgreSQL\<versión>\bin`, o en `PG_DUMP`); si no lo encuentra, exporta los datos por JDBC.
- **Por consola (Windows):** `.\scripts\respaldar.ps1` y `.\scripts\restaurar.ps1 -Archivo backups\sigem_db_....sql`.
- Cada respaldo queda registrado en la bitácora de auditoría (acción `RESPALDO`).

## 5.2 Pagos

SIGEM **no es una pasarela de pagos**: el dinero no pasa por el sistema. El cliente paga por los medios habituales y SIGEM registra cómo pagó y controla el saldo de cada contrato.

| Medio | ¿Quién lo registra? | N° de operación |
|---|---|---|
| Efectivo, tarjeta (POS) | Ventas, en oficina | Efectivo: no; tarjeta: N° de voucher |
| Transferencia, depósito, Yape, Plin | Ventas, o el cliente desde *Mis pagos* | Obligatorio y único |

1. **Ventas registra** un pago (Ventas → *Pagos y cobranzas* → *Registrar pago*, o desde el detalle del contrato): queda **CONFIRMADO** y se emite el recibo `REC-AAAA-NNNNN`.
2. **El cliente reporta** un pago remoto en *Mis pagos*: queda **POR VALIDAR** y Ventas recibe una notificación.
3. **Ventas valida**: lo **confirma** (se emite el recibo y se notifica al cliente) o lo **rechaza** con un motivo que el cliente ve.
4. **ADMIN** puede **anular** un recibo confirmado por error (con motivo); el monto vuelve al saldo.

El monto pagado nunca supera el del contrato, y cada contrato muestra su estado: *PENDIENTE*, *PARCIAL* o *PAGADO*.

## 6. Producción

- Cambiar `JPA_DDL_AUTO=validate` y versionar el esquema (Flyway/Liquibase).
- Definir `JWT_SECRET`, `DB_PASSWORD` y las credenciales de Cloudinary como secretos del servidor, nunca en el repositorio.
- Servir detrás de HTTPS (proxy inverso) para que las cookies de sesión viajen cifradas.
- Cambiar o desactivar las cuentas demo.
