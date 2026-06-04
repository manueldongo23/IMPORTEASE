# Checklist QA v3.1

## Preparacion

- [ ] Backup creado: `backup_importease_pre_v3_1.sql`.
- [ ] Migracion `sql/upgrade_v3.1_trazabilidad_base.sql` ejecutada.
- [ ] Rollback disponible: `sql/rollback_v3.1_trazabilidad_base.sql`.
- [ ] Tomcat levanta en `8082`.
- [ ] URL base responde: `http://localhost:8082/importease`.

## Permisos

- [ ] `GET /api/permisos/listar` responde.
- [ ] `GET /api/permiso/listar` responde como alias.
- [ ] Alias devuelve headers `X-Deprecated-Endpoint` y `X-New-Endpoint`.
- [ ] Evaluar permiso por operacion propia.
- [ ] Guardar respuestas.
- [ ] Autorrellenar expediente.
- [ ] Registrar SUCE.
- [ ] Descargar PDF con id valido.
- [ ] PDF no descarga con id de otro usuario.

## Tendencias

- [ ] `POST /api/tendencias/registrar` guarda evento.
- [ ] Rechaza termino vacio.
- [ ] Sanitiza HTML.
- [ ] `GET /api/tendencias/top-productos` devuelve lista ordenada.
- [ ] `GET /api/tendencias/conversion` no rompe si no hay datos.
- [ ] La busqueda "celular" aparece en tendencias.

## Buscador HS

- [ ] Buscar `celular`.
- [ ] Buscar `laptop`.
- [ ] Buscar texto vacio.
- [ ] Buscar caracteres especiales.
- [ ] Buscar producto inexistente.
- [ ] Seleccionar HS.
- [ ] Mostrar fuente/confianza.
- [ ] No hay mojibake visible.

## Wizard/costos

- [ ] Tipo de cambio OK muestra fuente/confianza.
- [ ] Tipo de cambio fallback muestra contingencia.
- [ ] FOB menor a 200.
- [ ] FOB menor a 2000.
- [ ] Uso comercial.
- [ ] Operacion guardada.
- [ ] Riesgo dice estimado, no canal oficial SUNAT.

## Incoterms Lab

- [ ] `/incoterms-lab.jsp` carga desde el sidebar.
- [ ] `GET /api/incoterms/listar` devuelve catalogo.
- [ ] `POST /api/incoterms/simular` calcula FOB vs CIF con fuente `ICC_2020_REFERENCIAL`.
- [ ] Caso "No estoy seguro" recomienda un Incoterm y justifica.
- [ ] `Usar este Incoterm en mi operacion` vuelve al wizard paso 4.
- [ ] El wizard refleja flete/seguro incluidos segun Incoterm.

## Observatorio HS

- [ ] `/observatorio-hs.jsp` carga desde el sidebar.
- [ ] `GET /api/observatorio/hs?codigo=8517130000` responde con cache o fuente UN Comtrade.
- [ ] Si falta `UN_COMTRADE_KEY`, muestra `PENDIENTE_CREDENCIALES` o `CACHE`, no dato oficial falso.

## Seguridad

- [ ] Sesion requerida en endpoints privados.
- [ ] CSRF en POST criticos.
- [ ] IDOR en permisos, documentos e historial.
- [ ] XSS en buscador.
- [ ] No se expone stacktrace al usuario.
- [ ] No se guardan IP cruda ni user-agent crudo.

## Build

- [ ] `mvn test` pasa.
- [ ] `mvn -DskipTests package` pasa.
- [ ] No hay 404 en consola del navegador.
- [ ] README documenta comandos, URL y scripts.
