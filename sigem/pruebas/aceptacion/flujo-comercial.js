// Prueba de aceptación: recorre el flujo comercial completo como lo haría el usuario.
// Cliente solicita → Ventas cotiza → Cliente aprueba → Ventas genera el contrato →
// Cliente reporta su pago → Ventas lo confirma → se emite el recibo en PDF.
// Uso: node pruebas/aceptacion/flujo-comercial.js   (con la aplicación en ejecución)
const path = require('path');
const { BASE_URL, SALIDA, abrirNavegador, iniciarSesion, cerrarSesion, guardar } = require('../comun');

const hoy = new Date().toISOString().slice(0, 10);
const enDias = n => new Date(Date.now() + n * 86400000).toISOString().slice(0, 10);
const marca = 'PA-' + Date.now(); // identifica los datos creados por esta ejecución

(async () => {
  const { navegador, contexto } = await abrirNavegador();
  const pagina = await contexto.newPage();
  const casos = [];
  let numeroCotizacion, numeroContrato;

  async function caso(id, descripcion, criterio, pasos) {
    try {
      const evidencia = await pasos();
      casos.push({ id, descripcion, criterio, resultado: 'APROBADO', evidencia });
      await pagina.screenshot({ path: path.join(SALIDA, `aceptacion-${id}.png`) });
    } catch (e) {
      casos.push({ id, descripcion, criterio, resultado: 'FALLIDO', evidencia: e.message });
      await pagina.screenshot({ path: path.join(SALIDA, `aceptacion-${id}-error.png`) });
      throw e;
    }
  }

  try {
    await caso('PA-01', 'El cliente registra una solicitud de alquiler', 'La solicitud aparece en Mis solicitudes como PENDIENTE', async () => {
      await iniciarSesion(pagina, 'CLIENTE');
      await pagina.goto(BASE_URL + '/cliente/solicitudes/nueva');
      await pagina.selectOption('select[name=tipoSolicitud]', 'ALQUILER');
      await pagina.fill('textarea[name=descripcion]', `Excavadora para movimiento de tierras (${marca})`);
      await Promise.all([pagina.waitForNavigation(), pagina.click('form[action$="/cliente/solicitudes"] button[type=submit]')]);
      const fila = pagina.locator('tr', { hasText: marca });
      if (!(await fila.textContent()).includes('PENDIENTE')) throw new Error('La solicitud no quedó PENDIENTE');
      await cerrarSesion(pagina);
      return 'Solicitud registrada y PENDIENTE';
    });

    await caso('PA-02', 'Ventas cotiza la solicitud con maquinaria y tarifa', 'La cotización queda ENVIADA con IGV incluido', async () => {
      await iniciarSesion(pagina, 'VENTAS');
      await pagina.goto(BASE_URL + '/ventas/cotizaciones/nuevo');
      const solicitud = await pagina.$$eval('select[name=solicitudId] option', ops => ops[ops.length - 1].value);
      await pagina.selectOption('select[name=solicitudId]', solicitud);
      await pagina.fill('input[name=fechaEmision]', hoy);
      await pagina.fill('input[name=vigencia]', enDias(15));
      // Primera maquinaria que tenga una tarifa registrada
      const maquinas = await pagina.$$eval('#tarifa-data option', ops => [...new Set(ops.map(o => o.dataset.maquina))]);
      await pagina.selectOption('select[name="detalles[0].maquinariaId"]', maquinas[0]);
      const tarifa = await pagina.$$eval('select[name="detalles[0].tarifaId"] option', ops => ops.find(o => o.value).value);
      await pagina.selectOption('select[name="detalles[0].tarifaId"]', tarifa);
      await pagina.fill('input[name="detalles[0].cantidad"]', '3');
      await Promise.all([pagina.waitForNavigation(), pagina.click('form[action$="/ventas/cotizaciones"] button[type=submit]')]);
      const fila = pagina.locator('tbody tr', { hasText: new RegExp(`SOL-${solicitud}\\b`) }).first();
      const texto = await fila.textContent();
      if (!texto.includes('ENVIADA')) throw new Error('La cotización no quedó ENVIADA');
      numeroCotizacion = texto.match(/COT-\d{4}-\d+/)[0];
      await cerrarSesion(pagina);
      return `Cotización ${numeroCotizacion} ENVIADA`;
    });

    await caso('PA-03', 'El cliente aprueba la cotización desde su portal', 'La cotización pasa a APROBADA', async () => {
      await iniciarSesion(pagina, 'CLIENTE');
      await pagina.goto(BASE_URL + '/cliente/cotizaciones');
      const tarjeta = pagina.locator('tr, .card, article', { hasText: numeroCotizacion }).last();
      await Promise.all([pagina.waitForNavigation(), tarjeta.locator('form:has(input[value=APROBAR]) button').click()]);
      const texto = await pagina.locator('tr, .card, article', { hasText: numeroCotizacion }).last().textContent();
      if (!texto.includes('APROBADA')) throw new Error('La cotización no quedó APROBADA');
      await cerrarSesion(pagina);
      return `${numeroCotizacion} APROBADA por el cliente`;
    });

    await caso('PA-04', 'Ventas genera el contrato desde la cotización aprobada', 'El contrato queda VIGENTE con el monto de la cotización', async () => {
      await iniciarSesion(pagina, 'VENTAS');
      await pagina.goto(BASE_URL + '/ventas/contratos/nuevo');
      const opcion = await pagina.$$eval('#cotizacion option', (ops, n) => ops.find(o => o.textContent.startsWith(n)).value, numeroCotizacion);
      await pagina.selectOption('#cotizacion', opcion);
      await pagina.fill('#inicio', hoy);
      await pagina.fill('#fin', enDias(30));
      await Promise.all([pagina.waitForNavigation(), pagina.click('form[action$="/ventas/contratos"] button[type=submit]')]);
      const texto = await pagina.textContent('body');
      numeroContrato = texto.match(/CTR-\d{4}-\d+/g).pop();
      if (!texto.includes('VIGENTE')) throw new Error('El contrato no quedó VIGENTE');
      await cerrarSesion(pagina);
      return `Contrato ${numeroContrato} VIGENTE`;
    });

    // Llena el formulario Reportar un pago del portal cliente y devuelve el saldo mostrado del contrato
    async function reportarPago(monto) {
      await pagina.goto(BASE_URL + '/cliente/pagos');
      const opcion = await pagina.$$eval('select[name=contratoId] option', (ops, n) => {
        const o = ops.find(x => x.textContent.includes(n));
        return { valor: o.value, saldo: Number(o.textContent.match(/saldo S\/ ([\d.]+)/)[1]) };
      }, numeroContrato);
      await pagina.selectOption('select[name=contratoId]', opcion.valor);
      await pagina.selectOption('select[name=metodo]', 'YAPE');
      await pagina.fill('input[name=numeroOperacion]', String(Date.now()).slice(-8));
      await pagina.fill('input[name=fechaPago]', hoy);
      await pagina.fill('input[name=monto]', String(monto(opcion.saldo)));
      await Promise.all([pagina.waitForNavigation(), pagina.click('form[action$="/cliente/pagos"] button[type=submit]')]);
      return opcion.saldo;
    }

    await caso('PA-05', 'El cliente intenta reportar un pago mayor a su saldo', 'SIGEM rechaza el pago e informa el saldo pendiente', async () => {
      await iniciarSesion(pagina, 'CLIENTE');
      const saldo = await reportarPago(s => (s + 100).toFixed(2));
      if (!(await pagina.textContent('body')).includes('excede el saldo pendiente')) throw new Error('El sistema aceptó un pago mayor al saldo');
      return `Rechazado: S/ ${(saldo + 100).toFixed(2)} supera el saldo de S/ ${saldo.toFixed(2)}`;
    });

    await caso('PA-06', 'El cliente reporta un pago parcial por Yape con su N° de operación', 'El pago queda POR_VALIDAR', async () => {
      const saldo = await reportarPago(s => (s / 2).toFixed(2));
      const enValidacion = await pagina.locator('tr', { hasText: numeroContrato }).filter({ hasText: /por.validar/i }).count();
      if (!(await pagina.textContent('body')).includes('Pago reportado') || enValidacion === 0)
        throw new Error('El pago no quedó POR_VALIDAR');
      await cerrarSesion(pagina);
      return `Pago de S/ ${(saldo / 2).toFixed(2)} reportado y POR_VALIDAR`;
    });

    await caso('PA-07', 'Ventas confirma el pago y emite el recibo en PDF', 'El pago pasa a CONFIRMADO y el recibo REC-AAAA-NNNNN se descarga en PDF', async () => {
      await iniciarSesion(pagina, 'VENTAS');
      await pagina.goto(BASE_URL + '/ventas/pagos?estado=POR_VALIDAR');
      const fila = pagina.locator('tr', { hasText: numeroContrato }).first();
      pagina.once('dialog', dialogo => dialogo.accept()); // "¿Confirmar este pago?"
      await Promise.all([pagina.waitForNavigation(), fila.locator('form[action*="/confirmar"] button').click()]);
      await pagina.goto(BASE_URL + '/ventas/pagos?estado=CONFIRMADO');
      const confirmada = pagina.locator('tr', { hasText: numeroContrato }).first();
      const recibo = (await confirmada.textContent()).match(/REC-\d{4}-\d{5}/)[0];
      const href = await confirmada.locator('a[href*="/recibo"]').getAttribute('href');
      const respuesta = await pagina.request.get(BASE_URL + href);
      if (!respuesta.headers()['content-type'].includes('pdf')) throw new Error('El recibo no es un PDF');
      return `Pago CONFIRMADO, recibo ${recibo} en PDF (${(await respuesta.body()).length} bytes)`;
    });
  } catch (e) {
    console.error('Se detuvo en un caso fallido:', e.message);
  }

  await navegador.close();
  guardar('aceptacion.json', casos);
  casos.forEach(c => console.log(`${c.id}  ${c.resultado.padEnd(9)} ${c.descripcion} → ${c.evidencia}`));
  const aprobados = casos.filter(c => c.resultado === 'APROBADO').length;
  console.log(`TOTAL: ${aprobados} de 7 casos de aceptación aprobados`);
  process.exit(aprobados === 7 ? 0 : 1);
})();
