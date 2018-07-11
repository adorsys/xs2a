#!/bin/bash

hostAndPort=$1
realmName=$2
adminUser=$3
adminPassword=$4

# Get and parse access token
RESP=$(curl -s -X POST "$hostAndPort/auth/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$adminUser" \
    -d "password=$adminPassword" \
    -d 'grant_type=password' \
    -d 'client_id=admin-cli')
TKN=`echo $RESP | sed 's/.*access_token":"//g' | sed 's/".*//g'`

realm="{\"enabled\":true,\"id\":\"$realmName\",\"realm\":\"$realmName\",\"registrationAllowed\":true}"

#Create REALM
curl -s -X POST "$hostAndPort/auth/admin/realms" \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H "Authorization: Bearer $TKN" \
    -d $realm
