# Arquitectura y patrones de diseño — SIGEM

Este documento responde al *Taller de Arquitectura: MVC, TDD, DAO, SOLID*: describe cómo se aplican los patrones en el código real del proyecto (`com.transur.sigem`) y en cada área de la empresa.

## 1. Vista general

```
Navegador (Thymeleaf)          App / dispositivo GPS (JSON + JWT)
        │  HTTP + sesión + CSRF          │  HTTP + Bearer token
        ▼                                ▼
┌──────────────────────── Spring Security (SecurityConfig, JwtAuthFilter) ────────────────────────┐
│  Controller (MVC: *ViewController → vista HTML)      Controller REST (*Controller → JSON)          │
│        │  recibe DTO Request / devuelve Model                 │ @Valid DTO → DTO Response          │
│        ▼                                                      ▼                                    │
│  Service (reglas de negocio, @Transactional)  ←  componentes SRP: ValidadorDisponibilidad,        │
│        │                                         AlertaService, ExportadorReporte (Strategy)      │
│        ▼                                                                                          │
│  DAO: Repository JPA (CRUD, consultas derivadas)   DAO JDBC (reportes, bitácora de auditoría)     │
└────────┼───────────────────────────────────────────────────┼──────────────────────────────────────┘
         ▼                                                   ▼
                         PostgreSQL 16 (sigem_db)
```

Transversales: `NotificacionesInterceptor` (contador de alertas en cada vista), `AuditoriaEntidadesListener` (eventos de Hibernate → bitácora), `ApiExceptionHandler` (errores JSON uniformes), tareas programadas (`@Scheduled`).

## 2. Patrones aplicados

### 2.1 MVC (Modelo – Vista – Controlador)

| Elemento | Implementación | Ejemplo |
|---|---|---|
| Modelo | Entidades JPA en `*/entity` | `Servicio`, `Maquinaria`, `PlanMantenimiento` |
| Vista | Plantillas Thymeleaf en `templates/<área>` con fragmentos comunes (`_nav`, `_styles`, `fragments/campana`) | `operaciones/ejecucion.html` |
| Controlador | `@Controller` (vistas) y `@RestController` (API) | `EjecucionServicioViewController`, `GpsApiController` |

El controlador no contiene reglas de negocio: valida la entrada, llama al servicio y elige la vista. Ejemplo: `EjecucionServicioViewController.finalizar()` delega en `ServicioService.finalizar()`, que calcula horas y km de uso y libera la unidad.

### 2.2 DTO (Data Transfer Object)

- **Request**: datos de entrada con Bean Validation (`@NotNull`, `@Pattern`, `@DecimalMin`…). Ej.: `ServicioRequest`, `UbicacionGpsRequest`, `IncidenciaRequest`.
- **Response**: datos de salida sin exponer la entidad ni sus relaciones perezosas. Ej.: `ServicioResponse`, `UbicacionGpsResponse`, `IncidenciaResponse`, `NotificacionResponse`.
- **DTO de reporte**: `ReporteVentasDTO`, `ReporteMarketingDTO`, `TablaReporte` (modelo neutro que comparten la vista y los exportadores), `RegistroAuditoria` / `FiltroAuditoria`.

Beneficio de seguridad: la API de incidencias recibía antes la entidad completa (`@RequestBody Incidencia`), lo que permitía asignar campos que el cliente no debía tocar (estado, fecha). Ahora recibe `IncidenciaRequest` y el servidor fija esos valores.

### 2.3 DAO (Data Access Object)

| Tipo | Dónde | Cuándo se usa |
|---|---|---|
| DAO JPA (Spring Data) | 40 interfaces `*Repository` | CRUD y consultas por convención: `MantenimientoRepository.findByMaquinariaIdAndEstadoInAndFechaProgramadaBetween(...)`, `UbicacionGpsRepository.ultimasPosiciones()` (`@Query`) |
| DAO JDBC (interfaz + implementación) | `operaciones/dao/ReporteOperativoDao` → `ReporteOperativoJdbcDao` | Reportes de agregación que cruzan 5–6 tablas (servicios, asignaciones, mantenimientos, incidencias) con SQL nativo y `NamedParameterJdbcTemplate` |
| DAO JDBC | `sistema/dao/AuditoriaDao` → `AuditoriaJdbcDao` | Escritura de la bitácora desde los eventos de Hibernate (no se puede volver a usar el `EntityManager` durante el *flush*) y búsqueda con filtros dinámicos |

Los servicios dependen de la **interfaz** del DAO, no de la clase JDBC (inversión de dependencias); en las pruebas se reemplaza por un *mock*.

### 2.4 Principios SOLID

