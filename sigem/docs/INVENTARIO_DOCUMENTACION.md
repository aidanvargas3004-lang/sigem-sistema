# Inventario de documentación por área — SIGEM

Relación de los documentos (registros, formatos y reportes) que cada área de TRANSUR gestiona en SIGEM: si el sistema los **recibe** (entrada), los **genera** (salida) o los **emite como archivo**; dónde se ven y qué tabla los guarda.

Leyenda de soporte: **Pantalla** (registro en la aplicación), **Excel/PDF** (archivo descargable), **Alerta** (notificación automática).

## 1. Marketing

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Ficha de cliente y contactos | Entrada | Pantalla | `/marketing/clientes` | `clientes`, `contactos_cliente` |
| Campaña comercial (presupuesto, fechas, estado, banner) | Entrada | Pantalla | `/marketing/campanias` | `campanias` |
| Registro de lead (prospecto) | Entrada | Pantalla | `/marketing/leads` | `leads` |
| Oportunidad de negocio (estado, monto estimado, probabilidad) | Entrada/Salida | Pantalla | `/marketing/oportunidades` | `oportunidades` |
| Reporte de marketing (conversión y campañas) | Salida | Pantalla, Excel, PDF | `/marketing/reportes`, `/marketing/reportes/exportar/{excel,pdf}` | consulta |

## 2. Ventas / Comercial

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Solicitud de servicio/alquiler (del cliente o registrada por ventas) | Entrada | Pantalla, portal cliente | `/ventas/solicitudes`, `/cliente/solicitudes` | `solicitudes` |
| Cotización con detalle de maquinaria y tarifas | Salida | Pantalla, portal cliente | `/ventas/cotizaciones` | `cotizaciones`, `cotizacion_detalles` |
| Contrato (servicio, alquiler o venta) | Salida | Pantalla (detalle) | `/ventas/contratos/{id}` | `contratos` |
| Registro de venta de maquinaria | Salida | Pantalla | `/ventas/ventas` | `ventas`, `venta_detalles` |
| Registro de alquiler | Salida | Pantalla | `/ventas/alquileres` | `alquileres`, `alquiler_detalles` |
| Reporte de ventas | Salida | Pantalla, Excel, PDF | `/ventas/reportes` | consulta |
| Aviso de contrato por vencer (15 días) | Salida | Alerta | `/notificaciones` | `notificaciones` |
| Registro de pago (efectivo, transferencia, depósito, Yape, Plin, tarjeta POS) con N° de operación | Entrada | Pantalla | `/ventas/pagos/nuevo` | `pagos` |
| Validación de pagos reportados por el cliente (confirmar / rechazar con motivo) | Entrada | Pantalla | `/ventas/pagos?estado=POR_VALIDAR` | `pagos` |
| Recibo de pago (correlativo REC-AAAA-NNNNN) | Salida | PDF | `/ventas/pagos/{id}/recibo` | `pagos` |
| Estado de pago por contrato (pendiente, parcial, pagado, saldo) | Salida | Pantalla | `/ventas/contratos`, `/ventas/contratos/{id}` | `pagos`, `contratos` |
| Reporte de pagos y cobranzas | Salida | Pantalla, Excel, PDF | `/ventas/pagos`, `/ventas/pagos/exportar/{excel,pdf}` | consulta |
| Aviso de pago por validar / pago confirmado o rechazado | Salida | Alerta | `/notificaciones` | `notificaciones` |

## 3. Producción / Operaciones

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Orden de servicio (origen, destino, inicio y fin programados) | Entrada | Pantalla | `/operaciones/servicios` | `servicios` |
| Programación semanal de servicios | Salida | Pantalla | `/operaciones/programacion` | consulta |
| Asignación de unidad y operador (con validación de conflictos) | Entrada | Pantalla, API | `/operaciones/asignaciones` | `asignaciones` |
| Acta de inicio (horómetro y km iniciales, hora real) | Entrada | Pantalla | `/operaciones/servicios/{id}/ejecucion` | `servicios` |
| Acta de cierre (horómetro y km finales, horas y km de uso) | Entrada/Salida | Pantalla | `/operaciones/servicios/{id}/ejecucion` | `servicios`, `maquinarias` |
| Checklist de servicio (evidencia fotográfica del cierre) | Entrada | Pantalla | `/operaciones/checklist` | `servicios` |
| Reporte de incidencia | Entrada | Pantalla, API | `/operaciones/checklist`, `POST /api/incidencias` | `incidencias` |
| Registro de posición GPS / recorrido | Entrada/Salida | Pantalla (mapa), API | `/operaciones/gps`, `/api/gps` | `ubicaciones_gps` |
| Ficha técnica de maquinaria y tarifario | Entrada | Pantalla | `/operaciones/maquinaria` | `maquinarias`, `tipos_maquinaria`, `tarifas` |
| Reporte de servicios realizados | Salida | Pantalla, Excel, PDF | `/reportes?tipo=servicios` | consulta (DAO JDBC) |
| Reporte de uso de flota por unidad | Salida | Pantalla, Excel, PDF | `/reportes?tipo=flota` | consulta |
| Reporte de incidencias | Salida | Pantalla, Excel, PDF | `/reportes?tipo=incidencias` | consulta |
| Reporte de disponibilidad de flota | Salida | Pantalla, Excel, PDF | `/reportes?tipo=disponibilidad` | consulta |
| Aviso de servicio sin unidad asignada (48 h) | Salida | Alerta | `/notificaciones` | `notificaciones` |
| Aviso de SOAT / revisión técnica por vencer o vencida | Salida | Alerta | `/notificaciones` | `notificaciones` |

