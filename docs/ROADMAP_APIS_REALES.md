# Roadmap de APIs reales

## v3.2 BCRP robusto

Objetivo: tipo de cambio real, cacheado y trazable.

- API BCRP.
- Cache en `tipo_cambio_diario`.
- Reintentos controlados.
- Circuit breaker.
- Eventos en `fuente_eventos`.
- UI con estado OK/CACHE/FALLBACK.

## v3.3 SUNAT / arancel / restricciones

Objetivo: actualizar arancel, subpartidas y restricciones con trazabilidad.

- Scraping controlado.
- Normalizador HTML.
- Historial de cambios.
- Fecha de vigencia.
- `fuente_url`.
- Marcado referencial cuando aplique.

## v3.4 VUCE / TUPA

Objetivo: permisos mas reales por entidad, producto y subpartida.

- Catalogo de entidades.
- Procedimientos.
- Requisitos.
- Costos.
- Plazos.
- Fuente URL y fecha de revision.

## v3.5 Web tracking referencial

Objetivo: no depender de APIs pagadas para la demo. Primero se plantea una version manual/verificable:

- Registro manual de BL, guia o contenedor.
- Campo de URL publica de consulta del operador.
- Evidencia documental cargada por el usuario.
- Estado `MANUAL_VERIFICADO` o `PENDIENTE_CREDENCIALES`.
- Integraciones DHL/FedEx/UPS/Maersk quedan fuera de la presentacion actual.

## v3.6 UN Comtrade

Objetivo: inteligencia comercial por HS.

- Importaciones de Peru por HS.
- Paises proveedores.
- Tendencia anual.
- Valor y cantidad.
- Precio unitario promedio.
- Riesgo de concentracion.
- Score de oportunidad comercial.