| Principio | Aplicación en SIGEM |
|---|---|
| **S** — Responsabilidad única | `ValidadorDisponibilidad` solo valida conflictos de unidad/operador; `AsignacionService` solo registra. `AlertaService` decide qué alertar; `NotificacionService` solo guarda y entrega. Los exportadores solo convierten `TablaReporte` a un formato. |
| **O** — Abierto/cerrado | Nuevo formato de exportación (p. ej. CSV) = nueva clase que implemente `ExportadorReporte`; `ExportadorReporteFactory` la descubre sola, sin modificar controladores. Nueva alerta = nuevo método privado en `AlertaService`. |
| **L** — Sustitución de Liskov | Cualquier `ExportadorReporte` (Excel o PDF) se usa de la misma manera en `/reportes` y `/seguridad/auditoria`. `ReporteOperativoJdbcDao` puede sustituirse por un *mock* de `ReporteOperativoDao` sin cambiar el servicio. |
| **I** — Segregación de interfaces | Interfaces pequeñas y específicas: `AuditoriaDao` (4 métodos), `ReporteOperativoDao`, `ExportadorReporte`; los repositorios exponen solo las consultas que cada módulo necesita. |
| **D** — Inversión de dependencias | Inyección por constructor (`@RequiredArgsConstructor`) de interfaces: `ReporteOperativoService` → `ReporteOperativoDao`; `ExportadorReporteFactory` → `List<ExportadorReporte>`; `AuditoriaEntidadesListener` → `AuditoriaDao`. |

### 2.5 Otros patrones

| Patrón | Clase |
|---|---|
| Strategy | `ExportadorReporte` con `ExcelExportador` y `PdfExportador` |
| Factory | `ExportadorReporteFactory` (elige la estrategia por formato) |
| Observer / Listener | `AuditoriaEntidadesListener` (eventos post-insert/update/delete de Hibernate), `AuditoriaAccesoListener` (eventos de login de Spring Security), `AlertaService.alIniciar()` (`ApplicationReadyEvent`) |
| Interceptor | `NotificacionesInterceptor` agrega el contador de alertas a todas las vistas |
| Front Controller / Filter Chain | `DispatcherServlet` + cadena de filtros de Spring Security (`JwtAuthFilter`) |
| Builder | DTOs con `@Builder` de Lombok (`ServicioResponse.builder()`) |
| Template Method | `OncePerRequestFilter.doFilterInternal` en `JwtAuthFilter` |
| Manejador global de excepciones | `ApiExceptionHandler` (`@RestControllerAdvice`): `NegocioException` → 422, `RecursoNoEncontradoException` → 404, validación → 400 |
| Inyección de dependencias / IoC | Contenedor de Spring en todo el proyecto |

## 3. Aplicación por área

| Área | Controladores (C) | Vistas (V) | Servicios / reglas | DAO | DTO | Patrones destacados |
|---|---|---|---|---|---|---|
| **Marketing** | `MarketingViewController`, `CampaniaController`, `LeadController`, `OportunidadController`, `ReporteController` | `marketing/*.html` | `CampaniaService`, `LeadService`, `OportunidadService`, `ReporteService` | `CampaniaRepository`, `LeadRepository`, `OportunidadRepository`, `ClienteRepository` | `CampaniaRequest`, `LeadRequest`, `OportunidadRequest/Response`, `ReporteMarketingDTO` | MVC, DTO, SRP por entidad del embudo |
| **Ventas / Comercial** | `SolicitudViewController`, `CotizacionViewController`, `VentasExtraViewController`, REST de solicitudes, cotizaciones, contratos, ventas y alquileres | `ventas/*.html`, `cliente/*.html` | `SolicitudService` → `CotizacionService` → `ContratoService` → `VentaService` / `AlquilerService`; `ReporteVentasService`, `ReporteExportService` | Repositorios de cada documento comercial | `*Request` / `*Response`, `ReporteVentasDTO` | MVC, DTO, `@PreAuthorize` por rol |
| **Pagos y cobranzas** | `PagoViewController` (`/ventas/pagos`), `ClientePagoController` (`/cliente/pagos`) | `ventas/pagos.html`, `ventas/pago-form.html`, `cliente/pagos.html`, sección de pagos en `ventas/contrato-detalle.html` | `PagoService` (saldo, N° de operación obligatorio y único, ciclo por validar → confirmado / rechazado → anulado), `ReciboPagoPdf` | `PagoRepository` (sumas agregadas por contrato y estado) | `PagoRequest` (Bean Validation), `ResumenPago`, `FiltroPago`; enums `MetodoPago`, `EstadoPago` | Enum con comportamiento (cada medio sabe si exige N° de operación y si es remoto), reutiliza `ExportadorReporteFactory` y `NotificacionService`, `@PreAuthorize` para anular |
| **Producción / Operaciones** | `OperacionesViewController`, `EjecucionServicioViewController`, `GpsViewController`, `ReporteOperativoViewController`, REST de servicios, asignaciones, incidencias y GPS | `operaciones/*.html` | `ServicioService` (inicio/cierre), `AsignacionService` + `ValidadorDisponibilidad`, `UbicacionGpsService`, `ReporteOperativoService` | `ServicioRepository`, `AsignacionRepository`, `UbicacionGpsRepository`, **`ReporteOperativoJdbcDao`** | `ServicioRequest`, `UbicacionGpsRequest/Response`, `IncidenciaRequest/Response`, `TablaReporte` | SRP, DIP, DAO JDBC, Strategy |
| **Mantenimiento** | `MantenimientoViewController`, `MantenimientoController` | `operaciones/mantenimiento*.html`, `planes.html` | `MantenimientoService` (ciclo programado → en proceso → completado, consumo de repuestos, planes preventivos) | `MantenimientoRepository`, `PlanMantenimientoRepository`, `MantenimientoRepuestoRepository` | Formularios con parámetros validados | Reutiliza `MovimientoInventarioService` (no duplica la lógica de stock) |
| **Logística / Almacén y Compras** | `LogisticaViewController`, REST de almacenes, repuestos, movimientos, órdenes de compra, recepciones | `logistica/*.html` | `MovimientoInventarioService` (entradas/salidas con validación de stock), `RepuestoService`, `OrdenCompraService`, `RecepcionService` | Repositorios de inventario y compras | `MovimientoRequest`, `OrdenCompraRequest`, `RecepcionRequest`… | MVC, DTO, excepciones de negocio |
| **RRHH** | `RrhhViewController`, `EmpleadoController`, `DocumentoController` | `rrhh/*.html` | `EmpleadoService`, `DocumentoService`; las licencias alimentan `ValidadorDisponibilidad` y `AlertaService` | `EmpleadoRepository`, `DocumentoEmpleadoRepository` | `EmpleadoRequest/Response`, `DocumentoRequest/Response` | DTO, reutilización entre módulos |
| **Gerencia** | `GerenciaViewController` | `gerencia/*.html` | Indicadores calculados con los repositorios de todas las áreas; `/reportes` para el detalle operativo | Repositorios de cada área + `ReporteOperativoDao` | Mapas de indicadores | MVC. *Mejora pendiente*: extraer un `IndicadoresService` para que el controlador no consulte repositorios directamente. |
| **Administración / Seguridad** | `SeguridadController`, `AuditoriaViewController`, `NotificacionViewController`, `AuthController` | `seguridad/*.html`, `sistema/notificaciones.html` | `AuthService`, `UserDetailsServiceImpl`, `AlertaService`, `NotificacionService` | `UsuarioRepository`, `RolRepository`, **`AuditoriaJdbcDao`** | `LoginRequest`, `AuthResponse`, `RegistroAuditoria`, `FiltroAuditoria` | Observer (auditoría), Interceptor, Filter Chain |

