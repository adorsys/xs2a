FROM adorsys/openjdk-jre-base:8-minideb

MAINTAINER https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a

ENV SERVER_PORT 8080
ENV SPRING_PROFILES_ACTIVE mongo
ENV JAVA_OPTS -Xmx1024m
ENV JAVA_TOOL_OPTIONS -Xmx1024m

WORKDIR /opt/aspsp-mock-server

COPY ./target/aspsp-mock-server*jar /opt/aspsp-mock-server/aspsp-mock-server.jar

EXPOSE 8080

CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/aspsp-mock-server/aspsp-mock-server.jar
