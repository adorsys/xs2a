#!/bin/bash
export PATH=$PATH:${JMETER_BIN}

current_time=$(date "+%Y-%m-%d_%H-%M")
mkdir -p test/reports/Report_$current_time

jmeter -n -t test/${JMETER_TEST_FILE} \
		-l test/reports/Report_$current_time/LogReport.jtl \
		-e -o test/reports/Report_$current_time \
		-Jthreads=${JMETER_THREADS} \
		-Jloop=${JMETER_LOOP} \
		-Jhost=${XS2A_URL} \
		-j test/reports/Logs.jtl -LDEBUG -Ljmeter.engine=DEBUG \
		-Jinfluxdburl=${INFLUX_DB_URL} \
		-Jinfluxreporttest=${JMETER_TEST_NAME} \
		-Jinfluxreportname=${INFLUX_REPORT_NAME} \
		-Jinfluxdbusername=${INFLUX_DB_USERNAME} \
		-Jinfluxdbpassword=${INFLUX_DB_PASSWORD} \
		-JinfluxDBDatabase=${INFLUX_DB_DATABASE} \
		-Jinfluxreportname=${INFLUX_REPORT_NAME}
