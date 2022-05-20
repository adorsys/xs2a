FROM alpine:latest

WORKDIR /jmeter
COPY . /jmeter

RUN apk add --no-cache \
	openjdk8-jre

ARG jmeterVersion=5.4.3
ENV JMETER_VERSION ${jmeterVersion}
ENV JMETER_HOME apache-jmeter-${JMETER_VERSION}
ENV JMETER_BIN ${JMETER_HOME}/bin
RUN wget -O /jmeter/apache-jmeter.tgz "http://mirror.its.dal.ca/apache/jmeter/binaries/apache-jmeter-5.4.3.tgz" \
	&& tar xvzf apache-jmeter.tgz

ENV testDir /jmeter/test
RUN sed -i "s|user.properties=user.properties|user.properties=${testDir}/user.properties|g; \
	    s|#javascript.use_rhino=false|javascript.use_rhino=true|g" ${JMETER_BIN}/jmeter.properties

RUN chmod +x ./entrypoint.sh

COPY entrypoint.sh /entrypoint.sh

CMD /bin/sh ./entrypoint.sh
