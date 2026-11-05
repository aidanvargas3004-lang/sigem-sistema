// Utilidades compartidas por las pruebas de sistema y de aceptación (Playwright)
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const SALIDA = process.env.SALIDA || path.join(__dirname, 'resultados');

// Cuentas de demostración creadas por DemoDataSeeder (docs/ACCESOS_DEMO.md)
const CUENTAS = {
  ADMIN: ['admin', 'Admin123*'],
  GERENCIA: ['gerente', 'Gerente123*'],
  VENTAS: ['ventas', 'Ventas123*'],
  MARKETING: ['marketing', 'Marketing123*'],
  OPERACIONES: ['operaciones', 'Operaciones123*'],
  RRHH: ['rrhh', 'Rrhh123*'],
  LOGISTICA: ['logistica', 'Logistica123*'],
  CLIENTE: ['cliente', 'Cliente123*'],
};

async function abrirNavegador() {
  const navegador = await chromium.launch();
  const contexto = await navegador.newContext({ viewport: { width: 1366, height: 800 }, acceptDownloads: true });
  // Sin internet, CDN_LOCAL permite servir Bootstrap, Chart.js y Leaflet desde una carpeta con esos paquetes npm
  if (process.env.CDN_LOCAL) {
    await contexto.route('https://cdn.jsdelivr.net/npm/**', async ruta => {
      const url = new URL(ruta.request().url());
      let rel = decodeURIComponent(url.pathname.replace('/npm/', ''));
      rel = rel.replace(/^(@?[^@/]+(?:\/[^@/]+)?)@[^/]+/, '$1');
      if (rel === 'chart.js') rel = 'chart.js/dist/chart.umd.js';
      const archivo = path.join(process.env.CDN_LOCAL, 'node_modules', rel);
      if (fs.existsSync(archivo)) return ruta.fulfill({ path: archivo });
      return ruta.abort();
    });
  }
  return { navegador, contexto };
}

async function iniciarSesion(pagina, rol) {
  const [usuario, clave] = CUENTAS[rol];
  await pagina.goto(BASE_URL + '/login');
  await pagina.fill('input[name=username]', usuario);
  await pagina.fill('input[name=password]', clave);
  await Promise.all([pagina.waitForNavigation(), pagina.click('button[type=submit]')]);
  if (pagina.url().includes('/login')) throw new Error(`No se pudo iniciar sesión como ${rol}`);
}

async function cerrarSesion(pagina) {
  await pagina.context().clearCookies();
}

function guardar(nombre, datos) {
  fs.mkdirSync(SALIDA, { recursive: true });
  fs.writeFileSync(path.join(SALIDA, nombre), JSON.stringify(datos, null, 2));
}

module.exports = { BASE_URL, SALIDA, CUENTAS, abrirNavegador, iniciarSesion, cerrarSesion, guardar };
