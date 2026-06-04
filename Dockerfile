FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
# CI debe compilar sin -DskipTests para ejecutar pruebas y validar cobertura
RUN mvn -B -ntp -DskipTests package
# Para produccion usar: RUN mvn -B -ntp package

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/Importease.war /usr/local/tomcat/webapps/importease.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
