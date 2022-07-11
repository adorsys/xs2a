#!/bin/bash
set -e

export PATH=$PATH:${JMETER_BIN}

current_time=$(date "+%Y-%m-%d_%H-%M")
mkdir -m 777 -p test/reports/Report_$current_time

jmeter -n -t test/${JMETER_TEST_FILE} \
		-l test/reports/Report_$current_time/LogReport.jtl \
		-e -o test/reports/Report_$current_time \
		-Jthreads=${JMETER_THREADS} \
		-Jloop=${JMETER_LOOP} \
		-Jhost=${XS2A_URL} \
		-Jprotocol=${XS2A_PROTOCOL} \
		-j test/reports/Logs.jtl \
		-L${DEBUG_LEVEL} \
		-Ljmeter.engine=${DEBUG_LEVEL} \
		-Jinfluxdburl=${INFLUX_DB_URL} \
		-Jinfluxdbport=${INFLUX_DB_PORT} \
	  -Jinfluxdbdatabasename=${INFLUX_DB_DATABASE_NAME} \
		-Jinfluxdbusername=${INFLUX_DB_USERNAME} \
		-Jinfluxdbpassword=${INFLUX_DB_PASSWORD} \
		-Jinfluxreportname=${INFLUX_REPORT_NAME} \
		-Jinfluxtestname=${JMETER_TEST_NAME} \
		&& tail -f /dev/null
