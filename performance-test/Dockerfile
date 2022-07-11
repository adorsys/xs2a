FROM openjdk:17.0.2-oraclelinux8

ARG BUILD_DATE="2022-07-01"
# Labels
LABEL org.label-schema.version="0.0.1" \
      org.label-schema.vendor="adorsys GmbH & Co. KG" \
      org.label-schema.name="xs2a-performance-tests" \
      org.label-schema.description="This service run performance tests on XS2A connector" \
      org.label-schema.build-date=${BUILD_DATE}


# Env Variables
ARG JMETER_VERSION="5.5"
ENV JMETER_HOME /opt/jmeter/apache-jmeter-${JMETER_VERSION}
ENV JMETER_LIB_FOLDER ${JMETER_HOME}/lib
ENV JMETER_PLUGINS_FOLDER ${JMETER_LIB_FOLDER}/ext/
ENV JMETER_BIN ${JMETER_HOME}/bin
ENV DEBUG_LEVEL="INFO"


RUN microdnf update -y  && microdnf install -y \
    wget \
    curl \
    && microdnf clean all

WORKDIR /opt/jmeter

RUN mkdir -p ${JMETER_HOME} ${JMETER_PLUGINS_FOLDER} \
    && curl -sL https://mirror.its.dal.ca/apache/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz | tar -xz \
    && curl -sL https://github.com/sfakrudeen78/JMeter-InfluxDB-Writer/releases/download/v-1.2.2/JMeter-InfluxDB-Writer-plugin-1.2.2.jar \
    --output ${JMETER_PLUGINS_FOLDER}/JMeter-InfluxDB-Writer-plugin-1.2.2.jar


RUN mkdir -m 777 -p temp test/reports
COPY ./test ./test
COPY  entrypoint.sh ./
USER 10000

ENTRYPOINT ["./entrypoint.sh"]
