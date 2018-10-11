#!/bin/sh

envsubst '$XS2A_URL
          $KEYCLOAK_URL
          $MOCKSERVER_URL
          $ONLINE_BANKING_SERVER_URL
          $CONSENT_MANAGEMENT_URL
          $PROFILE_SERVER_URL' \
 < /etc/nginx/conf.d/default.conf > /etc/nginx/conf.d/default.TMP

mv /etc/nginx/conf.d/default.TMP /etc/nginx/conf.d/default.conf

nginx -g "daemon off;"
