# Fuentes de datos

| Modulo | Fuente | Tipo | Estado v3.1 | Frecuencia | Credenciales | Uso |
|---|---|---|---|---|---|---|
| Tipo de cambio | BCRP API | OFICIAL_API | Activa con fallback | Diario/cache | No | Calculo tributario |
| HS/arancel | BD local Arancel 2022 | BD_LOCAL | Activa | Manual | No | Busqueda y sugerencias HS |
| HS/arancel | SUNAT/Aduanet | OFICIAL_WEB | Roadmap v3.3 | Programada | No | Validacion referencial |
| Permisos | VUCE web | OFICIAL_WEB | Roadmap v3.4 | Programada | No | Requisitos y entidades |
| RUC | Proveedor tercero con fuente SUNAT | TERCERO_API | Parcial | Bajo demanda | Segun proveedor | Validacion de empresa |
| RUC | Pendiente manual | PENDIENTE_VALIDACION | Activa | Bajo demanda | No | Continuidad cuando falla fuente |
| Riesgo | Motor ImportEase | ESTIMADO | Activa | Por operacion | No | Riesgo estimado / canal probable |
| PRE-DAM | Motor ImportEase | SIMULADO | Activa | Por operacion | No | Documento referencial |
| Observatorio HS | UN Comtrade API | OFICIAL_API/CACHE | Activa con cache referencial | Bajo demanda | `UN_COMTRADE_KEY` opcional | Inteligencia comercial por HS |
| Incoterms | Catalogo local basado en ICC 2020 | BD_LOCAL | Activa | Manual | No | Simulacion didactica de negociacion |

## Categorias comunes

- `OFICIAL_API`
- `OFICIAL_WEB`
- `TERCERO_API`
- `BD_LOCAL`
- `ESTIMADO`
- `SIMULADO`
- `FALLBACK`
- `CACHE`
- `MANUAL`
- `PENDIENTE_VALIDACION`

## Regla QA

Ningun fallback, simulacion o estimacion debe mostrarse como dato oficial.
