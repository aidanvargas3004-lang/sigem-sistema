-- =====================================================================
--  SIGEM - Sistema Integral de Gestión de Flotas y Maquinaria (TRANSUR)
--  Script completo de base de datos PostgreSQL 16
--
--  PASO 1: conéctese a la base "postgres" en pgAdmin 4 (Query Tool) y
--          ejecute SOLO el bloque "PASO 1".
--  PASO 2: seleccione la base "sigem_db" (clic sobre ella), abra un
--          Query Tool nuevo y ejecute desde "PASO 2" hasta el final.
--
--  Nota: este paso 2 es opcional. Si lo omite, la aplicación crea todas
--  las tablas al arrancar. En ambos casos, los roles, usuarios y datos
--  de demostración los inserta la aplicación al iniciar.
-- =====================================================================

-- ============================ PASO 1 =================================
CREATE DATABASE sigem_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE sigem_db
    IS 'Sistema Integral de Gestión de Flotas y Maquinaria - TRANSUR';

-- ============================ PASO 2 =================================
-- (Ejecutar conectado a sigem_db)

--
--



SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adquisiciones_maquinaria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.adquisiciones_maquinaria (
    id bigint NOT NULL,
    costo_total numeric(15,2) NOT NULL,
    destino_inicial character varying(30) NOT NULL,
    documentos_legal_url character varying(255),
    estado character varying(30) NOT NULL,
    fecha_adquisicion date NOT NULL,
    numero_serie_fabrica character varying(100),
    tipo_adquisicion character varying(30) NOT NULL,
    proveedor_id bigint NOT NULL
);


--
-- Name: adquisiciones_maquinaria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.adquisiciones_maquinaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: adquisiciones_maquinaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.adquisiciones_maquinaria_id_seq OWNED BY public.adquisiciones_maquinaria.id;


--
-- Name: almacenes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.almacenes (
    id bigint NOT NULL,
    descripcion text,
    direccion character varying(255),
    estado boolean NOT NULL,
    nombre character varying(100) NOT NULL,
    responsable_id bigint
);


--
-- Name: almacenes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.almacenes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: almacenes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.almacenes_id_seq OWNED BY public.almacenes.id;


--
-- Name: alquiler_detalles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alquiler_detalles (
    id bigint NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    unidades_tiempo integer NOT NULL,
    alquiler_id bigint NOT NULL,
    maquinaria_id bigint NOT NULL,
    tarifa_id bigint
);


--
-- Name: alquiler_detalles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alquiler_detalles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alquiler_detalles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.alquiler_detalles_id_seq OWNED BY public.alquiler_detalles.id;


--
-- Name: alquileres; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alquileres (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_fin_real timestamp(6) without time zone,
    fecha_inicio_real timestamp(6) without time zone,
    observaciones text,
    contrato_id bigint NOT NULL
);


--
-- Name: alquileres_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alquileres_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alquileres_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.alquileres_id_seq OWNED BY public.alquileres.id;


--
-- Name: asignaciones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.asignaciones (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_asignacion timestamp(6) without time zone NOT NULL,
    empleado_id bigint,
    maquinaria_id bigint NOT NULL,
    servicio_id bigint NOT NULL
);


--
-- Name: asignaciones_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.asignaciones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: asignaciones_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.asignaciones_id_seq OWNED BY public.asignaciones.id;


--
-- Name: auditoria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auditoria (
    id bigint NOT NULL,
    accion character varying(100) NOT NULL,
    datos_anteriores jsonb,
    datos_nuevos jsonb,
    entidad character varying(100),
    entidad_id bigint,
    fecha timestamp(6) without time zone NOT NULL,
    modulo character varying(100) NOT NULL,
    usuario_id bigint
);


--
-- Name: auditoria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: auditoria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.auditoria_id_seq OWNED BY public.auditoria.id;


--
-- Name: campanias; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.campanias (
    id bigint NOT NULL,
    banner_url character varying(500),
    descripcion text,
    estado character varying(20) NOT NULL,
    fecha_fin date,
    fecha_inicio date NOT NULL,
    leads_generados integer,
    nombre character varying(150) NOT NULL,
    presupuesto numeric(12,2)
);


--
-- Name: campanias_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.campanias_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: campanias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.campanias_id_seq OWNED BY public.campanias.id;


--
-- Name: categorias_repuesto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categorias_repuesto (
    id bigint NOT NULL,
    descripcion text,
    nombre character varying(100) NOT NULL
);


--
-- Name: categorias_repuesto_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categorias_repuesto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categorias_repuesto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categorias_repuesto_id_seq OWNED BY public.categorias_repuesto.id;


--
-- Name: clientes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clientes (
    id bigint NOT NULL,
    direccion text,
    documento character varying(30) NOT NULL,
    email character varying(100),
    estado boolean NOT NULL,
    fecha_registro timestamp(6) without time zone NOT NULL,
    logo_url character varying(500),
    razon_social character varying(200) NOT NULL,
    telefono character varying(20),
    tipo_cliente character varying(20) NOT NULL,
    usuario_id bigint
);


--
-- Name: clientes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clientes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clientes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clientes_id_seq OWNED BY public.clientes.id;


--
-- Name: contactos_cliente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contactos_cliente (
    id bigint NOT NULL,
    cargo character varying(100),
    email character varying(100),
    nombres character varying(100) NOT NULL,
    telefono character varying(20),
    cliente_id bigint NOT NULL
);


--
-- Name: contactos_cliente_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contactos_cliente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: contactos_cliente_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.contactos_cliente_id_seq OWNED BY public.contactos_cliente.id;


--
-- Name: contratos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contratos (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_fin date,
    fecha_inicio date NOT NULL,
    monto_total numeric(12,2) NOT NULL,
    numero character varying(30) NOT NULL,
    tipo_contrato character varying(30) NOT NULL,
    cliente_id bigint NOT NULL,
    cotizacion_id bigint NOT NULL
);


--
-- Name: contratos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contratos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: contratos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.contratos_id_seq OWNED BY public.contratos.id;


--
-- Name: cotizacion_detalles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cotizacion_detalles (
    id bigint NOT NULL,
    cantidad integer NOT NULL,
    descripcion character varying(255) NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    cotizacion_id bigint NOT NULL,
    maquinaria_id bigint
);


--
-- Name: cotizacion_detalles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cotizacion_detalles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cotizacion_detalles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cotizacion_detalles_id_seq OWNED BY public.cotizacion_detalles.id;


--
-- Name: cotizaciones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cotizaciones (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_emision date NOT NULL,
    impuestos numeric(12,2),
    numero character varying(30) NOT NULL,
    subtotal numeric(12,2),
    total numeric(12,2) NOT NULL,
    vigencia date,
    cliente_id bigint NOT NULL,
    solicitud_id bigint NOT NULL
);


--
-- Name: cotizaciones_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cotizaciones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cotizaciones_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cotizaciones_id_seq OWNED BY public.cotizaciones.id;


--
-- Name: detalles_adquisicion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.detalles_adquisicion (
    id bigint NOT NULL,
    observaciones text,
    precio_unitario numeric(15,2) NOT NULL,
    adquisicion_id bigint NOT NULL,
    maquinaria_id bigint NOT NULL
);


