# Imagem base JDK 21
FROM openjdk:21-jdk-slim

# Diretório de trabalho
WORKDIR /app

# Copia arquivos necessários
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn .mvn

# Dá permissão de execução para o wrapper
RUN chmod +x mvnw

# Build da aplicação
RUN ./mvnw clean package -DskipTests

# Expõe a porta do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
