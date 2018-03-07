FROM jboss/keycloak:3.4.3.Final

USER root

ADD keycloak-clientregistration-provider/target/keycloak-clientregistration-provider.jar /opt/jboss/keycloak/providers/keycloak-clientregistration-provider.jar

RUN chgrp -R 0 $JBOSS_HOME &&\
chmod -R g+rw $JBOSS_HOME

USER jboss