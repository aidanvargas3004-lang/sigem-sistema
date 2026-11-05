# Consideraciones de seguridad — SIGEM

## 1. Controles transversales implementados

| Riesgo | Control | Dónde |
|---|---|---|
| Acceso no autenticado | Toda ruta exige sesión salvo las públicas (`/`, `/catalogo`, `/login`, `/register`, recursos estáticos). Antes la regla final era `permitAll()`; ahora es `authenticated()` | `SecurityConfig` |
| Acceso a otra área | Autorización por rol en cada prefijo de URL (`/ventas/**` → VENTAS, `/operaciones/**` → OPERACIONES, `/seguridad/**` → ADMIN, `/reportes/**` → OPERACIONES/GERENCIA/ADMIN…) y `@PreAuthorize` en endpoints sensibles. Página `/acceso-denegado` (HTTP 403) | `SecurityConfig`, controladores |
| Contraseñas | Hash BCrypt; nunca se guardan ni se registran en claro (la auditoría enmascara `password`) | `SecurityConfig.passwordEncoder`, `AuditoriaEntidadesListener` |
| Falsificación de peticiones (CSRF) | CSRF activo en todas las vistas; Thymeleaf inserta el token en cada formulario `th:action`. Solo la API sin estado (`/api/**`, `/auth/**`) lo excluye porque usa JWT | `SecurityConfig` |
| API sin sesión | JWT firmado (HMAC-SHA) con expiración; la API responde **401** sin token y no redirige | `JwtUtil`, `JwtAuthFilter`, `SecurityConfig` |
| Secretos en el código | Credenciales de BD, `JWT_SECRET` y Cloudinary por variables de entorno (`.env.example`); aviso en el log si el secreto JWT es débil o el de desarrollo | `application.properties`, `JwtUtil` |
| Asignación masiva / exposición de entidades | La API recibe y devuelve DTOs (`IncidenciaRequest/Response`, `UbicacionGpsRequest/Response`, etc.) con Bean Validation | paquetes `dto` |
| Errores con detalles internos | `ApiExceptionHandler` devuelve JSON uniforme (fecha, estado, error, detalle) sin trazas | `common/exception` |
| Inyección SQL | Consultas JPA parametrizadas y DAO JDBC con parámetros (`?` / `:nombre`), nunca concatenando valores del usuario | `*Repository`, `AuditoriaJdbcDao`, `ReporteOperativoJdbcDao` |
| XSS | Thymeleaf escapa por defecto (`th:text`); en el mapa GPS el texto ingresado se escapa antes de insertarse en el popup | plantillas, `operaciones/gps.html` |
| Redirección abierta | Al atender una notificación solo se redirige a rutas internas que empiezan con `/` | `NotificacionViewController` |
| Acceso a datos de otro usuario | Las notificaciones solo se consultan/marcan por su dueño (o ADMIN); el portal cliente filtra por el cliente de la sesión | `NotificacionController`, `NotificacionService`, `ClientePortalController` |
| Trazabilidad / no repudio | Bitácora automática de altas, cambios y bajas con usuario, fecha y valores antes/después; registro de inicios de sesión exitosos y fallidos. Solo ADMIN la consulta; ya no existe un endpoint para escribirla manualmente | `AuditoriaEntidadesListener`, `AuditoriaAccesoListener`, `/seguridad/auditoria` |
| Cabeceras HTTP | `X-Content-Type-Options`, `X-Frame-Options: SAMEORIGIN`, `Referrer-Policy: strict-origin-when-cross-origin`, `Cache-Control` en páginas autenticadas | `SecurityConfig.headers` |
| Sesión | Invalidación de sesión y borrado de `JSESSIONID` al cerrar sesión; protección de fijación de sesión por defecto de Spring Security | `SecurityConfig.logout` |
| Documentación de la API | Swagger/OpenAPI solo para ADMIN | `SecurityConfig` |
| Pagos | SIGEM no recibe datos de tarjetas ni cuentas del cliente: solo registra el medio y el N° de operación. El cliente solo reporta pagos de sus propios contratos y solo descarga sus recibos; un pago reportado no cuenta como cobrado hasta que Ventas lo confirma; el N° de operación no se puede repetir; anular un recibo es exclusivo de ADMIN y exige motivo | `PagoService`, `ClientePagoController`, `PagoViewController` (`@PreAuthorize`) |

