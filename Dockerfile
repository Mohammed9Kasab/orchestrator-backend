FROM eclipse-temurin:11-jdk-jammy
RUN apt-get update && apt-get install -y dos2unix
WORKDIR /app

COPY .mvn/ .mvn
RUN find .mvn -type f -exec dos2unix {} \;

COPY mvnw ./
RUN dos2unix mvnw
COPY pom.xml sonar-project.properties ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=stage"]
