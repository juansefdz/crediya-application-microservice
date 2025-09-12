FROM gradle:8.5.0-jdk17 AS build
WORKDIR /app

# Copiar archivos de configuración
COPY build.gradle settings.gradle main.gradle ./

# Copiar directorio gradle (necesario para gradle wrapper properties)
COPY gradle ./gradle

# Descargar dependencias usando gradle directamente
RUN gradle dependencies --no-daemon

# Copiar el código fuente
COPY . .

# Construir la aplicación usando gradle directamente
RUN gradle build --no-daemon -x test -x validateStructure

# Etapa de runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/applications/app-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]