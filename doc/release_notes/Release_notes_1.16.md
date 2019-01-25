# Release notes v. 1.16


## Security Fix:  webpack-dev-server
webpack-dev-server and corresponding dependencies were updated.

[CVE-2018-14732](https://nvd.nist.gov/vuln/detail/CVE-2018-14732)

An issue was discovered in lib/Server.js in webpack-dev-server before 3.1.11.
Attackers are able to steal developer's code because the origin of requests is not checked by the WebSocket server, which is used for HMR (Hot Module Replacement). Anyone can receive the HMR message sent by the WebSocket server via a ws://127.0.0.1:8080/ connection from any origin.

## Expire AIS consent for TPP made by PSU when new AIS consent is created and authorised
The number of user AIS consents given to TPP is restricted: for one PSU can be only one consent given to the specific TPP.
New conditions:
* when new AIS consent is authorised and its `recurringIndicator` property is `true`, the previous consents statuses are set to `TERMINATED_BY_TPP`;
* if consent `recurringIndicator` property is `false`, the consent status is set to `EXPIRED` after consent has been used;
* when PSU send a consent creation request with the following data: `recurringIndicator` property is `false` AND `frequencyPerDay` is more than `1`, 
the response with `400 FORMAT_ERROR` is returned.

## Added new options to ASPSP profile
| Option                                       | Meaning                                                                                                | Default value | 
|----------------------------------------------|--------------------------------------------------------------------------------------------------------|---------------|
| availableAccountsConsentSupported            | This field indicates if ASPSP supports available accounts for a consent                                | true          |
| scaByOneTimeAvailableAccountsConsentRequired | This field indicates if ASPSP requires usage of SCA to validate a one-time available accounts consent  | true          |
