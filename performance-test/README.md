<h1 align="center">Jmeter parameters </h1>



#####JMETER_TEST_FILE
Put your `.jmx`file in `test/` folder `test/AIS-Embedded-Test.jmx` or change the environment variable `JMETER_TEST_FILE` on Openshift pod `perf-tests-xs2a-jmeter` YAML file with your filename
```sh
JMETER_TEST_FILE=AIS-Embedded-Test.jmx
```
#####JMETER_TEST_NAME
Test Name is passed to segregate the test results in grafana.
```sh
JMETER_TEST_NAME=PIIS-Test
```
#####JMETER_THREADS
Number of users to simulate.
```sh
JMETER_THREADS=50
```
#####XS2A_URL
Domain name or IP address of the web server. E.g. www.example.com. [Do not include the http:// prefix]
```sh
XS2A_URL=perf-tests-xs2a-connector.cloud.adorsys.de
```
#####XS2A_PROTOCOL
HTTP or HTTPS.
```sh
XS2A_PROTOCOL=https
```
#####JMETER_DURATION
JMeter will use this to calculate the End Time.
```sh
JMETER_DURATION=1200
```
#####JMETER_RAMPUP
How long JMeter should take to get all the threads started.
```sh
JMETER_RAMPUP=1
```
The ramp-up period tells JMeter how long to take to "ramp-up" to the full number of threads chosen.
#####JMETER_LOOP
Number of times to perform the test case
```sh
JMETER_LOOP=100
````
####INFLUX_DB_URL
The Influx DB URL. This variable is mapped to Jmeter Backend Listener.
```sh
INFLUX_DB_URL=perf-tests-xs2a-influxdb
````
#####INFLUX_DB_PORT=8086
The Influx DB Port number. This variable is mapped to Jmeter Backend Listener.
```sh
INFLUX_DB_PORT=8086
````
#####INFLUX_DB_USERNAME=writer
The Influx DB user name. This variable is mapped to Jmeter Backend Listener.
```sh
INFLUX_DB_USERNAME=admin
````
#####INFLUX_DB_PASSWORD
The Influx DB password. This variable is mapped to Jmeter Backend Listener.
```sh
NFLUX_DB_PASSWORD=admin123
````
#####INFLUX_REPORT_NAME
Influx Report Name is used to filter the different phases in grafana results.
```sh
INFLUX_REPORT_NAME=Phase1_PIIS_50x1_EmptyDB
````
#####INFLUX_DB_DATABASE_NAME
The Influx DB Name of the database. This variable is mapped to Jmeter Backend Listener.
```sh
INFLUX_DB_DATABASE_NAME=performance_tests
````

The `entrypoint.sh` script assumes that the test plan is named `AIS-Embedded-Test.jmx` and is present in the folder `/jmeter/test/`.

The JMeter logfile is available in `/jmeter/test/` for debugging purposes.

The JMeter HTML report is available in `/jmeter/test/reports`.


Access Grafana Dashboards on

```sh
https://perf-tests-xs2a-grafana.cloud.adorsys.de/
```

