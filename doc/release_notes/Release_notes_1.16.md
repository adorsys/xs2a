# Release notes v. 1.16

Main points of new Release:
* Support of multilevel SCA for Payments (Embedded approach)
* Interfaces to export Consents from CMS
* Provide correct error mapping according to Berlin Group's Open API 1.3

## Table of contents

- [Security Fix:  webpack-dev-server](#security-fix-webpack-dev-server)
- [Expire AIS consent for TPP made by PSU when new AIS consent is created and authorised](#expire-ais-consent-for-tpp-made-by-psu-when-new-ais-consent-is-created-and-authorised)
- [Added new options to ASPSP profile](#added-new-options-to-aspsp-profile)
- [Extend a list of Transaction Statuses for PIS](#extend-a-list-of-transaction-statuses-for-pis)
- [Inner integration tests](#inner-integration-tests)
- [TPP-Nok-Redirect-URI returned when scaRedirect URI is expired (for Payment cancellation)](#tpp-nok-redirect-uri-returned-when-scaredirect-uri-is-expired-for-payment-cancellation)
- [Bugfix: Remove default values for TPP in the database](#bugfix-remove-default-values-for-tpp-in-the-database)
- [Integration-tests package is removed](#integration-tests-package-is-removed)
- [Remove some not null constraints for TPP in the CMS database](#remove-some-not-null-constraints-for-tpp-in-the-cms-database)
- [Fixed logic of deleting consent from Xs2a Interface](#fixed-logic-of-deleting-consent-from-xs2a-interface)
- [Provide new Java interface and Endpoint to export AIS Consents by ASPSP Account ID](#provide-new-java-interface-and-endpoint-to-export-ais-consents-by-aspsp-account-id)
- [Provide new Java interface and Endpoint to export PIIS Consents by PSU, TPP and ASPSP Account ID](#provide-new-java-interface-and-endpoint-to-export-piis-consents-by-psu-tpp-and-aspsp-account-id)
- [Define new Java interface and Endpoint to save Account Access object in Consent by Online banking](#provide-new-java-interface-and-endpoint-to-export-piis-consents-by-psu-tpp-and-aspsp-account-id)
- [Bugfix: Remove TPP-ID from get payments by aspspAccountId endpoint in CMS](#bugfix-remove-tpp-id-from-get-payments-by-aspspaccountid-endpoint-in-cms)
- [Bugfix: Fix empty SpiAccountAccess being provided in SpiAccountConsent in some cases](#bugfix-fix-empty-spiaccountaccess-being-provided-in-spiaccountconsent-in-some-cases)
- [Change the paths of some PIS and AIS enpoints in CMS-PSU-API](#change-the-paths-of-some-pis-and-ais-enpoints-in-cms-psu-api)
- [Multilevel SCA for payment initiation in embedded approach](#multilevel-sca-for-payment-initiation-in-embedded-approach)
- [Updated error response mapping to be compliant with specification 1.3](#updated-error-response-mapping-to-be-compliant-with-specification-13)
- [Bugfix: validate PSU TAN during update PSU data requests for payments](#bugfix-validate-psu-tan-during-update-psu-data-requests-for-payments)

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

## Provide new Java interface and Endpoint to export AIS Consents by ASPSP Account ID
By accessing `/aspsp-api/v1/ais/consents/account/{account-id}`
(or corresponding method in `CmsAspspAisExportService.java`)
one can export AIS Consents that contain certain account id.

## Provide new Java interface and Endpoint to export PIIS Consents by PSU, TPP and ASPSP Account ID
By accessing `/aspsp-api/v1/piis/consents/*` endpoints
(or corresponding methods in `CmsAspspPiisFundsExportService.java`)
one can export PIIS Consents by the same criterias as for AIS Consents or PIS Payments.

## Define new Java interface and Endpoint to save Account Access object in Consent by Online banking
By accessing `/psu-api/v1/ais/consent/{consent-id}/save-access` endpoint
(or corresponding method in `CmsPsuAisService.java`)
one can save AccountAccess (along with `aspspAccountId` and `resourceId` if necessary) in consent from the online-banking side.


## Bugfix: Remove TPP-ID from get payments by aspspAccountId endpoint in CMS
TPP-ID was removed as a parameter from `exportPaymentsByAccountIdAndTpp` method in `de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService`, 
the method itself  was renamed to `exportPaymentsByAccountId`.
Corresponding endpoint in the CMS controller was changed as well:

| Method | Context                          | Old path                                                     | New path                                        |
|--------|----------------------------------|--------------------------------------------------------------|-------------------------------------------------|
| GET    | Get payments by ASPSP account ID | aspsp-api/v1/pis/payments/tpp/{tpp-id}/accounts/{account-id} | aspsp-api/v1/pis/payments/accounts/{account-id} |

## Bugfix: Fix empty SpiAccountAccess being provided in SpiAccountConsent in some cases
Now `SpiAccountConsent` argument contains proper `SpiAccountAccess` in `de.adorsys.psd2.xs2a.spi.service.AccountSpi#requestAccountList`
method when no accesses were previously provided by the connector in a response to AIS consent initiation.

## Change the paths of some PIS and AIS enpoints in CMS-PSU-API
Some paths were confusing, so that was not clear which endpoint should be used to validate redirectUrl.
Now "Update PSU Data" call is done using authorisationId, not redirectId.
Also PsuData is provided by request's body, not in the header (normal behaviour for PUT).
Please note as well that the word `/pis/consent` in the path was changed to `/payment`,
so it's much more clear that it is about PIS enpoints.


## Multilevel SCA for payment initiation in embedded approach
 
For accounts with multiple PSUs, now it is possible to execute multilevel SCA for each PSU in embedded approach. Now payment initiation response from SPI
contains new boolean field `multilevelScaRequired` to inform XS2A that this payment requires multilevel SCA and there should be always explicit authorisation approach used.
All successful authorisations(except the last one) will set payment status to `PATC` in CMS, and the final authorisation - will set it to `ACCP`.

## Updated error response mapping to be compliant with specification 1.3
In the previous release notes it was mentioned: `Please note that in this release some problems with errors responses appear`.
The fix was created. So now error responses are compliant with specification 1.3.

Error response body example: 
```json
{
    "tppMessages": [
        {
            "category": "ERROR",
            "code": "FORMAT_ERROR",
            "text": "Format of certain request fields are not matching the XS2A requirements."
        }
    ]
}
```

## Bugfix: validate PSU TAN during update PSU data requests for payments
From now on when PSU sends wrong TAN during the authorisation process, he will receive response with PSU_CREDENTIALS_INVALID error(response code HTTP 401).
