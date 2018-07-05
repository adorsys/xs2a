#!/bin/bash

hostAndPort=$1
realm=$2
clientId=$3
bearerOnly=$4
adminUser=$5
adminPassword=$6

# Get and parse access token
RESP=$(curl -s -X POST "$hostAndPort/auth/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$adminUser" \
    -d "password=$adminPassword" \
    -d 'grant_type=password' \
    -d 'client_id=admin-cli')
TKN=`echo $RESP | sed 's/.*access_token":"//g' | sed 's/".*//g'`

# Create client
client="{\"enabled\":true,\"attributes\":{},\"redirectUris\":[\"*\"],\"bearerOnly\":\"$bearerOnly\",\"webOrigins\":[\"*\"],\"clientId\":\"$clientId\",\"clientTemplate\":null,\"protocol\":\"openid-connect\",\"directAccessGrantsEnabled\":true,\"publicClient\":true,\"standardFlowEnabled\":true}"

RESP=$(curl -s -i -X POST "$hostAndPort/auth/admin/realms/$realm/clients/" \
    -H "Content-Type: application/json;charset=UTF-8" \
    -H "Authorization: Bearer $TKN" \
    -d $client | grep "Location:")

echo "${RESP##*/}" | tr -d '\n\r'
