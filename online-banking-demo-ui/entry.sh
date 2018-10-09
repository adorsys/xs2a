#!/bin/sh

envsubst '$XS2A_URL
          $MOCKSERVER_URL
          $ONLINE_BANKING_SERVER_URL
          $CONSENT_MANAGEMENT_URL
          $PROFILE_SERVER_URL' \
< /etc/nginx/nginx.tpl.conf > /etc/nginx/nginx.conf

nginx -g "daemon off;"
