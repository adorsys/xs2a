FROM adorsys/java:11
LABEL maintainer="https://github.com/adorsys/xs2a"

ENV SERVER_PORT 8092
ENV JAVA_OPTS -Xmx1024m
ENV JAVA_TOOL_OPTIONS -Xmx1024m -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n

WORKDIR /opt/certificate-generator

COPY ./target/certificate-generator*jar /opt/certificate-generator/certificate-generator.jar

EXPOSE 8092 8000
# hadolint ignore=DL3025
CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/certificate-generator/certificate-generator.jar
