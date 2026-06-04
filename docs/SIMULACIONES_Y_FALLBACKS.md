# Simulaciones y fallbacks

Este documento evita que el prototipo parezca mas oficial de lo que es.

## RUC

Si la fuente externa no responde, ImportEase no inventa una razon social comercial. El usuario puede continuar con:

- Fuente: `PENDIENTE_VALIDACION`
- Estado: `NO_VALIDADO`
- Confianza baja

## Tipo de cambio

Flujo esperado:

1. BCRP responde OK: `OFICIAL_API`.
2. Existe valor reciente en BD: `CACHE`.
3. No hay fuente disponible: `FALLBACK`.

Cuando se usa fallback, la UI debe decir: valor referencial usado por contingencia.

## HS

- Si existe en arancel local: `BD_LOCAL`.
- Si en el futuro viene de SUNAT/Aduanet: `OFICIAL_WEB`.
- Si se infiere por heuristica: `ESTIMADO`.
- Si no hay certeza: `PENDIENTE_VALIDACION`.

## Riesgo y canal

El sistema no asigna canal SUNAT. Solo informa:

- Riesgo estimado: bajo, medio o alto.
- Canal probable: verde, naranja o rojo.
- Advertencia: no representa canal oficial SUNAT.

## PRE-DAM

La PRE-DAM es referencial. No debe presentarse como DAM oficial ni como documento emitido por SUNAT.

## Incoterms Lab

El simulador de Incoterms es didactico y referencial. Usa resumen local basado en Incoterms 2020 y debe mostrarse como `ICC_2020_REFERENCIAL` / `BD_LOCAL`.

No reemplaza contrato de compraventa, asesoria legal ni validacion con agente de aduanas.

## Recuperacion de clave

Si el correo esta simulado en ambiente local, debe indicarse como simulacion de desarrollo. Para produccion se requiere SMTP real y tokens persistentes single-use.
