# Tahap 1: build WAR dengan Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Tahap 2: jalankan di Tomcat 10.1
FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
# Konfigurasi DB lewat environment variable: DB_URL, DB_USER, DB_PASS, ADMIN_PASS
EXPOSE 8080
CMD ["catalina.sh", "run"]
