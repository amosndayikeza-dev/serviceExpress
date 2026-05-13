FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN echo "=== FICHIERS DANS /app ==="
RUN ls -la
RUN echo "=== FICHIERS DANS src/main/java ==="
RUN ls -la src/main/java/ || echo "Pas de fichier Java!"
RUN echo "=== COMPILATION MAVEN ==="
RUN mvn clean package -X

FROM tomcat:9.0-jdk17-openjdk
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
RUN echo "=== WAR COPIÉ ==="
RUN ls -la /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]