FROM adorsys/openjdk-jre-base:8-minideb

MAINTAINER https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a

ENV JAVA_OPTS -Xmx1024m

WORKDIR /opt/aspsp-xs2a

COPY ./target/xs2a-impl/xs2a-impl.jar /opt/aspsp-xs2a/aspsp-xs2a.jar

EXPOSE 8080

CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/aspsp-xs2a/aspsp-xs2a.jar
