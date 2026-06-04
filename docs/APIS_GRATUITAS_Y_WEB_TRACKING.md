# APIs gratuitas y fuentes web para ImportEase

## Prioridad para la presentacion

1. BCRP API: tipo de cambio real sin API key.
2. UN Comtrade: observatorio HS con key gratuita/opcional y cache etiquetado.
3. SUNAT/Aduanet: consulta web oficial con scraping controlado, no masivo.
4. VUCE: fuente web oficial referencial para permisos y entidades.
5. ICC Incoterms: no API; catalogo local didactico con referencia a ICC.

## Que queda fuera de la demo actual

- DHL, FedEx, UPS y Maersk con credenciales pagadas o cuentas developer.
- Tracking logistico real por courier.
- Automatizacion productiva de VUCE o SUNAT sin permisos formales.

## Regla de confianza

Si una fuente no responde o no tiene credenciales, ImportEase debe mostrar `PENDIENTE_CREDENCIALES`, `CACHE`, `FALLBACK` o `PENDIENTE_VALIDACION`. Nunca debe inventar un dato real.
