# Historial de versiones

## [v3.0] - 2026-10-26

- Módulo de pagos y cobranzas: Ventas registra pagos en efectivo, transferencia, depósito, Yape, Plin o tarjeta (POS) con su número de operación y emite recibos en PDF; el cliente ve el saldo de sus contratos y reporta sus pagos desde *Mis pagos*; Ventas los confirma o rechaza y solo ADMIN anula recibos.
- Estado de pago (pendiente, parcial, pagado) en el listado y el detalle de contratos; reporte de cobranzas en Excel y PDF.
- Importación de repuestos desde Excel con plantilla descargable.
- Respaldos automáticos y a pedido de la base de datos (pg_dump o JDBC).
- Correcciones: error 500 en *Mis cotizaciones* y *Mis contratos* del portal cliente; rechazo de cotizaciones por POST con token CSRF; la página de acceso denegado acepta POST.
- 63 pruebas automatizadas (17 nuevas del módulo de pagos).
- Documentación: informe de avance v3.0, presentaciones del proyecto y evidencias de pagos, respaldos e importación en `docs/`.

> SIGEM no procesa cobros: registra y valida los pagos hechos por los medios habituales.

## [v2.1] - 2026-10-22

- Version 2 del sistema (rama `SigemVs2.1`).

## [v1.2] - 2026-10-02

- Primeras versiones integradas de SIGEM (ramas `SigemV1.1`, del 2026-09-30, y `SigemV1.2`).
