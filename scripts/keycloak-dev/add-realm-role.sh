#!/bin/bash

hostAndPort=$1
realm=$2
adminUser=$3
adminPassword=$4
role=$5

# Get and parse access token
RESP=$(curl -s -X POST "$hostAndPort/auth/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$adminUser" \
    -d "password=$adminPassword" \
    -d 'grant_type=password' \
    -d 'client_id=admin-cli')
TKN=`echo $RESP | sed 's/.*access_token":"//g' | sed 's/".*//g'`

# Create client
role="{\"name\":\"$role\",\"scopeParamRequired\":\"\"}"

RESP=$(curl -s -i -X POST "$hostAndPort/auth/admin/realms/$realm/roles/" \
    -H "Content-Type: application/json;charset=UTF-8" \
    -H "Authorization: Bearer $TKN" \
    -d $role | grep "Location:")

location=`echo ${RESP#* } | tr -d '\n\r'`

RESP=$(curl -s -i -X GET "$location" \
    -H "Content-Type: application/json;charset=UTF-8" \
    -H "Authorization: Bearer $TKN" )

roleId=`echo ${RESP##*/} | tr -d '\n\r'`
roleId=`echo ${roleId##*id\":\"}`

echo ${roleId:0:36}