--
-- Name: detalles_adquisicion_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.detalles_adquisicion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalles_adquisicion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.detalles_adquisicion_id_seq OWNED BY public.detalles_adquisicion.id;


--
-- Name: documentos_empleado; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.documentos_empleado (
    id bigint NOT NULL,
    estado boolean NOT NULL,
    fecha_emision date,
    fecha_vencimiento date NOT NULL,
    numero_documento character varying(50),
    ruta_archivo character varying(255),
    tipo_documento character varying(100) NOT NULL,
    empleado_id bigint NOT NULL
);


--
-- Name: documentos_empleado_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.documentos_empleado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: documentos_empleado_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.documentos_empleado_id_seq OWNED BY public.documentos_empleado.id;


--
-- Name: empleados; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.empleados (
    id bigint NOT NULL,
    apellidos character varying(100) NOT NULL,
    cargo character varying(100) NOT NULL,
    documento character varying(30) NOT NULL,
    email character varying(100),
    estado boolean NOT NULL,
    fecha_ingreso date NOT NULL,
    foto_perfil_url character varying(500),
    licencia_url character varying(500),
    nombres character varying(100) NOT NULL,
    telefono character varying(20),
    usuario_id bigint
);


--
-- Name: empleados_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.empleados_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: empleados_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.empleados_id_seq OWNED BY public.empleados.id;


--
-- Name: incidencias; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.incidencias (
    id bigint NOT NULL,
    descripcion text NOT NULL,
    estado character varying(30) NOT NULL,
    fecha timestamp(6) without time zone NOT NULL,
    foto_evidencia_url character varying(500),
    tipo character varying(50) NOT NULL,
    maquinaria_id bigint,
    servicio_id bigint NOT NULL
);


--
-- Name: incidencias_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.incidencias_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: incidencias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.incidencias_id_seq OWNED BY public.incidencias.id;


--
-- Name: inventario_maquinaria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventario_maquinaria (
    id bigint NOT NULL,
    fecha_ingreso date NOT NULL,
    observaciones text,
    situacion_logistica character varying(30) NOT NULL,
    maquinaria_id bigint NOT NULL,
    ubicacion_id bigint
);


--
-- Name: inventario_maquinaria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inventario_maquinaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inventario_maquinaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inventario_maquinaria_id_seq OWNED BY public.inventario_maquinaria.id;


--
-- Name: leads; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.leads (
    id bigint NOT NULL,
    email character varying(100),
    empresa character varying(150),
    estado character varying(30) NOT NULL,
    fecha_registro timestamp(6) without time zone,
    foto_contacto_url character varying(500),
    nombres character varying(150) NOT NULL,
    origen character varying(50),
    telefono character varying(20),
    campania_id bigint,
    servicio_id bigint
);


--
-- Name: leads_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.leads_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: leads_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.leads_id_seq OWNED BY public.leads.id;


--
-- Name: mantenimiento_repuestos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mantenimiento_repuestos (
    id bigint NOT NULL,
    cantidad integer NOT NULL,
    costo_unitario numeric(12,2),
    mantenimiento_id bigint NOT NULL,
    repuesto_id bigint NOT NULL
);


--
-- Name: mantenimiento_repuestos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mantenimiento_repuestos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mantenimiento_repuestos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.mantenimiento_repuestos_id_seq OWNED BY public.mantenimiento_repuestos.id;


--
-- Name: mantenimientos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mantenimientos (
    id bigint NOT NULL,
    costo numeric(12,2),
    descripcion text NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_fin date,
    fecha_inicio date,
    fecha_programada date,
    horometro_registro numeric(10,2),
    observaciones text,
    tipo_mantenimiento character varying(30) NOT NULL,
    empleado_id bigint,
    incidencia_id bigint,
    maquinaria_id bigint NOT NULL,
    plan_id bigint
);


--
-- Name: mantenimientos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mantenimientos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mantenimientos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.mantenimientos_id_seq OWNED BY public.mantenimientos.id;


--
-- Name: maquinarias; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.maquinarias (
    id bigint NOT NULL,
    anio integer,
    codigo_interno character varying(50) NOT NULL,
    destino_operativo character varying(30) NOT NULL,
    estado_operativo character varying(30) NOT NULL,
    horometro_actual numeric(10,2),
    imagen_url character varying(500),
    kilometraje_actual numeric(12,2),
    marca character varying(100) NOT NULL,
    modelo character varying(100) NOT NULL,
    placa character varying(20),
    precio_base_alquiler numeric(12,2),
    precio_base_venta numeric(12,2),
    revision_tecnica_vencimiento date,
    soat_vencimiento date,
    tipo_id bigint NOT NULL
);


--
-- Name: maquinarias_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.maquinarias_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: maquinarias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.maquinarias_id_seq OWNED BY public.maquinarias.id;


--
-- Name: movimientos_inventario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.movimientos_inventario (
    id bigint NOT NULL,
    cantidad integer NOT NULL,
    fecha timestamp(6) without time zone NOT NULL,
    motivo text,
    tipo_movimiento character varying(30) NOT NULL,
    repuesto_id bigint NOT NULL,
    ubicacion_id bigint,
    usuario_id bigint
);


--
-- Name: movimientos_inventario_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.movimientos_inventario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: movimientos_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.movimientos_inventario_id_seq OWNED BY public.movimientos_inventario.id;


--
-- Name: notificaciones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notificaciones (
    id bigint NOT NULL,
    clave character varying(150),
    enlace character varying(200),
    fecha timestamp(6) without time zone NOT NULL,
    leida boolean NOT NULL,
    mensaje text NOT NULL,
    tipo character varying(30),
    titulo character varying(150) NOT NULL,
    usuario_id bigint
);


--
-- Name: notificaciones_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notificaciones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notificaciones_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notificaciones_id_seq OWNED BY public.notificaciones.id;


--
-- Name: oportunidades; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oportunidades (
    id bigint NOT NULL,
    descripcion text,
    estado character varying(30) NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    monto_estimado numeric(12,2),
    probabilidad integer,
    cliente_id bigint,
    lead_id bigint
);


--
-- Name: oportunidades_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.oportunidades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: oportunidades_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.oportunidades_id_seq OWNED BY public.oportunidades.id;


--
-- Name: orden_compra_detalles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orden_compra_detalles (
    id bigint NOT NULL,
    cantidad integer NOT NULL,
    descripcion character varying(255) NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    orden_compra_id bigint NOT NULL,
    repuesto_id bigint
);


--
-- Name: orden_compra_detalles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.orden_compra_detalles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: orden_compra_detalles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.orden_compra_detalles_id_seq OWNED BY public.orden_compra_detalles.id;


--
-- Name: ordenes_compra; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ordenes_compra (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_emision date NOT NULL,
    impuestos numeric(12,2),
    numero character varying(30) NOT NULL,
    subtotal numeric(12,2),
    total numeric(12,2) NOT NULL,
    proveedor_id bigint NOT NULL,
    usuario_id bigint
);


--
-- Name: ordenes_compra_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ordenes_compra_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ordenes_compra_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ordenes_compra_id_seq OWNED BY public.ordenes_compra.id;


