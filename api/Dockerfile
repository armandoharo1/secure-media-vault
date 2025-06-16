# Usa la imagen oficial de Gradle para construir el proyecto
FROM gradle:8.4.0-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Usa una imagen liviana de JDK para correr la app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia el JAR construido desde el paso anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Puerto en el que correrá la app
EXPOSE 8080

# Ejecuta la app
ENTRYPOINT ["java", "-jar", "app.jar"]
