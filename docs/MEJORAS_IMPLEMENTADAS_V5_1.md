# Mejoras implementadas v5.1

## Recuperacion de contrasena

- Se elimino la exposicion del enlace de recuperacion en la respuesta JSON.
- La respuesta publica ahora es generica: no revela si el correo existe.
- Se incorporo `PasswordResetTokenDAO` para persistir tokens en `password_reset_tokens`.
- El token se genera con `SecureRandom` y se guarda solo como SHA-256.
- Los tokens vencen en 15 minutos.
- Los tokens se marcan como usados despues de un reset exitoso.
- Antes de crear un nuevo token se invalidan los tokens activos anteriores del usuario.
- Se agrego rate limit basico por IP y por usuario.
- Se eliminaron logs que mostraban el enlace/token.

## Seguridad general

- `recuperar.jsp` y `resetear-clave.jsp` quedaron publicos para usuarios sin sesion.
- `/api/usuario/recuperar` y `/api/usuario/resetear` siguen publicos, pero con respuesta segura.
- `/api/tendencias/*` ya no queda abierto completo; solo rutas publicas especificas.
- HSTS ahora solo se envia cuando la solicitud llega por HTTPS.
- Las cookies agregan `SameSite=Lax`; `Secure` se agrega dinamicamente bajo HTTPS.
- CSP fue reforzado con `base-uri`, `object-src` y `form-action`.

## Entorno local y despliegue

- Se agrego `docker-compose.yml` con MySQL + MailHog + app.
- Se agrego Dockerfile multi-stage para compilar y desplegar WAR en Tomcat 9.
- Se agrego `.env.example`.
- Se agrego `.dockerignore`.
- Se limpio `.gitignore` para excluir build, logs, secretos, agentes y dependencias generadas.

## Base de datos

- Se agrego `sql/upgrade_v5.1_password_reset.sql`.
- Se reemplazo el correo demo personal por `importador.demo@importease.local`.

## Limitaciones pendientes

- El sistema conserva arquitectura Servlet/JSP por compatibilidad. La migracion total a controladores Spring MVC modulares sigue siendo una fase posterior.
- No se ejecuto `mvn verify` dentro de este entorno porque Maven no esta instalado fuera del Dockerfile.
- La compilacion sintactica de los archivos modificados fue validada con `javac` usando las dependencias ya empaquetadas en el proyecto.