--
-- Name: pagos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pagos (
    id bigint NOT NULL,
    estado character varying(20) NOT NULL,
    fecha_pago date NOT NULL,
    fecha_registro timestamp(6) without time zone NOT NULL,
    fecha_revision timestamp(6) without time zone,
    metodo character varying(20) NOT NULL,
    monto numeric(12,2) NOT NULL,
    motivo character varying(255),
    numero_operacion character varying(40),
    numero_recibo character varying(20),
    observacion character varying(255),
    origen character varying(10) NOT NULL,
    registrado_por character varying(50) NOT NULL,
    revisado_por character varying(50),
    contrato_id bigint NOT NULL,
    CONSTRAINT pagos_estado_check CHECK (((estado)::text = ANY ((ARRAY['POR_VALIDAR'::character varying, 'CONFIRMADO'::character varying, 'RECHAZADO'::character varying, 'ANULADO'::character varying])::text[]))),
    CONSTRAINT pagos_metodo_check CHECK (((metodo)::text = ANY ((ARRAY['EFECTIVO'::character varying, 'TRANSFERENCIA'::character varying, 'DEPOSITO'::character varying, 'YAPE'::character varying, 'PLIN'::character varying, 'TARJETA'::character varying])::text[])))
);


--
-- Name: pagos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pagos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pagos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pagos_id_seq OWNED BY public.pagos.id;


--
-- Name: permisos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permisos (
    id bigint NOT NULL,
    codigo character varying(100) NOT NULL,
    estado boolean NOT NULL,
    icono character varying(100),
    modulo character varying(50) NOT NULL,
    nombre character varying(100) NOT NULL,
    orden integer NOT NULL,
    ruta character varying(150)
);


--
-- Name: permisos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.permisos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: permisos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.permisos_id_seq OWNED BY public.permisos.id;


--
-- Name: planes_mantenimiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.planes_mantenimiento (
    id bigint NOT NULL,
    actividad character varying(150) NOT NULL,
    activo boolean NOT NULL,
    criterio character varying(20) NOT NULL,
    frecuencia numeric(12,2) NOT NULL,
    ultima_fecha date,
    ultimo_valor numeric(12,2),
    maquinaria_id bigint NOT NULL
);


--
-- Name: planes_mantenimiento_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.planes_mantenimiento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: planes_mantenimiento_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.planes_mantenimiento_id_seq OWNED BY public.planes_mantenimiento.id;


--
-- Name: proveedores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.proveedores (
    id bigint NOT NULL,
    direccion text,
    documento character varying(30) NOT NULL,
    email character varying(100),
    estado boolean NOT NULL,
    fecha_registro timestamp(6) without time zone NOT NULL,
    razon_social character varying(200) NOT NULL,
    telefono character varying(20),
    tipo_proveedor character varying(20) NOT NULL
);


--
-- Name: proveedores_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.proveedores_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: proveedores_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.proveedores_id_seq OWNED BY public.proveedores.id;


--
-- Name: recepcion_detalles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recepcion_detalles (
    id bigint NOT NULL,
    cantidad_recibida integer NOT NULL,
    descripcion character varying(255),
    observacion text,
    recepcion_id bigint NOT NULL,
    repuesto_id bigint
);


--
-- Name: recepcion_detalles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recepcion_detalles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recepcion_detalles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recepcion_detalles_id_seq OWNED BY public.recepcion_detalles.id;


--
-- Name: recepciones_compra; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recepciones_compra (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_recepcion date NOT NULL,
    numero character varying(30) NOT NULL,
    observaciones text,
    orden_compra_id bigint NOT NULL,
    usuario_id bigint
);


--
-- Name: recepciones_compra_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recepciones_compra_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recepciones_compra_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recepciones_compra_id_seq OWNED BY public.recepciones_compra.id;


--
-- Name: repuestos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.repuestos (
    id bigint NOT NULL,
    codigo character varying(30) NOT NULL,
    descripcion text,
    estado boolean NOT NULL,
    imagen_url character varying(500),
    nombre character varying(150) NOT NULL,
    stock_actual integer NOT NULL,
    stock_minimo integer NOT NULL,
    unidad_medida character varying(30),
    categoria_id bigint
);


--
-- Name: repuestos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.repuestos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: repuestos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.repuestos_id_seq OWNED BY public.repuestos.id;


--
-- Name: rol_permisos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol_permisos (
    permiso_id bigint NOT NULL,
    rol_id bigint NOT NULL
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    descripcion character varying(255),
    estado boolean NOT NULL,
    nombre character varying(50) NOT NULL
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: servicios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.servicios (
    id bigint NOT NULL,
    destino character varying(255),
    estado character varying(30) NOT NULL,
    fecha_fin_programada timestamp(6) without time zone,
    fecha_fin_real timestamp(6) without time zone,
    fecha_inicio_real timestamp(6) without time zone,
    fecha_programada timestamp(6) without time zone NOT NULL,
    foto_checklist_url character varying(500),
    horas_uso numeric(10,2),
    horometro_final numeric(10,2),
    horometro_inicial numeric(10,2),
    km_final numeric(12,2),
    km_inicial numeric(12,2),
    km_recorridos numeric(12,2),
    observaciones text,
    origen character varying(255),
    contrato_id bigint NOT NULL,
    tipo_servicio_id bigint NOT NULL
);


--
-- Name: servicios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.servicios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: servicios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.servicios_id_seq OWNED BY public.servicios.id;


--
-- Name: solicitudes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.solicitudes (
    id bigint NOT NULL,
    descripcion text,
    estado character varying(30) NOT NULL,
    fecha_solicitud timestamp(6) without time zone NOT NULL,
    tipo_solicitud character varying(30) NOT NULL,
    cliente_id bigint NOT NULL
);


--
-- Name: solicitudes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.solicitudes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: solicitudes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.solicitudes_id_seq OWNED BY public.solicitudes.id;


--
-- Name: tarifas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tarifas (
    id bigint NOT NULL,
    precio numeric(12,2) NOT NULL,
    tipo_tarifa character varying(20) NOT NULL,
    vigencia_fin date,
    vigencia_inicio date NOT NULL,
    maquinaria_id bigint
);


--
-- Name: tarifas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tarifas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tarifas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tarifas_id_seq OWNED BY public.tarifas.id;


--
-- Name: tipos_maquinaria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipos_maquinaria (
    id bigint NOT NULL,
    descripcion text,
    imagen_url character varying(500),
    nombre character varying(100) NOT NULL
);


--
-- Name: tipos_maquinaria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tipos_maquinaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipos_maquinaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tipos_maquinaria_id_seq OWNED BY public.tipos_maquinaria.id;


--
-- Name: tipos_servicio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipos_servicio (
    id bigint NOT NULL,
    descripcion text,
    estado boolean NOT NULL,
    imagen_url character varying(500),
    nombre character varying(100) NOT NULL
);


--
-- Name: tipos_servicio_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tipos_servicio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipos_servicio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tipos_servicio_id_seq OWNED BY public.tipos_servicio.id;


--
-- Name: ubicaciones_almacen; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ubicaciones_almacen (
    id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    descripcion text,
    estado boolean NOT NULL,
    nombre character varying(100),
    almacen_id bigint NOT NULL
);


--
-- Name: ubicaciones_almacen_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ubicaciones_almacen_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ubicaciones_almacen_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ubicaciones_almacen_id_seq OWNED BY public.ubicaciones_almacen.id;


--
-- Name: ubicaciones_gps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ubicaciones_gps (
    id bigint NOT NULL,
    fecha_hora timestamp(6) without time zone NOT NULL,
    latitud double precision NOT NULL,
    longitud double precision NOT NULL,
    referencia character varying(200),
    registrado_por character varying(60),
    velocidad double precision,
    maquinaria_id bigint NOT NULL,
    servicio_id bigint
);


--
-- Name: ubicaciones_gps_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ubicaciones_gps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ubicaciones_gps_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ubicaciones_gps_id_seq OWNED BY public.ubicaciones_gps.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    apellidos character varying(100) NOT NULL,
    email character varying(100) NOT NULL,
    estado boolean NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    nombres character varying(100) NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(50) NOT NULL,
    cliente_id bigint,
    rol_id bigint NOT NULL
);


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: venta_detalles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.venta_detalles (
    id bigint NOT NULL,
    cantidad integer NOT NULL,
    precio_final numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    maquinaria_id bigint NOT NULL,
    venta_id bigint NOT NULL
);


