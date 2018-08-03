FROM adorsys/openjdk-jre-base:8-minideb

MAINTAINER https://github.com/adorsys/xs2a

ENV JAVA_OPTS -Xmx1024m -Dserver.port=8080
ENV JAVA_TOOL_OPTIONS -Xmx1024m

WORKDIR /opt/online-banking

COPY ./target/online-banking-demo*jar /opt/online-banking/online-banking-demo.jar

EXPOSE 8080

CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/online-banking/online-banking-demo.jar
