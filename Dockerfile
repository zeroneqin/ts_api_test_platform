FROM metersphere/fabric8-java-alpine-openjdk8-jre

ARG MS_VERSION=dev

RUN mkdir -p /opt/apps && mkdir -p /opt/jmeter/lib/junit

COPY backend/target/backend-1.0.jar /opt/apps

COPY backend/target/classes/jmeter/ /opt/jmeter/

ENV JAVA_APP_JAR=/opt/apps/backend-1.0.jar

ENV AB_OFF=true

ENV MS_VERSION=${MS_VERSION}

ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true -Xms8g -Xmx8g"
CMD ["/deployments/run-java.sh"]