--
-- Name: venta_detalles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.venta_detalles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: venta_detalles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.venta_detalles_id_seq OWNED BY public.venta_detalles.id;


--
-- Name: ventas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ventas (
    id bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_venta date NOT NULL,
    impuestos numeric(12,2),
    numero character varying(30) NOT NULL,
    subtotal numeric(12,2),
    total_final numeric(12,2) NOT NULL,
    contrato_id bigint NOT NULL
);


--
-- Name: ventas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ventas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ventas_id_seq OWNED BY public.ventas.id;


--
-- Name: adquisiciones_maquinaria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adquisiciones_maquinaria ALTER COLUMN id SET DEFAULT nextval('public.adquisiciones_maquinaria_id_seq'::regclass);


--
-- Name: almacenes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.almacenes ALTER COLUMN id SET DEFAULT nextval('public.almacenes_id_seq'::regclass);


--
-- Name: alquiler_detalles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquiler_detalles ALTER COLUMN id SET DEFAULT nextval('public.alquiler_detalles_id_seq'::regclass);


--
-- Name: alquileres id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquileres ALTER COLUMN id SET DEFAULT nextval('public.alquileres_id_seq'::regclass);


--
-- Name: asignaciones id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asignaciones ALTER COLUMN id SET DEFAULT nextval('public.asignaciones_id_seq'::regclass);


--
-- Name: auditoria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auditoria ALTER COLUMN id SET DEFAULT nextval('public.auditoria_id_seq'::regclass);


--
-- Name: campanias id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campanias ALTER COLUMN id SET DEFAULT nextval('public.campanias_id_seq'::regclass);


--
-- Name: categorias_repuesto id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categorias_repuesto ALTER COLUMN id SET DEFAULT nextval('public.categorias_repuesto_id_seq'::regclass);


--
-- Name: clientes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clientes ALTER COLUMN id SET DEFAULT nextval('public.clientes_id_seq'::regclass);


--
-- Name: contactos_cliente id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactos_cliente ALTER COLUMN id SET DEFAULT nextval('public.contactos_cliente_id_seq'::regclass);


--
-- Name: contratos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos ALTER COLUMN id SET DEFAULT nextval('public.contratos_id_seq'::regclass);


--
-- Name: cotizacion_detalles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_detalles ALTER COLUMN id SET DEFAULT nextval('public.cotizacion_detalles_id_seq'::regclass);


--
-- Name: cotizaciones id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizaciones ALTER COLUMN id SET DEFAULT nextval('public.cotizaciones_id_seq'::regclass);


--
-- Name: detalles_adquisicion id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalles_adquisicion ALTER COLUMN id SET DEFAULT nextval('public.detalles_adquisicion_id_seq'::regclass);


--
-- Name: documentos_empleado id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documentos_empleado ALTER COLUMN id SET DEFAULT nextval('public.documentos_empleado_id_seq'::regclass);


--
-- Name: empleados id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.empleados ALTER COLUMN id SET DEFAULT nextval('public.empleados_id_seq'::regclass);


--
-- Name: incidencias id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incidencias ALTER COLUMN id SET DEFAULT nextval('public.incidencias_id_seq'::regclass);


--
-- Name: inventario_maquinaria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventario_maquinaria ALTER COLUMN id SET DEFAULT nextval('public.inventario_maquinaria_id_seq'::regclass);


--
-- Name: leads id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leads ALTER COLUMN id SET DEFAULT nextval('public.leads_id_seq'::regclass);


--
-- Name: mantenimiento_repuestos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimiento_repuestos ALTER COLUMN id SET DEFAULT nextval('public.mantenimiento_repuestos_id_seq'::regclass);


--
-- Name: mantenimientos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos ALTER COLUMN id SET DEFAULT nextval('public.mantenimientos_id_seq'::regclass);


--
-- Name: maquinarias id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maquinarias ALTER COLUMN id SET DEFAULT nextval('public.maquinarias_id_seq'::regclass);


--
-- Name: movimientos_inventario id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario ALTER COLUMN id SET DEFAULT nextval('public.movimientos_inventario_id_seq'::regclass);


--
-- Name: notificaciones id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificaciones ALTER COLUMN id SET DEFAULT nextval('public.notificaciones_id_seq'::regclass);


--
-- Name: oportunidades id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades ALTER COLUMN id SET DEFAULT nextval('public.oportunidades_id_seq'::regclass);


--
-- Name: orden_compra_detalles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orden_compra_detalles ALTER COLUMN id SET DEFAULT nextval('public.orden_compra_detalles_id_seq'::regclass);


--
-- Name: ordenes_compra id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ordenes_compra ALTER COLUMN id SET DEFAULT nextval('public.ordenes_compra_id_seq'::regclass);


--
-- Name: pagos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pagos ALTER COLUMN id SET DEFAULT nextval('public.pagos_id_seq'::regclass);


--
-- Name: permisos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permisos ALTER COLUMN id SET DEFAULT nextval('public.permisos_id_seq'::regclass);


--
-- Name: planes_mantenimiento id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.planes_mantenimiento ALTER COLUMN id SET DEFAULT nextval('public.planes_mantenimiento_id_seq'::regclass);


--
-- Name: proveedores id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proveedores ALTER COLUMN id SET DEFAULT nextval('public.proveedores_id_seq'::regclass);


--
-- Name: recepcion_detalles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepcion_detalles ALTER COLUMN id SET DEFAULT nextval('public.recepcion_detalles_id_seq'::regclass);


--
-- Name: recepciones_compra id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepciones_compra ALTER COLUMN id SET DEFAULT nextval('public.recepciones_compra_id_seq'::regclass);


