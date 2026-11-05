// Prueba de sistema: cada rol inicia sesión y abre todas las opciones de su menú.
// Una página falla si responde con error HTTP, muestra la página de error de Spring o niega el acceso.
// Uso: node pruebas/sistema/recorrido-roles.js   (con la aplicación en ejecución)
const path = require('path');
const { BASE_URL, SALIDA, CUENTAS, abrirNavegador, iniciarSesion, cerrarSesion, guardar } = require('../comun');

(async () => {
  const { navegador, contexto } = await abrirNavegador();
  const pagina = await contexto.newPage();
  const resultados = [];

  for (const rol of Object.keys(CUENTAS)) {
    await iniciarSesion(pagina, rol);
    await pagina.screenshot({ path: path.join(SALIDA, `sistema-${rol.toLowerCase()}.png`) });
    const enlaces = await pagina.$$eval('a[href^="/"]', as =>
      [...new Set(as.map(a => a.getAttribute('href')))].filter(h => !h.includes('logout') && !h.includes('/exportar') && !h.includes('/recibo')));

    for (const enlace of enlaces) {
      const respuesta = await pagina.goto(BASE_URL + enlace);
      const texto = await pagina.textContent('body');
      const estado = respuesta ? respuesta.status() : 0;
      const ok = estado < 400 && !texto.includes('Whitelabel Error Page')
        && !pagina.url().includes('acceso-denegado') && !pagina.url().includes('/login');
      resultados.push({ rol, pagina: enlace, estado, resultado: ok ? 'APROBADO' : 'FALLIDO' });
    }
    await cerrarSesion(pagina);
  }

  await navegador.close();
  guardar('sistema.json', resultados);
  const fallidos = resultados.filter(r => r.resultado === 'FALLIDO');
  for (const rol of Object.keys(CUENTAS)) {
    const delRol = resultados.filter(r => r.rol === rol);
    console.log(`${rol.padEnd(12)} ${String(delRol.length).padStart(3)} páginas  ${delRol.filter(r => r.resultado === 'APROBADO').length} aprobadas`);
  }
  console.log(`TOTAL: ${resultados.length} páginas, ${resultados.length - fallidos.length} aprobadas, ${fallidos.length} fallidas (${BASE_URL})`);
  fallidos.forEach(f => console.log('  FALLIDO', f.rol, f.pagina, f.estado));
  process.exit(fallidos.length ? 1 : 0);
})();
