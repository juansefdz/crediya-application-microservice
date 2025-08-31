# --- ETAPA 1: Build con Gradle ---
# Usamos una imagen oficial de Gradle con JDK 17 para compilar la aplicación.
FROM gradle:8.5.0-jdk17 AS build

# Establecemos el directorio de trabajo dentro del contenedor.
WORKDIR /app

# --- IMPORTANTE: Elige la opción que coincida con tu proyecto ---
# Opción 1: Si tus archivos se llaman 'build.gradle' y 'settings.gradle' (Groovy DSL)
COPY build.gradle settings.gradle main.gradle ./

# Opción 2: Si tus archivos se llaman 'build.gradle.kts' y 'settings.gradle.kts' (Kotlin DSL)
# Descomenta la siguiente línea y comenta la de arriba si usas Kotlin.
# COPY build.gradle.kts settings.gradle.kts ./

# Copiamos el wrapper de Gradle para usar la versión exacta del proyecto.
COPY gradlew ./
COPY gradle ./gradle

# Descargamos las dependencias para aprovechar la caché de Docker.
# Si los archivos de build no cambian, esta capa no se volverá a ejecutar.
RUN ./gradlew dependencies --no-daemon

# Copiamos el resto del código fuente del proyecto.
COPY . .

# Compilamos la aplicación, empaquetamos el JAR y excluimos tareas innecesarias.
RUN ./gradlew build --no-daemon -x test -x validateStructure


# --- ETAPA 2: Run ---
# Usamos una imagen base mucho más ligera, solo con el JRE necesario para ejecutar.
# Esto reduce el tamaño final de la imagen y la superficie de ataque.
FROM eclipse-temurin:17-jre-jammy

# Establecemos el directorio de trabajo.
WORKDIR /app

# Exponemos el puerto en el que corre nuestra aplicación (definido en application.yml).
EXPOSE 8081

# Copiamos ÚNICAMENTE el JAR compilado desde la etapa "build".
# La ruta 'applications/app-service/' es estándar para el scaffold de Bancolombia.
# Si tu módulo principal tiene otro nombre, ajústalo aquí.
COPY --from=build /app/applications/app-service/build/libs/*.jar app.jar

# Comando para ejecutar la aplicación cuando se inicie el contenedor.
ENTRYPOINT ["java", "-jar", "app.jar"]