--
-- Name: repuestos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.repuestos ALTER COLUMN id SET DEFAULT nextval('public.repuestos_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: servicios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicios ALTER COLUMN id SET DEFAULT nextval('public.servicios_id_seq'::regclass);


--
-- Name: solicitudes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudes ALTER COLUMN id SET DEFAULT nextval('public.solicitudes_id_seq'::regclass);


--
-- Name: tarifas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tarifas ALTER COLUMN id SET DEFAULT nextval('public.tarifas_id_seq'::regclass);


--
-- Name: tipos_maquinaria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_maquinaria ALTER COLUMN id SET DEFAULT nextval('public.tipos_maquinaria_id_seq'::regclass);


--
-- Name: tipos_servicio id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_servicio ALTER COLUMN id SET DEFAULT nextval('public.tipos_servicio_id_seq'::regclass);


--
-- Name: ubicaciones_almacen id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_almacen ALTER COLUMN id SET DEFAULT nextval('public.ubicaciones_almacen_id_seq'::regclass);


--
-- Name: ubicaciones_gps id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_gps ALTER COLUMN id SET DEFAULT nextval('public.ubicaciones_gps_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Name: venta_detalles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_detalles ALTER COLUMN id SET DEFAULT nextval('public.venta_detalles_id_seq'::regclass);


--
-- Name: ventas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas ALTER COLUMN id SET DEFAULT nextval('public.ventas_id_seq'::regclass);


--
-- Name: adquisiciones_maquinaria adquisiciones_maquinaria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adquisiciones_maquinaria
    ADD CONSTRAINT adquisiciones_maquinaria_pkey PRIMARY KEY (id);


--
-- Name: almacenes almacenes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.almacenes
    ADD CONSTRAINT almacenes_pkey PRIMARY KEY (id);


--
-- Name: alquiler_detalles alquiler_detalles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquiler_detalles
    ADD CONSTRAINT alquiler_detalles_pkey PRIMARY KEY (id);


--
-- Name: alquileres alquileres_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquileres
    ADD CONSTRAINT alquileres_pkey PRIMARY KEY (id);


--
-- Name: asignaciones asignaciones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asignaciones
    ADD CONSTRAINT asignaciones_pkey PRIMARY KEY (id);


--
-- Name: auditoria auditoria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auditoria
    ADD CONSTRAINT auditoria_pkey PRIMARY KEY (id);


--
-- Name: campanias campanias_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campanias
    ADD CONSTRAINT campanias_pkey PRIMARY KEY (id);


--
-- Name: categorias_repuesto categorias_repuesto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categorias_repuesto
    ADD CONSTRAINT categorias_repuesto_pkey PRIMARY KEY (id);


--
-- Name: clientes clientes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_pkey PRIMARY KEY (id);


--
-- Name: contactos_cliente contactos_cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactos_cliente
    ADD CONSTRAINT contactos_cliente_pkey PRIMARY KEY (id);


--
-- Name: contratos contratos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_pkey PRIMARY KEY (id);


--
-- Name: cotizacion_detalles cotizacion_detalles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_detalles
    ADD CONSTRAINT cotizacion_detalles_pkey PRIMARY KEY (id);


--
-- Name: cotizaciones cotizaciones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizaciones
    ADD CONSTRAINT cotizaciones_pkey PRIMARY KEY (id);


--
-- Name: detalles_adquisicion detalles_adquisicion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalles_adquisicion
    ADD CONSTRAINT detalles_adquisicion_pkey PRIMARY KEY (id);


--
-- Name: documentos_empleado documentos_empleado_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documentos_empleado
    ADD CONSTRAINT documentos_empleado_pkey PRIMARY KEY (id);


--
-- Name: empleados empleados_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.empleados
    ADD CONSTRAINT empleados_pkey PRIMARY KEY (id);


--
-- Name: incidencias incidencias_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incidencias
    ADD CONSTRAINT incidencias_pkey PRIMARY KEY (id);


--
-- Name: inventario_maquinaria inventario_maquinaria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventario_maquinaria
    ADD CONSTRAINT inventario_maquinaria_pkey PRIMARY KEY (id);


--
-- Name: leads leads_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leads
    ADD CONSTRAINT leads_pkey PRIMARY KEY (id);


--
-- Name: mantenimiento_repuestos mantenimiento_repuestos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimiento_repuestos
    ADD CONSTRAINT mantenimiento_repuestos_pkey PRIMARY KEY (id);


--
-- Name: mantenimientos mantenimientos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT mantenimientos_pkey PRIMARY KEY (id);


--
-- Name: maquinarias maquinarias_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maquinarias
    ADD CONSTRAINT maquinarias_pkey PRIMARY KEY (id);


--
-- Name: movimientos_inventario movimientos_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT movimientos_inventario_pkey PRIMARY KEY (id);


--
-- Name: notificaciones notificaciones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificaciones
    ADD CONSTRAINT notificaciones_pkey PRIMARY KEY (id);


--
-- Name: oportunidades oportunidades_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades
    ADD CONSTRAINT oportunidades_pkey PRIMARY KEY (id);


--
-- Name: orden_compra_detalles orden_compra_detalles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orden_compra_detalles
    ADD CONSTRAINT orden_compra_detalles_pkey PRIMARY KEY (id);


--
-- Name: ordenes_compra ordenes_compra_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ordenes_compra
    ADD CONSTRAINT ordenes_compra_pkey PRIMARY KEY (id);


--
-- Name: pagos pagos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_pkey PRIMARY KEY (id);


--
-- Name: permisos permisos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permisos
    ADD CONSTRAINT permisos_pkey PRIMARY KEY (id);


--
-- Name: planes_mantenimiento planes_mantenimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.planes_mantenimiento
    ADD CONSTRAINT planes_mantenimiento_pkey PRIMARY KEY (id);


--
-- Name: proveedores proveedores_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proveedores
    ADD CONSTRAINT proveedores_pkey PRIMARY KEY (id);


--
-- Name: recepcion_detalles recepcion_detalles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepcion_detalles
    ADD CONSTRAINT recepcion_detalles_pkey PRIMARY KEY (id);


--
-- Name: recepciones_compra recepciones_compra_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepciones_compra
    ADD CONSTRAINT recepciones_compra_pkey PRIMARY KEY (id);


--
-- Name: repuestos repuestos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.repuestos
    ADD CONSTRAINT repuestos_pkey PRIMARY KEY (id);


--
-- Name: rol_permisos rol_permisos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol_permisos
    ADD CONSTRAINT rol_permisos_pkey PRIMARY KEY (permiso_id, rol_id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: servicios servicios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT servicios_pkey PRIMARY KEY (id);


--
-- Name: solicitudes solicitudes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudes
    ADD CONSTRAINT solicitudes_pkey PRIMARY KEY (id);


--
-- Name: tarifas tarifas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tarifas
    ADD CONSTRAINT tarifas_pkey PRIMARY KEY (id);


--
-- Name: tipos_maquinaria tipos_maquinaria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_maquinaria
    ADD CONSTRAINT tipos_maquinaria_pkey PRIMARY KEY (id);


--
-- Name: tipos_servicio tipos_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_servicio
    ADD CONSTRAINT tipos_servicio_pkey PRIMARY KEY (id);


--
-- Name: ubicaciones_almacen ubicaciones_almacen_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_almacen
    ADD CONSTRAINT ubicaciones_almacen_pkey PRIMARY KEY (id);


--
-- Name: ubicaciones_gps ubicaciones_gps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_gps
    ADD CONSTRAINT ubicaciones_gps_pkey PRIMARY KEY (id);


--
-- Name: recepciones_compra uk_12929rrch08x6yfaur3uk4a7x; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepciones_compra
    ADD CONSTRAINT uk_12929rrch08x6yfaur3uk4a7x UNIQUE (numero);


--
-- Name: tipos_maquinaria uk_1e4oavgtq6icv7xyxatxe6hda; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_maquinaria
    ADD CONSTRAINT uk_1e4oavgtq6icv7xyxatxe6hda UNIQUE (nombre);


--
-- Name: ventas uk_1ud2qhwp9bdpk3feb9hoop0jj; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT uk_1ud2qhwp9bdpk3feb9hoop0jj UNIQUE (numero);


--
-- Name: contratos uk_2v6fw88t3pg1ckdi5a5ky2w80; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT uk_2v6fw88t3pg1ckdi5a5ky2w80 UNIQUE (cotizacion_id);


--
-- Name: clientes uk_47fj0e5fbq2l3b80ij3rn1qss; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT uk_47fj0e5fbq2l3b80ij3rn1qss UNIQUE (documento);


--
-- Name: almacenes uk_4ilm0f1weyncwuyyikn7rrlf4; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.almacenes
    ADD CONSTRAINT uk_4ilm0f1weyncwuyyikn7rrlf4 UNIQUE (nombre);


--
-- Name: servicios uk_4rq09l9hp2dk3wy1ewok1goxo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT uk_4rq09l9hp2dk3wy1ewok1goxo UNIQUE (contrato_id);


--
-- Name: empleados uk_5l7j8378rxvcv2yq3kic8f17i; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.empleados
    ADD CONSTRAINT uk_5l7j8378rxvcv2yq3kic8f17i UNIQUE (documento);


--
-- Name: repuestos uk_5qbl67yw25ix3d8qkn2etsua0; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.repuestos
    ADD CONSTRAINT uk_5qbl67yw25ix3d8qkn2etsua0 UNIQUE (codigo);


--
-- Name: alquileres uk_5xqk8fhninycefmgxddyjbblt; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquileres
    ADD CONSTRAINT uk_5xqk8fhninycefmgxddyjbblt UNIQUE (contrato_id);


--
-- Name: cotizaciones uk_8r6qrgifd1obk2nfwotn4nigr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizaciones
    ADD CONSTRAINT uk_8r6qrgifd1obk2nfwotn4nigr UNIQUE (solicitud_id);


--
-- Name: clientes uk_96x1n5ut15ns1xhkm87y7qhnd; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT uk_96x1n5ut15ns1xhkm87y7qhnd UNIQUE (usuario_id);


--
-- Name: contratos uk_9d9mvis6h89l8frvo4k9ww8ca; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT uk_9d9mvis6h89l8frvo4k9ww8ca UNIQUE (numero);


--
-- Name: pagos uk_adgury1hgndcccjvxefg3ngw8; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT uk_adgury1hgndcccjvxefg3ngw8 UNIQUE (numero_recibo);


--
-- Name: maquinarias uk_ase3w2j7hj0o3vm5ls83b46as; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maquinarias
    ADD CONSTRAINT uk_ase3w2j7hj0o3vm5ls83b46as UNIQUE (placa);


--
-- Name: ordenes_compra uk_ci1e9cmymflmww3l7da6e05rr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ordenes_compra
    ADD CONSTRAINT uk_ci1e9cmymflmww3l7da6e05rr UNIQUE (numero);


--
-- Name: proveedores uk_ffj6y49mpe4t7pj6klbksla0t; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proveedores
    ADD CONSTRAINT uk_ffj6y49mpe4t7pj6klbksla0t UNIQUE (documento);


--
-- Name: cotizaciones uk_geapj7l30bi8adj45q0r4y5t5; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizaciones
    ADD CONSTRAINT uk_geapj7l30bi8adj45q0r4y5t5 UNIQUE (numero);


--
-- Name: inventario_maquinaria uk_gscv2nnykcq3skjstctf03u1q; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventario_maquinaria
    ADD CONSTRAINT uk_gscv2nnykcq3skjstctf03u1q UNIQUE (maquinaria_id);


--
-- Name: tipos_servicio uk_hlss2j38nfg2hepdjlu60hqs; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_servicio
    ADD CONSTRAINT uk_hlss2j38nfg2hepdjlu60hqs UNIQUE (nombre);


--
-- Name: ventas uk_jibwp26nqe13abacghb25ho94; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT uk_jibwp26nqe13abacghb25ho94 UNIQUE (contrato_id);


--
-- Name: usuarios uk_kfsp0s1tflm1cwlj8idhqsad0; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_kfsp0s1tflm1cwlj8idhqsad0 UNIQUE (email);


--
-- Name: categorias_repuesto uk_kwc34aip5v1n3xw21hxgvf79d; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categorias_repuesto
    ADD CONSTRAINT uk_kwc34aip5v1n3xw21hxgvf79d UNIQUE (nombre);


--
-- Name: roles uk_ldv0v52e0udsh2h1rs0r0gw1n; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_ldv0v52e0udsh2h1rs0r0gw1n UNIQUE (nombre);


--
-- Name: usuarios uk_m2dvbwfge291euvmk6vkkocao; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_m2dvbwfge291euvmk6vkkocao UNIQUE (username);


--
-- Name: detalles_adquisicion uk_mu4opb3qy8vl1fa6r9ys8vr2f; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalles_adquisicion
    ADD CONSTRAINT uk_mu4opb3qy8vl1fa6r9ys8vr2f UNIQUE (maquinaria_id);


--
-- Name: permisos uk_qd54p03hosdl6igbhtn1o1r3l; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permisos
    ADD CONSTRAINT uk_qd54p03hosdl6igbhtn1o1r3l UNIQUE (codigo);


--
-- Name: maquinarias uk_rf8exj00s90p5dremivksnuyh; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maquinarias
    ADD CONSTRAINT uk_rf8exj00s90p5dremivksnuyh UNIQUE (codigo_interno);


--
-- Name: empleados uk_sjpxfuol5uag54oxbayrfuuii; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.empleados
    ADD CONSTRAINT uk_sjpxfuol5uag54oxbayrfuuii UNIQUE (usuario_id);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: venta_detalles venta_detalles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_detalles
    ADD CONSTRAINT venta_detalles_pkey PRIMARY KEY (id);


--
-- Name: ventas ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_pkey PRIMARY KEY (id);


--
-- Name: idx_gps_maquinaria_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gps_maquinaria_fecha ON public.ubicaciones_gps USING btree (maquinaria_id, fecha_hora);


--
-- Name: idx_pagos_contrato; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pagos_contrato ON public.pagos USING btree (contrato_id);


--
-- Name: idx_pagos_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pagos_estado ON public.pagos USING btree (estado);


--
-- Name: ubicaciones_gps fk18xc801qk0ojy6stqhl1rn3tt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_gps
    ADD CONSTRAINT fk18xc801qk0ojy6stqhl1rn3tt FOREIGN KEY (servicio_id) REFERENCES public.servicios(id);


--
-- Name: notificaciones fk1mxbjb81ft61gwlh0kabubndc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificaciones
    ADD CONSTRAINT fk1mxbjb81ft61gwlh0kabubndc FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: contratos fk1pllgg7q558aedufre3y4pewp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk1pllgg7q558aedufre3y4pewp FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);


--
-- Name: adquisiciones_maquinaria fk1xa2rxtgb4d7vsn8x7gx8ggk7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adquisiciones_maquinaria
    ADD CONSTRAINT fk1xa2rxtgb4d7vsn8x7gx8ggk7 FOREIGN KEY (proveedor_id) REFERENCES public.proveedores(id);


--
-- Name: recepcion_detalles fk275l6frchu9orhjwtcsjg7xcc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepcion_detalles
    ADD CONSTRAINT fk275l6frchu9orhjwtcsjg7xcc FOREIGN KEY (recepcion_id) REFERENCES public.recepciones_compra(id);


--
-- Name: contratos fk2ehisqw470xm7mrq4chv4jai6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk2ehisqw470xm7mrq4chv4jai6 FOREIGN KEY (cotizacion_id) REFERENCES public.cotizaciones(id);


--
-- Name: detalles_adquisicion fk2jf795wv3ilmuodi16j6vkjdv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalles_adquisicion
    ADD CONSTRAINT fk2jf795wv3ilmuodi16j6vkjdv FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: tarifas fk2oiudm16957q3wnfu9eod253g; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tarifas
    ADD CONSTRAINT fk2oiudm16957q3wnfu9eod253g FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: venta_detalles fk2q7gao3u60i93vugwa6gjhin; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_detalles
    ADD CONSTRAINT fk2q7gao3u60i93vugwa6gjhin FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: servicios fk33gnfl7x58sxlxgoimod6jpwe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT fk33gnfl7x58sxlxgoimod6jpwe FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);


--
-- Name: cotizacion_detalles fk41qqs34d8u1fseahurpqu3rqx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_detalles
    ADD CONSTRAINT fk41qqs34d8u1fseahurpqu3rqx FOREIGN KEY (cotizacion_id) REFERENCES public.cotizaciones(id);


--
-- Name: documentos_empleado fk4m3eefke7ocxb5n7a5628a6p4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documentos_empleado
    ADD CONSTRAINT fk4m3eefke7ocxb5n7a5628a6p4 FOREIGN KEY (empleado_id) REFERENCES public.empleados(id);


--
-- Name: mantenimientos fk4q02h6lw0l69ta5v7kgnb3it0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT fk4q02h6lw0l69ta5v7kgnb3it0 FOREIGN KEY (incidencia_id) REFERENCES public.incidencias(id);


--
-- Name: recepciones_compra fk5cv94vfu025ny7c788x74bsjh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepciones_compra
    ADD CONSTRAINT fk5cv94vfu025ny7c788x74bsjh FOREIGN KEY (orden_compra_id) REFERENCES public.ordenes_compra(id);


--
-- Name: orden_compra_detalles fk5f3sv0q2fbt081kldr8sro2t2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orden_compra_detalles
    ADD CONSTRAINT fk5f3sv0q2fbt081kldr8sro2t2 FOREIGN KEY (orden_compra_id) REFERENCES public.ordenes_compra(id);


--
-- Name: incidencias fk5qjabc5ieau34qqfuhde7c2c0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incidencias
    ADD CONSTRAINT fk5qjabc5ieau34qqfuhde7c2c0 FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: movimientos_inventario fk6au5enah516i15i0eilxj50dr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT fk6au5enah516i15i0eilxj50dr FOREIGN KEY (repuesto_id) REFERENCES public.repuestos(id);


--
-- Name: contactos_cliente fk6fcl7aitk54um7iseylcfi2i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactos_cliente
    ADD CONSTRAINT fk6fcl7aitk54um7iseylcfi2i FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);


