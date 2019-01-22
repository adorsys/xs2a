# Release notes v. 1.16


## Security Fix:  webpack-dev-server
webpack-dev-server and corresponding dependencies were updated.

[CVE-2018-14732](https://nvd.nist.gov/vuln/detail/CVE-2018-14732)

An issue was discovered in lib/Server.js in webpack-dev-server before 3.1.11.
Attackers are able to steal developer's code because the origin of requests is not checked by the WebSocket server, which is used for HMR (Hot Module Replacement). Anyone can receive the HMR message sent by the WebSocket server via a ws://127.0.0.1:8080/ connection from any origin.

## Added new options to ASPSP profile
| Option                                       | Meaning                                                                                                | Default value | 
|----------------------------------------------|--------------------------------------------------------------------------------------------------------|---------------|
| availableAccountsConsentSupported            | This field indicates if ASPSP supports available accounts for a consent                                | true          |
| scaByOneTimeAvailableAccountsConsentRequired | This field indicates if ASPSP requires usage of SCA to validate a one-time available accounts consent  | true          |
