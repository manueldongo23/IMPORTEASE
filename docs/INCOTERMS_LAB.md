# Incoterms Lab

## Objetivo

Convertir Incoterms en una decision practica dentro de ImportEase.

El usuario no ve una tabla teorica primero. Simula una oferta del proveedor, entiende quien paga flete y seguro, revisa documentos necesarios y puede llevar la decision al wizard de costos.

## Flujo de demo

1. Entrar a `evaluacion.jsp`.
2. Ir al paso de costos.
3. Abrir `Incoterms Lab`.
4. Probar un caso: CIF Callao, courier, evitar sorpresas o no estoy seguro.
5. Revisar base CIF referencial, semaforo y checklist documental.
6. Pulsar `Usar este Incoterm en mi operacion`.
7. Confirmar que el wizard vuelve al paso 4 con el Incoterm y costos aplicados.

## Fuente y confianza

- Fuente UI: `ICC_2020_REFERENCIAL`.
- Tipo: `BD_LOCAL`.
- Confianza: calculada por `DataConfidenceService`.
- No se copia texto oficial extenso de ICC.
- No reemplaza asesoria legal, agente de aduanas ni contrato de compraventa.

## Reglas de calculo

- FOB/EXW/FCA: se suma flete y seguro para estimar CIF.
- CFR/CPT/DAP/DDP: se considera flete incluido; se revisa seguro por separado.
- CIF/CIP: se considera flete y seguro incluidos en la oferta.
- DDP: se marca como riesgo alto si no existe validacion local de tributos y despacho en Peru.

## APIs necesarias

No requiere API externa. Se apoya en tabla local `incoterms_2020`.

APIs gratuitas o referenciales recomendadas para el resto del sistema:

- BCRP API para tipo de cambio.
- UN Comtrade API para observatorio HS.
- SUNAT/Aduanet via consulta web controlada para arancel y restricciones.
- VUCE como fuente web referencial para requisitos y entidades.