--
-- Name: detalles_adquisicion fk6hiuubjmqmhslqtinact9q3su; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalles_adquisicion
    ADD CONSTRAINT fk6hiuubjmqmhslqtinact9q3su FOREIGN KEY (adquisicion_id) REFERENCES public.adquisiciones_maquinaria(id);


--
-- Name: venta_detalles fk6oe5rpuq8yx6emqvsfb50nipb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_detalles
    ADD CONSTRAINT fk6oe5rpuq8yx6emqvsfb50nipb FOREIGN KEY (venta_id) REFERENCES public.ventas(id);


--
-- Name: oportunidades fk6swt0epy4lws95k2oynt67if9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades
    ADD CONSTRAINT fk6swt0epy4lws95k2oynt67if9 FOREIGN KEY (lead_id) REFERENCES public.leads(id);


--
-- Name: mantenimientos fk6vedjrf569kq2puq9xk3yb2ew; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT fk6vedjrf569kq2puq9xk3yb2ew FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: inventario_maquinaria fk7vdyjelmcdk5si108wbdam7mt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventario_maquinaria
    ADD CONSTRAINT fk7vdyjelmcdk5si108wbdam7mt FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: ordenes_compra fk7ximp03n72hmygxmikaapavac; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ordenes_compra
    ADD CONSTRAINT fk7ximp03n72hmygxmikaapavac FOREIGN KEY (proveedor_id) REFERENCES public.proveedores(id);


