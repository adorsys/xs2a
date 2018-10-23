#!/bin/bash

sh wtfc.sh -T 60 -S 0 -I 2 curl -f localhost:8081/auth

echo 'create xs2a realm'
sh add-realm.sh http://localhost:8081 xs2a admin admin123

echo 'create client xs2a-impl'
sh add-realm-client.sh http://localhost:8081 xs2a xs2a-impl false admin admin123

echo 'create client aspsp-mock'
sh add-realm-client.sh http://localhost:8081 xs2a aspsp-mock false admin admin123

echo 'create admin role'
roleId=$(sh add-realm-role.sh http://localhost:8081 xs2a admin admin123 admin)

echo 'create admin user'
userId=$(sh add-domain-user.sh localhost:8081 admin admin123 xs2a aspsp aspsp@adorsys.de zzz)

if [[ -n "$userId" && -n "$roleId" ]]
then
    echo 'assign admin role'
    sh assign-user-role.sh localhost:80801 admin admin123 xs2a $userId $roleId admin
fi

