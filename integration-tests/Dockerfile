FROM adorsys/openjdk-jre-base:8-minideb

MAINTAINER https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a

ENV JAVA_OPTS -Xmx1024m -Dserver.port=8080
ENV JAVA_TOOL_OPTIONS -Xmx1024m

WORKDIR /opt/xs2a-integtests

COPY ./target/xs2a-integtests*jar /opt/aspsp-xs2a/xs2a-integtests.jar

EXPOSE 8080

CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/xs2a-integtests/xs2a-integtests.jar
