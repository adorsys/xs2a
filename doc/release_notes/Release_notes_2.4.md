# Release notes v.2.4

## Bugfix: Fix update authorisation status endpoint in CMS-PSU-API not working with lowercase values for authorisation status
From now on, CMS-PSU-API endpoints for updating authorisation status for payments 
(`PUT /psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}`) and consents 
(`PUT /psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}`) will work correctly with
lowercase status values. Also trying to provide an invalid status value will result in `400 Bad Request` error being 
returned instead of `500 Internal Server Error`.
