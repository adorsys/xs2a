#!/bin/bash

hostAndPort=$1
adminUser=$2
adminPassword=$3
realm=$4
userName=$5
userMail=$6
userPassword=$7

# Get and parse access token
RESP=$(curl -s -X POST "$hostAndPort/auth/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$adminUser" \
    -d "password=$adminPassword" \
    -d 'grant_type=password' \
    -d 'client_id=admin-cli')
TKN=`echo $RESP | sed 's/.*access_token":"//g' | sed 's/".*//g'`

# create user and parse result link. Http location header.
data="{\"enabled\":true,\"attributes\":{},\"requiredActions\":[\"UPDATE_PASSWORD\"],\"username\":\"$userName\",\"email\":\"$userMail\",\"firstName\":\"$userName\",\"lastName\":\"$userName\",\"emailVerified\":true}"
RESP=$(curl -s -i -X POST "$hostAndPort/auth/admin/realms/$realm/users/" \
    -H "Content-Type: application/json;charset=UTF-8" \
    -H "Authorization: Bearer $TKN" -d $data | grep Location:)

location=`echo ${RESP#* } | tr -d '\n\r'`

#actionEmailLocation=${location}/execute-actions-email
# send reset password mail
#curl -s -X PUT $actionEmailLocation -H 'Content-Type: application/json;charset=UTF-8' -H "Authorization: Bearer $TKN" -d []


# set user password
resetPasswordLocation=${location}/reset-password
data="{\"type\":\"password\",\"value\":\"$userPassword\",\"temporary\":false}"
curl -s -X PUT $resetPasswordLocation -H 'Content-Type: application/json;charset=UTF-8' -H "Authorization: Bearer $TKN" -d $data

echo "${RESP##*/}" | tr -d '\n\r'
