# DICT_01 The dictionaries of common business objects used in XS2A Framework

## DICT_01_01 PSU Data

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
|password | String | Optional | | 


## DICT_01_02 TPP Message Information

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| category | String | Mandatory | Only "ERROR" or "WARNING" permitted |
| code | Message Code | Mandatory | |
| path | String | Conditional | |
| text | Max512Text | Optional | Additional explaining text. |


## DICT_01_03 Amount

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| currency | Currency Code | Mandatory | ISO 4217 code |
| content | Floating Point Number | Mandatory | The amount given with fractional digits, where fractions must be compliant to the currency definition. The decimal separator is a dot. |


## DICT_01_04 Address

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| street | Max70Text | Optional | |
| buildingNumber | String | Optional | |
| city | String | Optional | |
| postalCode | String | Optional | |
| country | Country Code | Mandatory | |


## DICT_01_05 Remittance

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| reference | Max35Text | Mandatory | The actual reference. |
| referenceType | Max35Text | Optional | |
| refrenceIssuer | Max35Text | Optional |


## DICT_01_06 Links

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| redirect | String | Optional | A link to an ASPSP site where SCA is performed within the Redirect SCA approach. |
| oAuth | String | Optional | The link refers to a JSON document specifying the OAuth details of the ASPSP’s authorization server. JSON document follows the definition given in https://tools.ietf.org/html/draft-ietf-oauth- discovery. |
| updatePSUIdentification | String | Optional | The link to the payment initiation or account information resource, which needs to be updated by the PSU identification if not delivered yet. |
| updateProprietaryData | String | Optional | The link to the payment initiation or account information resource which needs to be updated by the proprietary data. |
| updatePsuAuthentication | String | Optional | The link to the payment initiation or account information resource, which needs to be updated by a PSU password and eventually the PSU identification if not delivered yet. |
| selectAuthenticationMethod | String | optional | This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there were several available authentication methods. |
| self | String | Optional | The link to the payment initiation resource created by the request itself. This link can be used later to retrieve the transaction status of the payment initiation. |
| status | String | Optional | |
| viewAccount | String | Optional | |
| viewBalances | String | Optional | A link to the resource providing the balance of a dedicated account. |
| viewTransactions | String | Optional | A link to the resource providing the transaction history of a dediated amount. |
| first | String | Optional | Navigation link for paginated account reports. |
| next | String | Optional | Navigation link for paginated account reports. |
| previous | String | Optional | Navigation link for paginated account reports. |
| last | String | Optional | Navigation link for paginated account reports. |
| download | String | Optional | Download link for huge AIS data packages. |
 
 
## DICT_01_07 Authentication Object

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| authenticationType | Authentication Type | Mandatory | Type of the authentication method. |
| authenticationVersion | String | Conditional | Depending on the authenticationType. This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type. This version can be referred to in the ASPSP’s documentation. |
| authenticationMethodId | Max35Text | Mandatory | An identification provided by the ASPSP for the later identification of the authentication method selection. |
| name | String | Optional | This is the name of the authentication method defined by the PSU in the Online Banking frontend of the ASPSP. Alternatively this could be a description provided by the ASPSP like “SMS OTP on phone +49160 xxxxx 28”. This name shall be used by the TPP when presenting a list of authentication methods to the PSU, if available. |
| explanation | String | Optional | Detailed information about the sca method for PSU |


## DICT_01_08 Authentication Type

| Name | Description |
|:----:|-----------|
| SMS_OTP | An SCA method, where an OTP linked to the transaction to be authorized is sent to the PSU through a SMS channel. |
| CHIP_OTP | An SCA method, where an OTP is generated by a chip card, e.g. an TOP derived from an EMV cryptogram. To contact the card, the PSU normally needs a (handheld) device. With this device, the PSU either reads the challenging data through a visual interface like flickering or the PSU types in the challenge through the device key pad. The device then derives an OTP from the challenge data and displays the OTP to the PSU. |
| PHOTO_OTP | An SCA method, where the challenge is a QR code or similar encoded visual data which can be read in by a consumer device or specific mobile app. The device resp. the specific app than derives an OTP from the visual challenge data and displays the OTP to the PSU. |
| PUSH_OTP | An OTP is pushed to a dedicated authentication APP and displayed to the PSU. |


## DICT_01_09 Challenge

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| image | String | Optional | PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding <br><br> This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method. |
| data | String | Optional | String challenge data |
| image_Link | String | Optional | A link where the ASPSP will provides the challenge image for the TPP. |
| optMaxLength | Integer | Optional | The maximal length for the OTP to be typed in by the PSU. |
| otpFormat | String | Optional | The format type of the OTP to be typed in. The admitted values are “characters” or “integer”. |
| additionalInformation | String | Optional | Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU. |

 
## DICT_01_10 Message Code

