FROM tomcat:9.0-jdk17-openjdk

# Copier directement le WAR
COPY target/ServiceExpress.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]