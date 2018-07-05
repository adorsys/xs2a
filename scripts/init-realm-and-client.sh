#!/bin/bash

sh /scripts/wtfc.sh -T 60 -S 0 -I 2 curl -f localhost:8081/auth

echo 'create xs2a realm'
sh /scripts/add-realm.sh http://localhost:8081 xs2a admin zzz

echo 'create client xs2a-impl'
sh /scripts/add-realm-client.sh http://localhost:8081 xs2a xs2a-impl false admin zzz

echo 'create client aspsp-mock'
sh /scripts/add-realm-client.sh http://localhost:8081 xs2a aspsp-mock false admin zzz

echo 'create admin role'
roleId=$(sh /scripts/add-realm-role.sh http://localhost:8081 xs2a admin zzz admin)

echo 'create admin user'
userId=$(sh /scripts/add-domain-user.sh localhost:8081 admin zzz xs2a fractal pruex@adorsys.com.ua zzz)

if [[ -n "$userId" && -n "$roleId" ]]
then
    echo 'assign admin role'
    sh /scripts/assign-user-role.sh localhost:80801 admin zzz xs2a $userId $roleId admin
fi

