# Release notes v. 1.16


## Security Fix:  webpack-dev-server
webpack-dev-server and corresponding dependencies were updated.

[CVE-2018-14732](https://nvd.nist.gov/vuln/detail/CVE-2018-14732)

An issue was discovered in lib/Server.js in webpack-dev-server before 3.1.11.
Attackers are able to steal developer's code because the origin of requests is not checked by the WebSocket server, 
which is used for HMR (Hot Module Replacement). 
Anyone can receive the HMR message sent by the WebSocket server via a ws://127.0.0.1:8080/ connection from any origin.

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

## Extend a list of Transaction Statuses for PIS
New version of API Yaml file published by Berlin Group contains three new statuses for Transactions:
| Code | Value                               | Provided description                                                                                                                                                   |
|------|-------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ACCC | AcceptedSettlementCompletedCreditor | Settlement on the creditor's account has been completed.                                                                                                               |
| ACFC | AcceptedFundsChecked                | Preceeding check of technical validation and customer profile was successful and an automatic funds check was positive.                                                |
| PATC | PartiallyAcceptedTechnicalCorrect   |  The payment initiation needs multiple authentications, where some but not yet all have been performed. Syntactical and semantical validations are successful.         |

XS2A Classes were updated with these new values, so that they may be used in SPI level.

## Inner integration tests
Added integration tests in spi-mock service.
12 tests check successes payments initiation:
  - for payment types Single, Periodic and Bulk 
  - for modes Explicit and Implicit 
  - for sca approach Redirect and Embedded
1 test checks payment sca status

## TPP-Nok-Redirect-URI returned when scaRedirect URI is expired (for Payment cancellation)
Now for Payment cancellation if scaRedirect URI is expired we deliver TPP-Nok-Redirect-URI in the response from CMS to Online-banking. This response is returned with code 408.
If TPP-Nok-Redirect-URI was not sent from TPP and in CMS is stored null, then CMS returns empty response with code 408. If payment is not found or psu data is incorrect, CMS returns 404. 

## Bugfix: Remove default values for TPP in the database
From now on default values for TPP-related fields are no longer provided in the database.

## Integration-tests package is removed
Due to various internal reasons integration tests on cucumber are removed from the project 
and will be not part of Open Source solution anymore.
Last version with this package in Open Source is 1.15.

## Remove some not null constraints for TPP in the CMS database
Not null constraints were removed from most of the columns in the `tpp_info` table.
From now on only `tpp_info_id`, `authorisation_number`, `authority_id` and `instance_id` columns can't be null.

## Fixed logic of deleting consent from Xs2a Interface
If endpoint "Delete AIS consent" (DELETE /v1/consents/{consent-id}) is triggered by TPP, now Xs2a checks the status of the consent: if the consent status is RECEIVED, then 
the status would be changed to REJECTED, because the consent is not yet authorized and is in the initiation phase. If the consent is in the 
lifecycle phase (has status VALID), consent status is set to TERMINATED_BY_TPP. This affects only calls made to Xs2a interface, calls made from Online-Banking or to CMS directly are not affected.

## Bugfix: Remove TPP-ID from get payments by aspspAccountId endpoint in CMS
TPP-ID was removed as a parameter from `exportPaymentsByAccountIdAndTpp` method in `de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService`, 
the method itself  was renamed to `exportPaymentsByAccountId`.
Corresponding endpoint in the CMS controller was changed as well:

| Method | Context                          | Old path                                                     | New path                                        |
|--------|----------------------------------|--------------------------------------------------------------|-------------------------------------------------|
| GET    | Get payments by ASPSP account ID | aspsp-api/v1/pis/payments/tpp/{tpp-id}/accounts/{account-id} | aspsp-api/v1/pis/payments/accounts/{account-id} |