| Message Code | http response code | Description |
|:------------:|:------------------:|-----------|
| CERTIFICATE_INVALID | 401 | The contents of the signature/corporate seal certificate are not matching PSD2 general PSD2 or attribute requirements. |
| CERTIFICATE_EXPIRED | 401 | Signature/corporate seal certificate is expired. |
| CERTIFICATE_BLOCKED | 401 | Signature/corporate seal certificate has been blocked by the ASPSP. |
| CERTIFICATE_REVOKED | 401 | Signature/corporate seal certificate has been revoked by QSTP. |
| CERTIFICATE_MISSING | 401 | Signature/corporate seal certificate was not available in the request but is mandated for the corresponding. |
| SIGNATURE_INVALID | 401 | Application layer eIDAS Signature for TPP authentication is not correct. |
| SIGNATURE_MISSING | 401 | Application layer eIDAS Signature for TPP authentication is mandated by the ASPSP but is missing. |
| FORMAT_ERROR | 400 | Format of certain request fields are not matching the XS2A requirements. An explicit path to the corresponding field might be added in the return message. |
| PSU_CREDENTIALS_INVALID | 401 | The PSU-ID cannot be matched by the addressed ASPSP or is blocked, or a password resp. OTP was not correct. Additional information might be added. |
| SERVICE_INVALID | 400 (if payload) <br> 405 (if http method) | The addressed service is not valid for the addressed resources or the submitted data. |
| SERVICE_BLOCKED | 403 | This service is not reachable for the addressed PSU due to a channel independent blocking by the ASPSP. Additional information might be given by the ASPSP. |
| CORPORATE_ID_INVALID | 401 | The PSU-Corporate-ID cannot be matched by the addressed ASPSP. |
| CONSENT_UNKNOWN | 403 (if path) <br> 400 (if payload) | The consent-ID cannot be matched by the ASPSP relative to the TPP. | 
| CONSENT_INVALID | 401 | The consent was created by this TPP bus is not valid for the addressed service/resource. |
| CONSENT_EXPIRED | 401 | The consent was created by this TPP but has expired and needs to be renewed. |
| TOKEN_UNKNOWN | 401 | The OAuth2 token cannot be matched by the ASPSP relative to the TPP. |
| TOKEN_INVALID | 401 | The OAuth2 token is associated to the TPP but is not valid for the addressed service/resource. | 
| TOKEN_EXPIRED | 401 | The OAuth2 token is associated to the TPP but has expired and needs to be renewed. |
| RESOURCE_UNKNOWN | 404 (if account-id in path) <br> 403 (if other resource in path) <br> 400 (if payload) | The addressed resource is associated with the TPP but has expired, not addressable anymore. |
| TIMESTAMP_INVALID | 400 | Timestamp not in accepted time period. | 
| PERIOD_INVALID | 400 | Requested time period out of bound. |
| SCA_METHOD_UNKNOWN | 400 | Addressed SCA method in the Authentication Mehtod Select Request is unknown or cannot be matched by the ASPSP with the PSU. |
| TRANSACTION_ID_INVALID | 400 | The TPP-Transaction-ID is not matching the temporary resource. |

**PIS specific error codes**

| Message Code | http response code | Description |
|:------------:|:------------------:|-----------|
| PRODUCT_INVALID | 403 | The addressed payment product is not available for the PSU . |
| PRODUCT_UNKNOWN | 404 | The addressed payment product is not supported by the ASPSP. |
| PAYMENT_FAILED | 400 | The payment initiation POST request failed during the initial process.. Additional information may be provided by the ASPSP. |

**AIS specific error codes**

| Message Code | http response code | Description |
|:------------:|:------------------:|-----------|
| CONSENT_INVALID | 401 | The consent definition is not complete or invalid. In case of being not complete, the bank is not supporting a completion of the consent towards the PSU. Additional information will be provided. |
| SESSIONS_NOT_SUPPORTED | 400 | The combined service flag may not be used with this ASPSP. |
| ACCESS_EXCEEDED | 429 | The access on the account has been exceeding the consented multiplicity per day. |
| REQUESTED_FORMATS_INVALID | 406 | The requested formats in the Accept header entry are not matching the formats offered by the ASPSP. |

**PIIS specific error codes**

| Message Code | http response code | Description |
|:------------:|:------------------:|-----------|
| CARD_INVALID | 400 | Addressed card number is unknown to the ASPSP or not associated to the PSU. |
| NO_PIIS_ACTIVATION | 400 | The PSU has not activated the addressed account for the usage of the PIIS associated with the TPP. |


