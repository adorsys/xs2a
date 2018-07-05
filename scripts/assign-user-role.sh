#!/bin/bash

hostAndPort=$1
adminUser=$2
adminPassword=$3
realm=$4
userId=$5
roleId=$6
role=$7

echo "admin user id: $userId"
echo "admin role id: $roleId"

# Get and parse access token
RESP=$(curl -s -X POST "$hostAndPort/auth/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$adminUser" \
    -d "password=$adminPassword" \
    -d 'grant_type=password' \
    -d 'client_id=admin-cli')
TKN=`echo $RESP | sed 's/.*access_token":"//g' | sed 's/".*//g'`

request="[{\"id\":\"$roleId\",\"name\":\"$role\",\"scopeParamRequired\":false,\"composite\":false,\"clientRole\":false,\"containerId\":\"$realm\"}]"

RESP=$(curl -s -i -X POST "$hostAndPort/auth/admin/realms/$realm/users/$userId/role-mappings/realm" \
    -H "Content-Type: application/json;charset=UTF-8" \
    -H "Authorization: Bearer $TKN" \
    -d $request)

echo $RESP | tr -d '\n\r'
