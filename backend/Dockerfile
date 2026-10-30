# 1) Build com Maven (dependências em camada própria para aproveitar o cache)
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# 2) Só o JRE para rodar: imagem bem menor que a do JDK + Maven
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
