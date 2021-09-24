#
# Copyright 2018-2021 adorsys GmbH & Co KG
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

















ADORSYS_USER=cms
ADORSYS_PASSWORD=cms
DATSPACE=asldat
IDXSPACE=aslidx

cat << EOF | sqlplus sys/oracle as sysdba
create tablespace $DATSPACE
datafile '$ORACLE_HOME/$DATSPACE' size 256M
autoextend on next 100M;
create tablespace $IDXSPACE
datafile '$ORACLE_HOME/$IDXSPACE' size 256M
autoextend on next 100M;
create user $ADORSYS_USER identified by $ADORSYS_PASSWORD
default tablespace $DATSPACE
temporary tablespace temp
profile default
account unlock
quota unlimited on $DATSPACE
quota unlimited on $IDXSPACE;
grant create session to $ADORSYS_USER;
grant create table to $ADORSYS_USER;
grant select_catalog_role to $ADORSYS_USER;
grant create procedure to $ADORSYS_USER;
grant create view to $ADORSYS_USER;
grant create sequence to $ADORSYS_USER;
grant create user to $ADORSYS_USER;
alter profile "default" limit password_verify_function null;
EOF
