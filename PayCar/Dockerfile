FROM eclipse-temurin:17-jdk as build

# Copia todos los archivos al directorio /app en el contenedor
COPY . /app

# Establece el directorio de trabajo
WORKDIR /app

# Verifica la presencia del archivo mvnw y muestra su contenido
RUN ls -l /app

# Otorga permisos de ejecución al archivo mvnw y ejecuta el empaquetado
RUN chmod +x mvnw && ./mvnw package -DskipTests

# Mueve el archivo JAR generado al nombre app.jar
RUN mv -f target/*.jar app.jar

# Imagen final
FROM eclipse-temurin:17-jre
ARG PORT
ENV PORT=${PORT}

# Copia el archivo JAR desde la etapa de construcción
COPY --from=build /app/app.jar .

# Crea un usuario no root para ejecutar la aplicación
RUN useradd runtime
USER runtime

# Define el comando de entrada
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
