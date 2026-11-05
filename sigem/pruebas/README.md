# Pruebas de sistema y de aceptación

Las pruebas unitarias y de integración están en `src/test/java` y se ejecutan con Maven.
Esta carpeta contiene las pruebas que usan la aplicación desde el navegador, con Playwright.

| Script | Tipo | Qué comprueba |
|---|---|---|
| `sistema/recorrido-roles.js` | Sistema | Los 8 roles inician sesión y abren cada opción de su menú sin errores ni accesos denegados |
| `aceptacion/flujo-comercial.js` | Aceptación | Solicitud → cotización → aprobación del cliente → contrato → pago → confirmación y recibo PDF |

## Ejecución

1. Inicie SIGEM con la base de datos de demostración (`mvnw.cmd spring-boot:run`).
2. En otra terminal:

```bash
cd pruebas
npm install
npx playwright install chromium
npm run todas
```

Variables opcionales: `BASE_URL` (por defecto `http://localhost:8080`) y `SALIDA` (carpeta de capturas y resultados, por defecto `pruebas/resultados`).
Cada ejecución del flujo comercial crea una solicitud, una cotización, un contrato y un pago nuevos; úselo en la base de demostración, no en datos reales.