## DICT_01_11 Transaction status

Code | Name | ISO 20022 Definition |
------- | ---------------- |  ---------
| ACCP |  AcceptedCustomerProfile |	Preceding check of technical validation was successful. Customer profile check was also successful. |
| ACSC | AcceptedSettlementCompleted | Settlement on th’ debtor's account has been completed.  Usage : this can be used by the first agent to report to  the debtor that the transaction has been completed.  Warning : this status is provided for transaction status  reasons, not for financial information. It can only be used after bilateral agreement.  |
| ACSP | AcceptedSettlementInProcess | All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution. |
| ACTC | AcceptedTechnicalValidation | Authentication and syntactical and semantical validation are successful |
| ACWC | AcceptedWithChange | Instruction is accepted but a change will be made, such as date or remittance not sent. |
| ACWP | AcceptedWithoutPosting | Payment instruction included in the credit transfer is accepted without being posted to the creditor  customer’s account. |
| RCVD | Received | Payment initiation has been received by the receiving  agent. |
| PDNG | Pending | Payment initiation or individual transaction included in  the payment initiation is pending. Further checks and  status update will be performed. |
| RJCT | Rejected | Payment initiation or individual transaction included in   the payment initiation has been rejected. |

If the response is JSON based, then the Name entry is used, to get a better readability.


## DICT_01_12 Account Access

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| accounts | Array of Account Reference | Optional | Is asking for detailed account information. If the array is empty, the TPP is asking for an accessible account list. This may be restricted in a PSU/ASPSP authorization dialogue. |
| balances | Array of Account Reference | Optional | Is asking for balances of the addressed accounts. If the array is empty, the TPP is asking for the balances of all accessible account lists. This may be restricted in a PSU/ASPSP authorization dialogue. |
| transactions | Array of Account Reference | Optional | Is asking for transactions of the addressed accounts. If the array is empty, the TPP is asking for the transactions of all accessible account lists. This may be restricted in a PSU/ASPSP authorization dialogue. |
| availableAccounts | String | [Optional] | Only the values "allAccounts" and "allAccountsWithBalances" are admitted. |
| allPsd2 | String | [Optional] | Only the values "allAccounts" and "allAccountsWithBalances" are admitted. | 


## DICT_01_13 Account Reference

This type is containing any account identification which can be used on payload-level to address specific accounts. The ASPSP will document which account reference type it will support. Exactly one of the attributes defined as "conditional" shall be used.

**Remark:** The currency of the account is needed, where the currency is an account characteristic identifying certain sub-accounts under one external identifier like an IBAN. These sub-accounts are separated accounts from a legal point of view and have separated balances, transactions etc.

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| iban | IBAN | Conditional | |
| bban | BBAN | Conditional | This data elements is used for payment accounts which have no IBAN. |
| pan | String | Conditional | Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements. |
| maskedPan | String | Conditional | Primary Account Number (PAN) of a card in a masked form. |
| msisdn | String | Conditional | An alias to access a payment account via a registered mobile phone number. |
| currency | Currency Code | Optional | |


## DICT_01_14 Account Details

**Remark:** The ASPSP shall give at least one of the account reference identifiers listed as optional below.

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| id | Max35Text | Conditional | This is the data element to be used in the path when retrieving data from a dedicated account. This shall be filled, if addressable resource are created by the ASPSP on the /accounts or /card-accounts endpoint. |
| iban | IBAN | Optional | This data element can be used in the body of the Consent Request Message for retrieving account access consent from this payment account. |
| bban | BBAN | Optional | This data element can be used in the body of the Consent Request Message for retrieving account access consent from this account. This data elements is used for payment accounts which have no IBAN. |
| pan | Max35Text | Optional | Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements. This data element can be used in the body of the Consent Request Message for retrieving account access consent from this card. |
| maskedPan | Max35Text | Optional | Primary Account Number (PAN) of a card in masked form. This data element can be used in the body of the Consent Request Message for retrieving account access consent from this card. |
| msisdn | Max35Text | Optional | An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service. The support of this alias must be explicitly documented by the ASPSP for the corresponding API Calls. |
| currency | Currency Code | Mandatory | Account currency |
| name | Max35Text | Optional | Name of the account given by the bank or the PSU in Online-Banking |
| accountType | Max35Text | Optional | Product Name of the Bank for this account, proprietary definition |
| cashAccountType | Cash Account Type | Optional | ExternalCashAccountType1Code from ISO20022 |
| bic | BICFI | Optional | The BIC associated to the account. |
| balances | Array of Balances | Conditional |
| _links | Links | Optional | Links to the account, which can be directly used for retrieving account information from this dedicated account. <br> Links to “balances” and/or “transactions” <br> These links are only supported, when the corresponding consent has been already granted. |


