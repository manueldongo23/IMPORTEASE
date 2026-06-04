# ImportEase Aduanero

Sistema academico/prototipo avanzado para gestion de importaciones: busqueda HS, permisos, costos, expediente aduanero, documentos, seguimiento y recuperacion segura de contrasena.

## Estado de esta version

Esta version fue limpiada y endurecida para demo tecnica:

- Recuperacion de contrasena con token persistido en base de datos.
- Token guardado solo como hash SHA-256.
- Token con expiracion de 15 minutos y uso unico.
- Respuesta generica en recuperacion para evitar enumeracion de usuarios.
- El enlace de recuperacion ya no se devuelve por JSON ni se registra en logs.
- Rate limit basico por IP y usuario.
- Variables de entorno para SMTP y base de datos.
- Docker Compose con MySQL y MailHog para demo local sin Gmail real.
- Limpieza de carpetas generadas: `target/`, `node_modules/`, logs y archivos locales quedan fuera del ZIP final.

## Requisitos para ejecucion manual

- Java 17
- Maven 3.9+
- MySQL 8+

## Ejecucion recomendada con Docker

```bash
docker compose up --build
```

URLs locales:

```text
Aplicacion: http://localhost:8082/importease
MailHog:    http://localhost:8025
MySQL:      localhost:3306
```

MailHog captura los correos de recuperacion en local. No necesitas usar Gmail para la demo.

## Ejecucion manual con Maven/Cargo

1. Crear base de datos y usuario:

```sql
CREATE DATABASE IF NOT EXISTS importease_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'importease_app'@'localhost' IDENTIFIED BY 'importease_dev';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON importease_db.* TO 'importease_app'@'localhost';
FLUSH PRIVILEGES;
```

2. Ejecutar scripts SQL:

```bash
mysql -u root -p < sql/importease_full_schema.sql
mysql -u root -p < sql/seed_normativa.sql
mysql -u root -p < sql/upgrade_v4.0_flujo_guiado.sql
mysql -u root -p < sql/upgrade_v4.1_normativa.sql
mysql -u root -p < sql/upgrade_v5.0_fuentes_reales.sql
mysql -u root -p < sql/upgrade_v5.1_password_reset.sql
```

3. Configurar variables de entorno. Puedes tomar como base `.env.example`.

4. Ejecutar:

```bash
mvn clean package cargo:run
```

URL local habitual:

```text
http://localhost:8082/importease
```

## Variables de entorno

```text
DB_URL
DB_USER
DB_PASSWORD
SMTP_HOST
SMTP_PORT
SMTP_USERNAME
SMTP_PASSWORD
SMTP_FROM
SMTP_STARTTLS
SMTP_SSL
APP_PUBLIC_URL
PERU_API_TOKEN
UN_COMTRADE_KEY
```

Ejemplo para MailHog local:

```env
DB_URL=jdbc:mysql://localhost:3306/importease_db?useSSL=false&serverTimezone=America/Lima
DB_USER=importease_app
DB_PASSWORD=importease_dev
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_FROM=no-reply@importease.local
SMTP_STARTTLS=false
SMTP_SSL=false
APP_PUBLIC_URL=http://localhost:8082/importease
```

Ejemplo para Gmail en pruebas controladas:

```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=tu_correo@gmail.com
SMTP_PASSWORD=CONTRASENA_DE_APLICACION_NUEVA
SMTP_FROM=tu_correo@gmail.com
SMTP_STARTTLS=true
SMTP_SSL=false
APP_PUBLIC_URL=http://localhost:8082/importease
```

No subas contrasenas reales a GitHub, ZIP, README ni capturas. Usa variables de entorno o secretos del servidor.

## Credenciales demo

```text
Email: md2023076842@virtual.upt.pe
Clave: Demo1234!

Email: importador.demo@importease.local
Clave: Demo1234!
```

## Probar recuperacion de contrasena en demo local

1. Levanta el sistema:

```bash
docker compose up --build
```

2. Entra a:

```text
http://localhost:8082/importease/recuperar.jsp
```

3. Ingresa un correo registrado.
4. Abre MailHog:

```text
http://localhost:8025
```

5. Abre el correo recibido y usa el enlace de recuperacion.

Por seguridad, el sistema no muestra el enlace en la respuesta del navegador.

## Archivos importantes

```text
src/main/java/com/importease/proyecto/service/login/PasswordResetService.java
src/main/java/com/importease/proyecto/repository/PasswordResetTokenDAO.java
sql/upgrade_v5.1_password_reset.sql
.env.example
docker-compose.yml
Dockerfile
```

## Checklist de seguridad aplicado

- [x] No devolver `resetUrl` en `/api/usuario/recuperar`.
- [x] No revelar si el correo existe.
- [x] No imprimir tokens ni links en logs.
- [x] Token persistido en BD como hash.
- [x] Token de un solo uso.
- [x] Expiracion de 15 minutos.
- [x] Rate limit basico.
- [x] Paginas de recuperacion publicas sin sesion.
- [x] Headers de seguridad reforzados.
- [x] Cookies compatibles con demo HTTP y Secure automatico en HTTPS.
