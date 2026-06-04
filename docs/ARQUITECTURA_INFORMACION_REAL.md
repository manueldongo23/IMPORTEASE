# Arquitectura de Informacion Real

ImportEase v3.5 separa datos oficiales, referenciales, cacheados, manuales y simulados para que el usuario no confunda una ayuda academica con una respuesta oficial.

## Fuentes

- BCRP API: tipo de cambio mediante API oficial.
- SUNAT/Aduanet: tratamiento arancelario y restricciones como fuente web referencial/cacheada.
- VUCE: permisos, entidades y requisitos como matriz referencial.
- UN Comtrade: estadisticas comerciales por HS con API o cache.
- DHL/FedEx/UPS/Maersk: tracking solo cuando existan credenciales o respuesta verificable.
- Incoterms 2020: catalogo local documentado.

## Metodos

- `API_OFICIAL`: endpoint estructurado de institucion o plataforma.
- `WEB_SCRAPING`: consulta publica controlada, cacheada y de baja frecuencia.
- `WEB_TRACKING`: consulta por tracking number, BL o contenedor.
- `BD_LOCAL`: matriz interna documentada.
- `MANUAL`: dato ingresado por usuario y marcado como no oficial.

## Trazabilidad

Cada consulta relevante debe generar un evento en `fuente_eventos` con fuente, URL, metodo HTTP, estado, resultado, hash del contenido, source type, confianza y fecha.

## Politica de fallback

Si una fuente no responde, ImportEase puede usar cache o fallback, pero debe mostrarlo. Ningun dato simulado, manual o pendiente de credenciales debe mostrarse como oficial.