## DICT_01_15 Balances

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| closingBooked | Single Balance | Optional | Balance of the account at the end of the pre-agreed account reporting period. It is the sum of the opening booked balance at the beginning of the period and all entries booked to the account during the pre- agreed account reporting period. |
| expected | Single Balance | Optional | Balance composed of booked entries and pending items known at the time of calculation, which projects the end of day balance if everything is booked on the account and no other entry is posted. |
| authorized | Single Balance | Optional | The expected balance together with the value of a pre-approved credit line the ASPSP makes permanently available to the user. |
| openingBooked | Single Balance | Optional | Book balance of the account at the beginning of the account reporting period. It always equals the closing book balance from the previous report. |
| interimAvailable | Single Balance | Optional | Available balance calculated in the course of the account ’servicer’s business day, at the time specified, and subject to further changes during the business day. The interim balance is calculated on the basis of booked credit and debit items during the calculation time/period specified. |


## DICT_01_16 Single Balance

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| amount | Amount | Mandatory | | 
| lastActionDateTime | ISODateTime | Optional | This data element might be used to indicate e.g. with the expected or booked balance that no action is known on the account, which is not yet booked. |
| date | ISODate | Optional | |


## DICT_01_17 Account Report

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| booked | Array of transactions | Mandatory | |
| pending | Array of transactions | Optional | |
| _links | Links | Mandatory | The following links might be used within this context: <br> viewAccount (mandatory) <br> first (optional) <br> next (optional) <br> previous (optional) <br> last (optional) |
 
 
## DICT_01_18 Transctions 

| Attribute | Type | Condition | Description |
|:---------:|:----:|:---------:|-----------|
| transactionId | Max35Text | Optional | Can be used as access-ID in the API where more details on an transaction is offered. |
| endToEndId | Max35Text | Optional | |
| mandateId | Max35Text | Optional | Identification of Mandates, e.g. a SEPA Mandate ID |
| creditorId | Max35Text | Optional | Identification of Creditors, e.g. a SEPA Creditor ID |
| bookingDate | ISODate | Optional | |
| valueDate | ISODate | Optional | |
| amount | Amount | Mandatory | |
| creditorName | Max70Text | Optional | Name of the creditor if a "Debited" transaction |
| creditorAccount | Account Reference | Conditional | |
| ultimateCreditor | Max70Text | Optional | |
| debtorName | Max70Text | Optional | Name of the debtor if a "Credited" transaction. |
| debtorAccount | Account Reference | Conditional | |
| ultimateDebtor | Max70Text | Optional | | 
| remittanceInformationUnstructured | Max140Text | Optional | |
| remittanceInformationStructured | Max140Text | Optional | Reference to be transported in the field. |
| purposeCode | Purpose Code | Optional | |
| bankTransactionCode | Bank Transaction Code| Optional | Bank transaction code as used by the ASPSP in ISO20022 related formats. |


## DICT_01_19 Geo Location

Example for Format: "GEO:" <latitude>,<longitude>
 
 
## DICT_01_20 Frequency Code

The following codes from the EventFrequency7Code of ISO20022 are supported:
* Daily
* Weekly 
* EveryTwoWeeks
* Monthly 
* EveryTwoMonths
* Quaterly
* SemiAnnual
* Annual

## DICT_01_21 Other ISO-related basic Types

The following codes and definitions are used from ISO20022
* Purpose Code: ExternalPurpose1Code
* Cash Account Type: ExternalCashAccountType1Code
* BankTransactionCode: ExternalBankTransactionDomain1Code
* BICFI: BICFIIdentifier
* IBAN: IBAN2007Identifier
* BBAN: BBANIdentifier
* Floating Point Number: Same definitions as done by the ActiveOrHistoricCurrencyAndAmount definition on the amount

For all codes used in JSON structures not the abbreviation defined for XML encoding, but the name of the code is used as value.

The following codes are used from other ISO standards: 
* Currency Code: Codes following ISO 4217

Further basic ISO data types:
* ISODateTime: A particular point in the progression of time defined by a mandatory date and a mandatory time component, expressed in either UTC time format (YYYY-MM-DDThh:mm:ss.sssZ), local time with UTC offset format (YYYY- MM-DDThh:mm:ss.sss+/-hh:mm), or local time format (YYYY- MMDDThh:mm:ss.sss). These representations are defined in "XML Schema Part 2: Datatypes Second Edition - W3C Recommendation 28 October 2004" which is aligned with ISO 8601.
* ISODate: A particular point in the progression of time in a calendar year expressed in the YYYY-MM-DD format.cd 

