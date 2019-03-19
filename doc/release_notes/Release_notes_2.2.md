# Release notes v.2.2

## Bugfix: Add missing links and `Location` header to the AIS consent creation response
From now on, response to the AIS consent creation request(`POST /v1/consents`) contains previously missing `Location` 
header and the `self` link. Also this response now contains `status` and `scaStatus`(in case of `implicit` start of the 
authorisation process) links that were missing in `EMBEDDED` approach.
