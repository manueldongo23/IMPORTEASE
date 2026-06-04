# Modulos v3.2: Observatorio HS e Incoterms Lab

## Observatorio Comercial por HS

Pantalla: `/observatorio-hs.jsp`

API:

- `GET /api/observatorio/hs?codigo=`
- `GET /api/observatorio/top-origenes?codigo=`
- `GET /api/observatorio/tendencia?codigo=`
- `GET /api/observatorio/oportunidad?codigo=`

Fuente objetivo:

- UN Comtrade API, reporter Peru, flujo importaciones, sistema HS.

Regla de confianza:

- Con `UN_COMTRADE_KEY` y respuesta HTTP OK: `OFICIAL_API`.
- Sin key: `PENDIENTE_CREDENCIALES` y cache referencial etiquetado.
- Si existe respuesta previa guardada: `CACHE`.

## Incoterms Lab

Pantalla: `/incoterms-lab.jsp`

API:

- `GET /api/incoterms/listar`
- `GET /api/incoterms/comparar?base=FOB&contra=CIF`
- `POST /api/incoterms/simular`
- `POST /api/incoterms/guardar-decision`

Fuente:

- Catalogo local `incoterms_2020`, basado en resumen propio de Incoterms 2020.
- Fuente normativa referencial: ICC.
- No requiere API externa ni credenciales.

Regla de confianza:

- `BD_LOCAL` con confianza media-alta para didactica.
- Nunca se presenta como asesoria legal ni como texto oficial completo de ICC.
- El modulo explica impacto en costos, documentos y riesgo operativo.

## Variables de entorno

```text
UN_COMTRADE_KEY
```

## Mensaje de defensa para presentacion

ImportEase no solo calcula tributos. Ahora ayuda a decidir si conviene importar un HS y tambien ensena a negociar el Incoterm con impacto real en costos, flete, seguro y documentos. Si una fuente externa no tiene credenciales, el sistema lo dice; si un dato es didactico, lo etiqueta como referencial.
