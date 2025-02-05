FROM adoptopenjdk/maven-openjdk8 AS builder
ADD EmailService/dummy-data /emailservice-src/dummy-data
ADD EmailService/pom.xml /emailservice-src/pom.xml
ADD EmailService/src /emailservice-src/src
ADD EmailService/mvnw /emailservice-src/mvnw
ADD ./contrast_security.yaml /emailservice-src/contrast_security.yaml

WORKDIR /emailservice-src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests=true


FROM adoptopenjdk/openjdk8

RUN mkdir -p /etc/contrast/java/
RUN mkdir -p /root/.ssh/
RUN apt-get update && apt-get install -y net-tools iputils-ping
COPY --from=builder /emailservice-src/dummy-data/* /root/.ssh/
COPY --from=builder /emailservice-src/target/EmailService-1.0.0-SNAPSHOT.jar /app.jar
COPY --from=builder /emailservice-src/contrast_security.yaml /etc/contrast/java/contrast_security.yaml

RUN rm -f /tmp/application.log || true
RUN rm -rf /tmp/tomcat* || true

# Install socat, used for reverse shell
RUN apt-get update && apt-get --assume-yes install socat
RUN curl -Ls "https://download.java.contrastsecurity.com/latest/" -o /contrast.jar

# Expose port 8081 from the container
EXPOSE 8081

ENTRYPOINT ["bash", "-c", "rm -f /tmp/application.log || true && rm -rf /tmp/tomcat* || true && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -javaagent:/contrast.jar -Dcom.sun.jndi.ldap.object.trustURLCodebase=true -jar /app.jar"]