## 4. Mantenimiento

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Plan de mantenimiento preventivo (por horas, km o días) | Entrada | Pantalla | `/operaciones/mantenimiento/planes` | `planes_mantenimiento` |
| Orden de trabajo (OT) preventiva o correctiva | Entrada/Salida | Pantalla | `/operaciones/mantenimiento/{id}` | `mantenimientos` |
| Vale de repuestos consumidos en la OT | Entrada | Pantalla | `/operaciones/mantenimiento/{id}` | `mantenimiento_repuestos`, `movimientos_inventario` |
| Informe de trabajo realizado y costo | Salida | Pantalla | `/operaciones/mantenimiento/{id}` | `mantenimientos` |
| Reporte de mantenimientos | Salida | Pantalla, Excel, PDF | `/reportes?tipo=mantenimientos` | consulta |
| Aviso de preventivo próximo (90 %) o vencido | Salida | Alerta | `/notificaciones` | `notificaciones` |

## 5. Logística / Almacén y Compras

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Catálogo de repuestos (stock actual y mínimo) | Entrada | Pantalla | `/logistica/repuestos` | `repuestos`, `categorias_repuesto` |
| Almacenes y ubicaciones | Entrada | Pantalla | `/logistica/almacenes` | `almacenes`, `ubicaciones_almacen` |
| Kardex de movimientos (entradas, salidas, ajustes) | Entrada/Salida | Pantalla | `/logistica/movimientos` | `movimientos_inventario` |
| Inventario de maquinaria por almacén | Salida | Pantalla | `/logistica/inventario` | `inventario_maquinaria` |
| Registro de proveedores | Entrada | Pantalla | `/logistica/proveedores` | `proveedores` |
| Orden de compra | Salida | Pantalla | `/logistica/ordenes-compra` | `ordenes_compra`, `orden_compra_detalles` |
| Acta de recepción de compra | Entrada | Pantalla | `/logistica/recepcion` | `recepciones_compra`, `recepcion_detalles` |
| Aviso de stock mínimo | Salida | Alerta | `/notificaciones` | `notificaciones` |

## 6. Recursos Humanos

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Legajo del empleado (cargo, contacto, foto) | Entrada | Pantalla | `/rrhh/empleados` | `empleados` |
| Licencia de conducir, SCTR, certificaciones | Entrada | Pantalla | `/rrhh/documentacion` | `documentos_empleado` |
| Cuadro de vencimientos | Salida | Pantalla | `/rrhh/vencimientos` | consulta |
| Historial de alertas de RRHH | Salida | Pantalla | `/rrhh/alertas` | `notificaciones` |
| Aviso de documento por vencer (30 días) o vencido | Salida | Alerta | `/notificaciones` | `notificaciones` |

## 7. Gerencia General

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Tablero de indicadores | Salida | Pantalla | `/gerencia/dashboard`, `/gerencia/indicadores` | consulta |
| Análisis de rentabilidad | Salida | Pantalla | `/gerencia/rentabilidad` | consulta |
| Resumen de operaciones | Salida | Pantalla | `/gerencia/operaciones` | consulta |
| Reportes ejecutivos | Salida | Pantalla | `/gerencia/reportes` | consulta |
| Reportes operativos detallados | Salida | Pantalla, Excel, PDF | `/reportes` | consulta |

## 8. Administración del sistema

| Documento | E/S | Soporte | Ruta | Tabla |
|---|---|---|---|---|
| Registro de usuarios y asignación de rol | Entrada | Pantalla | `/seguridad/usuarios` | `usuarios` |
| Roles y permisos | Entrada | Pantalla | `/seguridad/roles` | `roles`, `permisos`, `rol_permisos` |
| Bitácora de auditoría (altas, cambios, bajas, accesos) | Salida | Pantalla, Excel, PDF | `/seguridad/auditoria` | `auditoria` |

## 9. Portal del cliente

| Documento | E/S | Soporte | Ruta |
|---|---|---|---|
| Solicitud de servicio | Entrada | Pantalla | `/cliente/solicitudes/nueva` |
| Cotizaciones recibidas | Salida | Pantalla | `/cliente/cotizaciones` |
| Contratos | Salida | Pantalla | `/cliente/contratos` |
| Seguimiento de servicios | Salida | Pantalla | `/cliente/servicios` |
| Saldo de contratos y datos para pagar (cuenta, CCI, Yape, Plin) | Salida | Pantalla | `/cliente/pagos` |
| Reporte de pago (transferencia, depósito, Yape o Plin con N° de operación) | Entrada | Pantalla | `/cliente/pagos` |
| Recibos de pago | Salida | PDF | `/cliente/pagos/{id}/recibo` |

## 10. Documentación técnica del proyecto

| Documento | Ubicación |
|---|---|
| Manual de instalación y ejecución, cuentas demo | `README.md` |
| Arquitectura y patrones (MVC, DTO, DAO, SOLID, TDD) | `docs/ARQUITECTURA.md` |
| Inventario de documentación por área | `docs/INVENTARIO_DOCUMENTACION.md` |
| Consideraciones de seguridad por área | `docs/SEGURIDAD.md` |
| Accesos demo | `docs/ACCESOS_DEMO.md`, `docs/ACCESOS_SIGEM_DEMO.md` |
| Variables de entorno | `.env.example` |
| Documentación de la API REST (OpenAPI) | `/swagger-ui.html` (solo ADMIN) |
| Pruebas automatizadas | `src/test/java` |