--
-- Name: asignaciones fk9osl4xbq0mt1n71gipm9r5m4x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asignaciones
    ADD CONSTRAINT fk9osl4xbq0mt1n71gipm9r5m4x FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: alquiler_detalles fk9rn27aaw2vko9aid4p7t68fio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquiler_detalles
    ADD CONSTRAINT fk9rn27aaw2vko9aid4p7t68fio FOREIGN KEY (alquiler_id) REFERENCES public.alquileres(id);


--
-- Name: mantenimiento_repuestos fk9tvbnrjbkson3sw9fdrvdthxb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimiento_repuestos
    ADD CONSTRAINT fk9tvbnrjbkson3sw9fdrvdthxb FOREIGN KEY (repuesto_id) REFERENCES public.repuestos(id);


--
-- Name: cotizaciones fk9w9etyi188ahl4515let655mq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizaciones
    ADD CONSTRAINT fk9w9etyi188ahl4515let655mq FOREIGN KEY (solicitud_id) REFERENCES public.solicitudes(id);


--
-- Name: rol_permisos fk_rol_permisos_permiso; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol_permisos
    ADD CONSTRAINT fk_rol_permisos_permiso FOREIGN KEY (permiso_id) REFERENCES public.permisos(id);


--
-- Name: rol_permisos fk_rol_permisos_rol; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol_permisos
    ADD CONSTRAINT fk_rol_permisos_rol FOREIGN KEY (rol_id) REFERENCES public.roles(id);


--
-- Name: usuarios fk_usuario_cliente; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);


--
-- Name: usuarios fk_usuarios_rol; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES public.roles(id);


--
-- Name: movimientos_inventario fkakkxu577al9vljuur2ahk0lo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT fkakkxu577al9vljuur2ahk0lo FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: asignaciones fkbgm9c362r4y28m9y2lf6giagf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asignaciones
    ADD CONSTRAINT fkbgm9c362r4y28m9y2lf6giagf FOREIGN KEY (empleado_id) REFERENCES public.empleados(id);


--
-- Name: orden_compra_detalles fkc3pbwu3fvtdghpfuit7mn9yc7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orden_compra_detalles
    ADD CONSTRAINT fkc3pbwu3fvtdghpfuit7mn9yc7 FOREIGN KEY (repuesto_id) REFERENCES public.repuestos(id);


