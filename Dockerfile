# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache de dependencias (solo se reconstruye si pom.xml cambia)
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B -q

# Compilar sin tests (se ejecutan aparte en CI)
COPY src ./src

# Generar config.properties mínimo (está en .gitignore, se sobreescribe con env vars en runtime)
RUN printf 'peru.api.token=${PERU_API_TOKEN}\nsmtp.host=${SMTP_HOST}\nsmtp.port=${SMTP_PORT}\nsmtp.username=${SMTP_USERNAME}\nsmtp.password=${SMTP_PASSWORD}\nsmtp.from=${SMTP_FROM}\nsmtp.starttls=true\nsmtp.ssl=false\napp.public.url=${APP_PUBLIC_URL}\n' > src/main/resources/config.properties

RUN mvn package -DskipTests -Pdev -B -q

# ── Stage 2: Runtime ───────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar el WAR construido
COPY --from=build /app/target/Importease.war app.war

# Puerto que Railway asignará dinámicamente
ENV PORT=8084
EXPOSE ${PORT}

# Arrancar Spring Boot con el puerto dinámico de Railway
CMD java -Xms256m -Xmx512m \
  -Djava.awt.headless=true \
  -Dserver.port=${PORT} \
  -jar app.war
