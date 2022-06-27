FROM ubuntu:18.04
# Step 1
WORKDIR /jmeter
COPY . /jmeter

# Step 2
ARG JMETER_VERSION="5.4.3"
ENV JMETER_HOME /jmeter/apache-jmeter-${JMETER_VERSION}
ENV JMETER_LIB_FOLDER ${JMETER_HOME}/lib/
ENV JMETER_PLUGINS_FOLDER ${JMETER_LIB_FOLDER}ext/
ENV JMETER_BIN ${JMETER_HOME}/bin

# Step 3:
WORKDIR ${JMETER_HOME}
RUN  apt-get -y update \
&& apt-get install -y wget gnupg curl

# Step 4:
# Download Apache JMeter
RUN wget http://mirror.its.dal.ca/apache/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz
RUN tar -xzf apache-jmeter-${JMETER_VERSION}.tgz
RUN mv apache-jmeter-${JMETER_VERSION}/* /jmeter/apache-jmeter-${JMETER_VERSION}
RUN rm -r /jmeter/apache-jmeter-${JMETER_VERSION}/apache-jmeter-${JMETER_VERSION}

# Step 5:
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
&& apt-get update \
&& apt-get install -y --no-install-recommends \
git \
openjdk-8-jre-headless

# Step 6:
# Download JMeter Plugins manager and move it to lib/ext folder
WORKDIR ${JMETER_PLUGINS_FOLDER}
RUN wget https://github.com/sfakrudeen78/JMeter-InfluxDB-Writer/releases/download/v-1.2.2/JMeter-InfluxDB-Writer-plugin-1.2.2.jar

# Step 7:
RUN mkdir -m 777 -p /jmeter/temp
WORKDIR /jmeter

# Step 8:
RUN chmod +x entrypoint.sh
COPY entrypoint.sh entrypoint.sh
CMD /bin/sh entrypoint.sh
