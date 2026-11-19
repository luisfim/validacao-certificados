FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /projeto
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
COPY exemplos ./exemplos
RUN mvn -q package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends python3-opencv tesseract-ocr tesseract-ocr-por && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /projeto/target/validacao-certificados.jar app.jar
COPY scripts ./scripts
ENV APP_ADDRESS=0.0.0.0
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
