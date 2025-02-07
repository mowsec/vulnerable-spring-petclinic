#!/bin/bash


# Check if Java & mvn are installed
command -v "java" >/dev/null 2>&1 || { echo "Error: java is not installed."; exit 1; }
command -v "mvn" >/dev/null 2>&1 || { echo "Error: mvn is not installed."; exit 1; }

if [[ -n "$MYSQL_USER" ]] && [[ -n "$MYSQL_PASSWORD" ]]; then
  echo "Both MYSQL_USER and MYSQL_PASSWORD are set."
else
  echo "Either MYSQL_USER or MYSQL_PASSWORD is not set."
  exit 1
fi


# Download Contrast agent if not already downloaded
WORKDIR=$(pwd) #Change this if something different is needed
AGENT_VERSION=$(curl -sI "https://download.java.contrastsecurity.com/latest/" | grep -i location | sed -E 's/.*-(.*)\.jar/\1/' | tr -d '\r')
echo "Latest Contrast agent version: $AGENT_VERSION"

AGENT_FILE="$WORKDIR/contrast-agent-$AGENT_VERSION.jar"
if [ -f "$AGENT_FILE" ]; then
  echo "Latest contrast agent already downloaded."
else
  echo "Downloading Contrast agent."
  curl -Ls "https://download.java.contrastsecurity.com/latest/" -o "$AGENT_FILE"
fi
echo "Agent file: $AGENT_FILE"

# make sure apps aren't running still clean log files
killall java  || true 
rm /tmp/application.log||true
rm -rf /tmp/tomcat*
rm -rf /tmp/webapptmp||true
mkdir /tmp/webapptmp||true
rm emailservice.log||true
rm webapp.log||true

# Build and run JARs
echo "Building Email Service."
cd EmailService && mvn clean package -DskipTests=true || { echo "Error: Maven build failed."; exit 1; }
cd ..

echo "Building WebApplication Service."
cd WebApplication && mvn clean package -DskipTests=true || { echo "Error: Maven build failed."; exit 1; }
cd ..

echo "Running Email Service"
java -javaagent:"$AGENT_FILE" -Dcom.sun.jndi.ldap.object.trustURLCodebase=true -Dspring.profiles.active=mysql -jar EmailService/target/EmailService-1.0.0-SNAPSHOT.jar &> "$WORKDIR/emailservice.log" &
sleep(30)
echo "Running WebApplication Service"
java -javaagent:"$AGENT_FILE" -Dcom.sun.jndi.ldap.object.trustURLCodebase=true -Djava.io.tmpdir=/tmp/webapptmp -Dcontrast.agent.security_logger.path=$WORKDIR/.contrast-webapp/security.log -Dcontrast.application.name=adr-petclinic-webapp -Dspring.profiles.active=mysql -jar WebApplication/target/WebApplication-3.1.0-SNAPSHOT.jar &> "$WORKDIR/webapp.log" &