Pruebas que lo respaldan: `SeguridadIntegracionTest` (redirección al login, 401, 403 por rol, CSRF, JWT, validación de DTO, aislamiento de notificaciones).

## 2. Consideraciones por área

| Área | Activos de información sensibles | Riesgos | Controles en SIGEM | Recomendaciones operativas |
|---|---|---|---|---|
| **Marketing** | Datos de contacto de clientes y prospectos (nombre, RUC/DNI, teléfono, correo) | Fuga de base de clientes, uso no autorizado de datos personales (Ley 29733) | Acceso solo para MARKETING/ADMIN; auditoría de cambios en clientes, leads y oportunidades | Exportar reportes solo cuando sea necesario; no compartir archivos Excel por canales públicos |
| **Ventas / Comercial** | Cotizaciones, montos de contratos, condiciones comerciales, pagos recibidos | Alteración de precios o estados de contratos, divulgación a la competencia, pagos ficticios o duplicados, cobro registrado dos veces | Solo VENTAS/ADMIN modifican; el cliente ve solo sus propios documentos en el portal; `@PreAuthorize` en la API; el monto pagado nunca supera el del contrato; N° de operación único por medio; los pagos reportados por el cliente quedan por validar; solo ADMIN anula recibos; auditoría de cada pago y cambio de estado | Confirmar un pago solo después de verlo en el estado de cuenta o en la app; conciliar mensualmente el reporte de cobranzas con el banco |
| **Producción / Operaciones** | Programación de servicios, ubicación GPS de unidades, lecturas de horómetro/km (base de facturación) | Manipulación de horas de uso, asignaciones en conflicto, exposición de la ubicación de la flota | Lecturas finales no pueden ser menores a las iniciales; reglas de conflicto en `ValidadorDisponibilidad`; API GPS solo con JWT de OPERACIONES/ADMIN; auditoría de lecturas y estados | Usar un usuario/token por dispositivo GPS; rotar `JWT_SECRET` |
| **Mantenimiento** | Costos de OT, consumo de repuestos, estado operativo de las unidades | Consumo de repuestos no registrado, unidad liberada sin completar el trabajo | Repuestos descontados con movimiento de inventario y validación de stock; la unidad queda bloqueada (EN_MANTENIMIENTO) mientras la OT está en proceso; auditoría | Conciliar mensualmente el reporte de mantenimientos con el kardex |
| **Logística / Almacén y Compras** | Stock, kardex, órdenes de compra, proveedores | Salidas sin respaldo, stock negativo, compras no autorizadas | `MovimientoInventarioService` rechaza stock insuficiente; acceso solo LOGISTICA/ADMIN; alerta de stock mínimo; auditoría de cada movimiento | Inventarios físicos periódicos contra el kardex |
| **Recursos Humanos** | Datos personales de empleados, licencias, documentos escaneados | Exposición de datos personales, asignar personal con licencia vencida | Acceso solo RRHH/ADMIN; la licencia vencida bloquea la asignación; alertas de vencimiento; archivos en Cloudinary con URL HTTPS | Restringir la carpeta de Cloudinary; no subir documentos que no se necesiten |
| **Gerencia General** | Indicadores financieros, rentabilidad | Divulgación de información estratégica | Solo GERENCIA/ADMIN; vistas de solo lectura | Revisar periódicamente quién tiene el rol GERENCIA |
| **Administración del sistema** | Usuarios, roles, bitácora | Escalamiento de privilegios, borrado de huellas | Solo ADMIN; bitácora automática e inalterable desde la aplicación; registro de intentos de acceso fallidos | Cambiar las cuentas demo antes de producción; revisar `LOGIN_FALLIDO` en la bitácora |
| **Portal cliente** | Documentos del propio cliente | Ver documentos de otro cliente | El rol CLIENTE solo accede a `/cliente/**` y las consultas se filtran por el cliente asociado al usuario | Registro con correo verificado (mejora futura) |

## 3. Pendientes para producción

1. HTTPS obligatorio (proxy inverso con certificado) y cookie de sesión `Secure`.
2. `JPA_DDL_AUTO=validate` y migraciones versionadas (Flyway).
3. Bloqueo temporal de la cuenta tras varios `LOGIN_FALLIDO` consecutivos.
4. Política de contraseñas (longitud y complejidad) en el registro y cambio de clave.
5. Copiar la carpeta `backups/` (respaldo diario automático ya implementado) a un almacenamiento externo y probar la restauración periódicamente.
6. Retención y archivado de la bitácora de auditoría.
