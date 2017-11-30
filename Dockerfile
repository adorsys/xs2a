FROM adorsys/openjdk-jre-base:8-minideb

MAINTAINER https://git.adorsys.de/adorsys/multibanking-xs2a

ENV JAVA_OPTS -Xmx1024m

WORKDIR /opt/multibanking-xs2a

COPY ./target/multibanking-xs2a.jar /opt/multibanking-xs2a/multibanking-xs2a.jar

EXPOSE 8080

CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/multibanking-xs2a/multibanking-xs2a.jar
