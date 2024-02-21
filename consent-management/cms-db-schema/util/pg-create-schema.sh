#!/usr/bin/env bash

#
# Copyright 2018-2024 adorsys GmbH & Co KG
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU Affero General Public License as published
# by the Free Software Foundation, either version 3 of the License, or (at
# your option) any later version. This program is distributed in the hope that
# it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#
# This project is also available under a separate commercial license. You can
# contact us at sales@adorsys.com.
#

# create consent schema and give permissions to cms user. Needed for
# docker-compose so  that we can start the DB with the schema already being
# present (rat)
set -e
echo "Create schema='consent' for local postgres installation"
psql -U postgres -d consent -c 'CREATE SCHEMA IF NOT EXISTS consent AUTHORIZATION cms;'
