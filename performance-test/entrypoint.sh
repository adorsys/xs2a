#!/bin/bash
#
# Copyright 2018-2024 adorsys GmbH & Co KG
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU Affero General Public License as published
# by the Free Software Foundation, either version 3 of the License, or (at
# your option) any later version. This program is distributed in the hope that
# it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#
# This project is also available under a separate commercial license. You can
# contact us at sales@adorsys.com.
#

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