## 4. TDD — pruebas automatizadas

| Prueba | Tipo | Qué verifica |
|---|---|---|
| `ValidadorDisponibilidadTest` (7) | Unitaria (Mockito) | Doble asignación con cruce de horario, servicios consecutivos permitidos, unidad en mantenimiento o vendida, mantenimiento programado en el rango, licencia vencida, cargo no habilitado |
| `ServicioServiceEjecucionTest` (5) | Unitaria | Inicio y cierre calculan horas/km y liberan la unidad; lecturas menores rechazadas; no inicia con unidad en mantenimiento |
| `PlanMantenimientoTest` (4) | Unitaria | Situación OK / PRÓXIMO (90 %) / VENCIDO (100 %) por horómetro, km y fecha |
| `ReporteOperativoServiceTest` (3) | Unitaria (DAO simulado) | Armado de la tabla, formato de fechas y decimales, validación del período |
| `ExportadoresTest` (4) | Unitaria | Excel (`PK`) y PDF (`%PDF`) válidos, nombre de archivo, formato no soportado |
| `NotificacionServiceTest` (3) | Unitaria | Alertas sin duplicados; un usuario no marca notificaciones ajenas |
| `AuditoriaEntidadesListenerTest` (3) | Unitaria | Módulo deducido del paquete, relaciones guardadas por id, recorte de textos |
| `RepuestoImportServiceTest` (3) | Unitaria | Importación de repuestos desde Excel: filas válidas, errores por fila, movimiento de entrada |
| `RespaldoServiceTest` (2) | Unitaria | Respaldo por JDBC y retención de archivos |
| `PagoServiceTest` (11) | Unitaria (Mockito) | Monto no supera el saldo, N° de operación obligatorio y no repetido, contrato cancelado sin pagos, el cliente solo reporta sus contratos y medios remotos, confirmación con recibo, rechazo/anulación con motivo, cálculo del estado de pago |
| `PagosIntegracionTest` (6) | Integración (MockMvc + PostgreSQL) | Listado y exportación para Ventas, solo ADMIN anula, CSRF obligatorio, validación del formulario, el cliente no entra a `/ventas/pagos` |
| `SeguridadIntegracionTest` (11) | Integración (MockMvc + PostgreSQL) | Redirección al login, 401 en API sin token, 403 por rol, CSRF obligatorio, JWT, validación de DTO, aislamiento de notificaciones |
| `SigemApplicationTests` (1) | Integración | Arranque completo del contexto |

Cada prueba describe una regla de negocio en lenguaje del dominio (p. ej. *"rechaza doble asignación en horario que se cruza"*). Para incorporar una regla nueva se sigue el ciclo de TDD: **rojo** (escribir la prueba que falla), **verde** (implementar lo mínimo en el servicio o validador) y **refactorizar** (extraer componentes como `ValidadorDisponibilidad` o los exportadores manteniendo las pruebas en verde). Ejecutar con `./mvnw test`.