--
-- Name: almacenes fkcc3ab1pq4aeuj7ahudhluwcmq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.almacenes
    ADD CONSTRAINT fkcc3ab1pq4aeuj7ahudhluwcmq FOREIGN KEY (responsable_id) REFERENCES public.empleados(id);


--
-- Name: cotizaciones fkcu4nr9ynn1dv7mac2qbci0co7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizaciones
    ADD CONSTRAINT fkcu4nr9ynn1dv7mac2qbci0co7 FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);


--
-- Name: alquileres fke5w4kg0m3862oa8mnre5xqdb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquileres
    ADD CONSTRAINT fke5w4kg0m3862oa8mnre5xqdb FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);


--
-- Name: servicios fkefe143sxtfk5s7y1pbyeh45ke; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT fkefe143sxtfk5s7y1pbyeh45ke FOREIGN KEY (tipo_servicio_id) REFERENCES public.tipos_servicio(id);


--
-- Name: mantenimiento_repuestos fkej9ik5hdii7vbx6ukc2bvvibn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimiento_repuestos
    ADD CONSTRAINT fkej9ik5hdii7vbx6ukc2bvvibn FOREIGN KEY (mantenimiento_id) REFERENCES public.mantenimientos(id);


--
-- Name: incidencias fketvhcad0mc504yr69cdl04mlp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incidencias
    ADD CONSTRAINT fketvhcad0mc504yr69cdl04mlp FOREIGN KEY (servicio_id) REFERENCES public.servicios(id);


--
-- Name: mantenimientos fkf1x47mttwnho05fuj6tmcn8gj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT fkf1x47mttwnho05fuj6tmcn8gj FOREIGN KEY (plan_id) REFERENCES public.planes_mantenimiento(id);


--
-- Name: empleados fkf5la9sef5mevf72dun7f2rxu4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.empleados
    ADD CONSTRAINT fkf5la9sef5mevf72dun7f2rxu4 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: ubicaciones_gps fkf73jyaiec22lwj03f8g03efdp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_gps
    ADD CONSTRAINT fkf73jyaiec22lwj03f8g03efdp FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: pagos fkfsgbuky3qe4emfg7adfd4md2l; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT fkfsgbuky3qe4emfg7adfd4md2l FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);


--
-- Name: recepciones_compra fkgx9ca0ds2ju1lwpbibxh6hpi2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepciones_compra
    ADD CONSTRAINT fkgx9ca0ds2ju1lwpbibxh6hpi2 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: repuestos fkh0ptf3iqfygro3116l48t446v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.repuestos
    ADD CONSTRAINT fkh0ptf3iqfygro3116l48t446v FOREIGN KEY (categoria_id) REFERENCES public.categorias_repuesto(id);


--
-- Name: alquiler_detalles fkim38w09phucvba2evqqmgfwtq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquiler_detalles
    ADD CONSTRAINT fkim38w09phucvba2evqqmgfwtq FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: mantenimientos fkivrg9q970l3csdi2op59kctvc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT fkivrg9q970l3csdi2op59kctvc FOREIGN KEY (empleado_id) REFERENCES public.empleados(id);


--
-- Name: auditoria fkj0mvp43e8kl8ec92f9u0q46g9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auditoria
    ADD CONSTRAINT fkj0mvp43e8kl8ec92f9u0q46g9 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: ubicaciones_almacen fkj6qv4xrc1v068wpd8gw4m7jts; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ubicaciones_almacen
    ADD CONSTRAINT fkj6qv4xrc1v068wpd8gw4m7jts FOREIGN KEY (almacen_id) REFERENCES public.almacenes(id);


--
-- Name: leads fkj7brsyrkdcxskamy7yrm1sdb0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leads
    ADD CONSTRAINT fkj7brsyrkdcxskamy7yrm1sdb0 FOREIGN KEY (campania_id) REFERENCES public.campanias(id);


--
-- Name: asignaciones fkja7b4xmo1bl6q1akbjmpq2ae4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.asignaciones
    ADD CONSTRAINT fkja7b4xmo1bl6q1akbjmpq2ae4 FOREIGN KEY (servicio_id) REFERENCES public.servicios(id);


--
-- Name: clientes fkk6iwsq3kts1bblivkjy6epajx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT fkk6iwsq3kts1bblivkjy6epajx FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: oportunidades fkk8aqlhcc741wj5kefsotd7gu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oportunidades
    ADD CONSTRAINT fkk8aqlhcc741wj5kefsotd7gu FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);


--
-- Name: inventario_maquinaria fkk9v6oj0175m6bytk2d0gg514s; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventario_maquinaria
    ADD CONSTRAINT fkk9v6oj0175m6bytk2d0gg514s FOREIGN KEY (ubicacion_id) REFERENCES public.ubicaciones_almacen(id);


--
-- Name: recepcion_detalles fkkb938h4xx38s7pnyhy2sm31c6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recepcion_detalles
    ADD CONSTRAINT fkkb938h4xx38s7pnyhy2sm31c6 FOREIGN KEY (repuesto_id) REFERENCES public.repuestos(id);


--
-- Name: leads fknlvy6aitvj3ftr6mkliatr7jn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leads
    ADD CONSTRAINT fknlvy6aitvj3ftr6mkliatr7jn FOREIGN KEY (servicio_id) REFERENCES public.maquinarias(id);


--
-- Name: movimientos_inventario fkp5cm5m8vbp7q7vr483bt3f78f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT fkp5cm5m8vbp7q7vr483bt3f78f FOREIGN KEY (ubicacion_id) REFERENCES public.ubicaciones_almacen(id);


--
-- Name: cotizacion_detalles fkq3sc4b0ornvrkxbql2a3jayxx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_detalles
    ADD CONSTRAINT fkq3sc4b0ornvrkxbql2a3jayxx FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: alquiler_detalles fkqmgiltnjkncubyioo1xhy1ew5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alquiler_detalles
    ADD CONSTRAINT fkqmgiltnjkncubyioo1xhy1ew5 FOREIGN KEY (tarifa_id) REFERENCES public.tarifas(id);


--
-- Name: maquinarias fkqt5fy8ntqsfdcugvdle1nrcni; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maquinarias
    ADD CONSTRAINT fkqt5fy8ntqsfdcugvdle1nrcni FOREIGN KEY (tipo_id) REFERENCES public.tipos_maquinaria(id);


--
-- Name: ventas fks99s1e0xixrts8rpnf2idlchu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT fks99s1e0xixrts8rpnf2idlchu FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);


--
-- Name: planes_mantenimiento fkschy0r4iguq7e9n66qpotjpa8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.planes_mantenimiento
    ADD CONSTRAINT fkschy0r4iguq7e9n66qpotjpa8 FOREIGN KEY (maquinaria_id) REFERENCES public.maquinarias(id);


--
-- Name: ordenes_compra fksyjkblv6bto1nvga26qyk5yal; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ordenes_compra
    ADD CONSTRAINT fksyjkblv6bto1nvga26qyk5yal FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: solicitudes fktfxgn25p8c3x41bkb43vpmfuu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudes
    ADD CONSTRAINT fktfxgn25p8c3x41bkb43vpmfuu FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);


--
--


