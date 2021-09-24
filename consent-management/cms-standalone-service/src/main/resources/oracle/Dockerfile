FROM deepdiver/docker-oracle-xe-11g
MAINTAINER adorsys <rye@adorsys.com.ua>

# set oracle env
ENV ORACLE_SID XE
ENV ORACLE_BASE /u01/app/oracle
ENV ORACLE_HOME $ORACLE_BASE/product/11.2.0/xe
ENV PATH $ORACLE_HOME/bin:$PATH
ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 22
EXPOSE 1521

# create adorsys env script
COPY create-oracle-env.sh /
RUN chmod +x /create-oracle-env.sh

# default command
CMD sed -i -E "s/HOST = [^)]+/HOST = $HOSTNAME/g" $ORACLE_HOME/network/admin/listener.ora; \
service oracle-xe start; \
/create-oracle-env.sh > /create-oracle-env.out 2>&1; \
while true; do sleep 1000; done
