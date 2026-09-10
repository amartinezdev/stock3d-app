# =====================================================================
# Imagen de la API. Construcción en dos etapas (multi-stage):
#
#   1. "build": trae el JDK completo y Maven para compilar el .jar.
#   2. Imagen final: solo el JRE y el .jar ya construido.
#
# Así la imagen que se despliega no lleva ni el compilador, ni el código
# fuente, ni el repositorio de dependencias de Maven: pesa mucho menos y
# tiene menos superficie de ataque.
# =====================================================================

FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# El wrapper y el pom.xml se copian ANTES que el código: mientras no
# cambien las dependencias, Docker reutiliza la capa de descarga en cada
# build en vez de bajarse medio Maven Central otra vez.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# El wrapper viene de Windows y puede llevar saltos de línea CRLF, que en
# Linux dan el clásico "bad interpreter: no such file or directory".
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src ./src
# Los tests ya se ejecutan en local y en CI; repetirlos aquí solo alargaría
# el despliegue y exigiría una base de datos dentro del contenedor.
RUN ./mvnw -B clean package -DskipTests

# ---------------------------------------------------------------------

FROM eclipse-temurin:25-jre

WORKDIR /app

# Un usuario sin privilegios: si alguien lograra ejecutar algo dentro del
# contenedor, no sería root.
RUN useradd --system --create-home --shell /usr/sbin/nologin stock
USER stock

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# La configuración (base de datos, secreto JWT, perfil) entra por variables
# de entorno, nunca horneada en la imagen: la misma imagen sirve para
# cualquier entorno.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
