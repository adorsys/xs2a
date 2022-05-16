#!/bin/bash
export PATH=$PATH:${JMETER_BIN}

current_time=$(date "+%Y-%m-%d_%H-%M")
mkdir -p test/reports/Report_$current_time

jmeter -n -t test/${JMETER_TEST_FILE} -l test/reports/Report_$current_time/LogReport.jtl -e -o test/reports/Report_$current_time -Jhost=${XS2A_URL} -Jthreads=${JMETER_THREADS} -Jinfluxdburl=${INFLUX_DB_URL} -Jinfluxreportname=${INFLUX_REPORT_NAME} -Jloop=${JMETER_LOOP} -Jinfluxdbusername=${INFLUX_DB_USERNAME} -Jinfluxdbpassword={INFLUX_DB_PASSWORD} -JinfluxDBDatabase={INFLUX_DB_DATABASE}
 
