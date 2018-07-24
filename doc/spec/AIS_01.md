# XS2A interface –> Core services-> Account Information Service


## AIS_01 Account Information Service (AIS).
This service may be used by an AISP to request different types of information about the account of a PSU:
 * Transaction reports
 * Balances
 * List of availlable accounts
 * Account details
 
 The account is managed by the ASPSP providing the XS2A Interface.


### Account Information Service Flows
The Account Information Service is separated in two phases.


#### Establish Account Information Consent  
PSU is giving consent on
  * the type type of Account Information Service to grant an access to,
  * the Multiplicity of the Account Information Service, i.e. a once-off or
      recurring access, and
  * in the latter case on the duration of the consent in days or as maximally
      offered by the ASPSP and optionally the frequency of a recurring request.
      

    With Redirect SCA Approach
    If the ASPSP supports the Redirect SCA Approach, the message flow within the Account Information Consent sub-service is simple. 
    The Account Information Consent Request is followed by a redirection to the ASPSP SCA authorization site. 
    A status or content request on the created consent resource might be requested by the TPP after the session is reredirected to the TPP’s system.
    
![Account Information Consent Flow for Redirect Approach](img/AIS_Consent_Redirect.png)


    With OAuth2 SCA Approach
    If the ASPSP supports the OAuth2 SCA Approach, the flow is very similar to the Redirect SCA Approach. 
    Instead of redirecting the PSU directly on an authentication server, the OAuth2 protocol is used for the transaction autorization process.
    
![Account Information Consent Flow for OAuth2 Approach](img/AIS_Consent_OAuth2.png)


#### Read Account Data  
The AISP gets access to the account data as defined by the PSU's consent. The Read Account Data Request will indicate 
  * the type of the account data to be accessed
  * the identification of the addressed account where applicable
  * whether a PSU has directly initiated the request real-time
  * whether balances should be delivered in addition where applicable
  * in case of transaction reports as Account Information type additionally    
    * the addressed account identification and
    * the period of the transaction report
    * in addition optionally a delta-flag indicating the request for a delta-report relative to the last request with additional data
    * the preferred formats of the transaction reports
 
    
![Read Account Data Flow](img/AIS_Account.png)



### Data overview Account Information Service
The following table defines the technical description of the abstract data model as defined for the account information service. The columns give an overview on the API
protocols as follows:

* The **"Data element"** column is using the abstract data elements to deliver the connection to rules and role definitions.
* The **"Attribute encoding"** is giving the actual encoding definition within the XS2A API.
* The **"Location"** columns define, where the corresponding data elements are transported as https parameters, resp. are taken from e-IDas certificates. There are:
    * Path
    * Header
    * Body
    * Certificate
* The **"Usage"** column gives an overview on the usage of data elements in the different services and API Calls. These calls will be technically realised as HHTPS POST, PUT and GET commands. There are:
    * Information Consent Request
    * Information Consent Response
    * Update Data Request 
    * Update Data Response
    * Status Request
    * Status Response
    * Read Data Request
    * Read Data Response

The calls are divided into the following calls for Account Information:
* The Information Request which shall be the first API Call for every
      transaction within the corresponding XS2A Payment Initiation service. 
* The Update Data Call is a call, where the TPP needs to add PSU related
      data, which is requested in the return of the first call. This call might be
      repeated.
* The Get Data Request is the request to retrieve Account Information data.
* The Status Request is used in cases, where the SCA control is taken
      over by the ASPSP and the TPP needs later information about the
      outcome.

The following usage of abbreviations in the Location and Usage columns is defined:
* x: This data element is transported on the corresponding level.
* m: Mandatory
* o : Optional for the TPP to use
* c: Conditional. The Condition is described in the API Calls, condition defined by
  the ASPSP

| Data element                        | Attribute encoding                | Path | Query Param. | Header | Body | Certificate | Establ. Cons. Req. | Establ. Cons. Resp. | Upd. Req. | Upd. Resp. | Stat. Req. | Stat. Resp. | Read D. Req. | Read D. Resp. |
|-------------------------------------|-----------------------------------|:----:|:---:|:------:|:----:|:-----------:|:---------------:|:----------------:|:---------:|:----------:|:----------:|:-----------:|:------------:|:-------------:|
| Provider Identification             |                                   |   x  |     |        |      |             |        m        |                  |     m     |            |     m      |             |      m       |               | 
| TPP Registration Number             |                                   |      |     |        |      |      x      |        m        |                  |     m     |            |     m      |             |      m       |               |
| TPP Name                            |                                   |      |     |        |      |      x      |        m        |                  |     m     |            |     m      |             |      m       |               |
| TPP Role                            |                                   |      |     |        |      |      x      |        m        |                  |     m     |            |     m      |             |      m       |               |
| TPP National Competent Authority    |                                   |      |     |        |      |      x      |        m        |                  |     m     |            |     m      |             |      m       |               |
| Transaction Identification          | TPP-Transaction-ID                |      |     |    x   |      |             |        m        |                  |     m     |            |     m      |             |      m       |               |
| Request Identification              | x-request-id                    |      |     |    x   |      |             |        m        |                  |     m     |            |     m      |             |      m       |               |
| Resource ID                         | consentId                         |      |     |        |  x   |             |                 |        m         |           |            |            |             |              |               |
| Resource-ID[^5]                     | Consent-ID                        |      |     |    x   |      |             |                 |                  |           |            |            |             |              |      c        |
| Access Token (from optional OAuth2) | Authorization Bearer              |      |     |    x   |      |             |        c        |                  |     c     |            |     c      |             |      c       |               |
| TPP Signing Certificate Data        | TPP-Certificate                   |      |     |    x   |      |             |        c        |                  |     c     |            |     c      |             |      c       |               |
| TPP Signing Electronic Signature    | Signature                         |      |     |    x   |      |             |        c        |                  |     c     |            |     c      |             |      c       |               |
| Further signature related data      | Digest                            |      |     |    x   |      |             |        c        |                  |     c     |            |     c      |             |      c       |               |
| Service Type                        |                                   |   x  |     |        |      |             |        m        |                  |     m     |            |     m      |             |      m       |               |
| Response Code                       |                                   |      |     |    x   |      |             |                 |        m         |           |     m      |            |     m       |              |      m        |
| Transaction Status                  | transactionStatus                 |      |     |        |  x   |             |                 |        m         |           |     m      |            |     m       |              |               |
| PSU Message Information             | psuMessage                        |      |     |        |  x   |             |                 |        o         |           |     o      |            |     o       |              |      o        |
| TPP Message Information             | tppMessages                       |      |     |        |  x   |             |                 |        o         |           |     o      |            |     o       |              |      o        |
| PSU Identification                  | PSU-ID                            |      |     |    x   |      |             |        c        |                  |     c     |            |            |             |              |               |
| PSU Identification Type             | PSU-ID-Type                       |      |     |    x   |      |             |        c        |                  |     c     |            |            |             |              |               |
| Corporate Identification            | PSU-Corporate-ID                  |      |     |    x   |      |             |        c        |                  |     c     |            |     c      |             |              |               |
| Corporate Type                      | PSU-Corporate-ID-Type             |      |     |        |      |             |                 |                  |           |            |            |             |              |               |
| IP Address PSU                      | PSU-IP-Address                    |      |     |    x   |      |             |        m        |                  |           |            |            |             |              |               |
| PSU Agent                           | PSU Agent                         |      |     |    x   |      |             |        o        |                  |           |            |            |             |              |               |
| GEO Information                     | PSU-Geo-Location                  |      |     |    x   |      |             |        o        |                  |           |            |            |             |              |               |
| Redirect URL ASPSP                  | _links.redirect                   |      |     |        |  x   |             |                 |        c         |           |            |            |             |              |               |
| Redirect Preference                 | tppRedirectPreferred              |      |  x  |        |      |             |        o        |                  |           |            |            |             |              |               |
| Redirect URL TPP                    | TPP-Redirect-URI                  |      |     |    x   |      |             |        c        |                  |           |            |            |             |              |               |
| PSU Account                         | psuAccount                        |      |     |        |  x   |             |                 |                  |           |            |            |             |      c       |               |
| PSU Account List                    | accessAccounts                    |      |     |        |  x   |             |        m        |                  |           |            |            |             |              |               |
| Date From                           | dateFrom                          |      |  x  |        |      |             |                 |                  |           |            |            |             |      c       |               |
| Date To                             | dateTo                            |      |  x  |        |      |             |                 |                  |           |            |            |             |      c       |               |
| Booking Status                      | bookingStatus                     |      |  x  |        |      |             |                 |                  |           |            |            |             |      c       |               |
| Delta Indicator                     | deltaList                         |      |  x  |        |      |             |                 |                  |           |            |            |             |      c       |               |
| With Balance Flag                   | withBalance                       |      |  x  |        |      |             |                 |                  |           |            |            |             |      c       |               |
| PSU Involvement Flag                | psuInvolved                       |      |  x  |        |      |             |                 |                  |           |            |            |             |      c       |               |
| Validity Period                     | validUntil                        |      |     |        |  x   |             |        m        |                  |           |            |            |             |              |               |
| Frequency                           | frequencyPerDay                   |      |     |        |  x   |             |        m        |                  |           |            |            |             |              |               |
| Recurring Indicator                 | recurringIndicator                |      |     |        |  x   |             |        m        |                  |           |            |            |             |      c       |               |
| Combined service                    | combinedServiceIndicator          |      |     |        |  x   |             |        m        |                  |           |            |            |             |              |               |



### Multicurrency Accounts
Definition: A multicurrency account is an account which is a collection of different sub- accounts which are all addressed by the same account identifier like an IBAN by e.g. payment initiating parties. The sub-accounts are legally different accounts and all differ in their currency, balances and transactions. An account identifier like an IBAN together with a currency always addresses uniquely a sub-account of a multicurrency account.
This specification supports to address multicurrency accounts either on collection or on sub- account level. The currency data attribute in the corresponding data structure "Account Reference" allows to build structures like

    {"iban": "DE87123456781234567890"}

or

    {"iban": "DE87123456781234567890",
     "currency": "EUR"}

If the underlying account is a multicurrency account, then
* the first reference is referring to the collection of all sub-accounts addressable by this IBAN, and
* the second reference is referring to the euro sub-account only.

This interface specification is acting on sub-accounts of multicurrency accounts in exactly the
same way as on regular accounts.
The methods on multicurrency accounts differ in the inter-face due to the fact, that a collection of accounts is addressed. In the following the differences are described on abstract level.

**Multicurrency Accounts in Submission of Consents**

Multicurrency accounts are addressed by just using the external account identifier in the submission of a consent on dedicated accounts, without specifying a currency. Asking for the consent to retrieve account information data of a multicurrency accounts implies getting it for all sub-accounts.

**Multicurrency Accounts in Reading Accounts or Account Details**

The ASPSP will decide in its implementation whether to grant data access to a multicurrency account on aggregation level, on aggregation and sub-account level, or only on sub-account level.

**Multicurrency Accounts in Reading Balances**

The consequence for this function is that an array of balances of all sub-accounts are returned, if a multicurrency account is addressed on aggregation level.

**Multicurrency Accounts in Reading Transactions**

The consequence for this function is that the list of transactions will contain all transactions of all sub-accounts, if a multicurrency account is addressed on aggregation level. In this case the payment transactions contained in the report may have different transaction currencies.

### Establish Account Information Consent
#### AIS_01_01 Consent Request

#### AIS_01_01_01 Consent Request on Dedicated Accounts

##### Call
    POST /v1/consents
Creates an account information consent resource at the ASPSP regarding access to accounts specified in this request.

##### Side Effects
When this Consent Request is a request where the "recurringIndicator" equals "true" and if it exists already a former consent for recurring access on account information for the addressed PSU, then the former consent automatically expires as soon as the new consent request is authorized by the PSU.
 
##### Query Parameters
| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| tppRedirectPreferred	| Boolean	| Optional |	If it equals "true", the TPP prefers a redirect over an embedded SCA approach. If it equals "false", the TPP prefers not to be redirected for SCA. The ASPSP will then choose between the Embedded or the Decoupled SCA approach, depending on the choice of the SCA procedure by the TPP/PSU. If the parameter is not used, the ASPSP will choose the SCA approach to be applied depending on the SCA method chosen by the TPP/PSU. |
| withBalance	| Boolean	| [Optional] |	This parameter may only be used together with the access sub attribute "available- accounts" in the request body. The request is rejected if the ASPSP is not supporting this parameter. If the ASPSP accepts this parameter in the /consents endpoint, he shall also accept it for the GET access method on the /accounts endpoint. |


##### Request Header

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| TPP-Transaction-ID	| UUID	|Mandatory|	ID of the transaction as determined by the initiating party|
| x-request-id	| UUID|  	Mandatory	| ID of the request, unique to the call, as determined by the initiating party.|
| PSU-ID|	String	|Conditional	|Might be mandated in the ASPSP’s documentation. Is not contained if the optional OAuth Pre-Step was performed.|
| PSU-ID-Type |	String	|Conditional	|Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. |
| PSU-Corporate-ID |	String |	Conditional |	Might be mandated in the ASPSP's documentation. Only used in a corporate context. |
| PSU-Corporate-ID-Type |	String |	Conditional |	Might be mandated in the ASPSP's documentation. Only used in a corporate context. |
| Authorization Bearer |	String |	Conditional |	Is contained only, if the optional OAuth2 Pre-Step was performed to authenticate the PSU. |
| TPP-Redirect-URI | String | Conditional | URI of the TPP, where the transaction flow shall be redirected to after a Redirect. Shall be contained at least if the tppRedirectPreferred parameter is set to true or is missing. |
| Signature	| String |	Conditional |	A signature of the request by the TPP on application level. This might be mandated by ASPSP. |
| certificate	|String |	Conditional	| The certificate used for signing the request in base64 encoding. Shall be contained if the signature is used. |


##### Request body

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| access	| Account Access | Mandatory | Requested access service. Only the sub attributes with the tags "accounts", "balances" and "transactions" are accepted for this request. |
| recurringIndicator | boolean | Mandatory | "true", if the consent is for recurring access to the account data or "false", if the consent is for one access to the account data |
| validUntil | ISODate | Mandatory | This parameter is requesting a valid until referenceDate for the requested consent. The content is the local ASPSP referenceDate in ISODate Format, e.g. 2017-10-30. If a maximal available referenceDate is requested, a referenceDate in far future is to be used: "9999-12-31". The consent object to be retrieved by the GET Consent Request will contain the adjusted referenceDate. |
| frequencyPerDay | Integer | Mandatory | This field indicates the requested maximum frequency for an access per day. For a once-off access, this attribute is set to "1". |
| combinedServiceIndicator | boolean | Mandatory | If "true" indicates that a payment initiation service will be addressed in the same "session". |

**Note:** All permitted "access" attributes ("accounts", "balances" and "transactions") used in this message shall carry a non-empty array of account references, indicating the accounts where the type of access is requested. Please note that a "transactions", "balances" or "accounts" access right also gives access to the generic /accounts endpoints, i.e. is implicitly supporting also the "accounts" access. 

This specification mandates the ASPSP to support all POST consent requests with dedicated accounts, i.e. POST requests with the above mentioned sub-attributes, where at least one sub-attribute is contained, and where all contained sub-attributes carry a non-empty array of account references. This results in a consent on dedicated accounts. For this Consent Request on Dedicated Accounts, no assumptions are made for the SCA Approach by this specification. 

Optionally, the ASPSP can support also Consent Requests, where the above mentioned sub-attributes "accounts", "balances" and "transactions" only carry an empty array or where the sub-attributes "available-accounts" or "allPsd2" are used – both with the value "all- accounts".


##### Response Header    
The Location field is used as hyperlink to the status of the created resource. No other specific requirements.


##### Response Body
| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| transactionStatus | TransactionStatus | Mandatory | Authentication status of the consent. |
| consentId | String | Conditional | Identification of the consent resource as it is used in the API structure. Shall be contained, if a consent resource was generated. | 
| scaMethods | Array of Authentication Object |	Conditional |	This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods. Depending on the risk management of the ASPSP this choice might be offered before or after the PSU has been identified with the first relevant factor, or if an access token is transported. If this data element is contained, then there is also an hyperlink of type "select_authentication_methods" contained in the response body. These methods shall be presented towards the PSU for selection by the TPP. |
| _links | Links  |	Mandatory |	A list of hyperlinks to be recognized by the TPP. Type of links admitted in this response, (further links might be added for ASPSP defined extensions): <br><br> **"redirect":** In case of an SCA Redirect Approach, the ASPSP is transmitting the link  to which to redirect the PSU browser. <br><br> **"oAuth":** In case of an OAuth2 based Redirect Approach the ASPSP is transmitting the link where the configuration of the OAuth2 Server is defined. <br><br> **"updatePsuIdentification":** The link to the payment initiation resource, which needs to be updated by the psu identification. This  might be used in a redirect or decoupled approach, where the PSU ID was missing in the first request. <br><br> **"updatePsuAuthentication":** The link to the payment initiation resource, which need to be updated by a psu password and  eventually the psu identification if not delivered yet. This is used in a case of the Embedded SCA approach. <br><br> **"selectAuthenticationMethod":** This is a link to a resource, where the TPP can select  the applicable strong customer authentication methods for the PSU, if there  were several available authentication methods. This link contained under exactly the same conditions as the data element “authentication_methods”, see above. <br><br> **“status”:** The link to retrieve the transaction status of the account information consent. |
| psuMessage | String  |	Optional | Text to be displayed to the PSU |


##### Example

*Request*

    POST https://api.testbank.com/v1/consents
    Content-Encoding        gzip
    Content-Type            application/json
    TPP-Transaction-ID      3dc3d5b3-7023-4848-9853-f5400a64e80g
    x-request-id          99391c7e-ad88-49ec-a2ad-99ddcb1f7756
    PSU-IP-Address          192.168.8.78
    PSU-Agent               Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0
    Date                    Sun, 06 Aug 2017 15:02:37 GMT
    {
     "access":
        {"balances": 
           [{"iban": "DE2310010010123456789"}},
            {"iban": "DE2310010010123456790",
             "currency": "USD"},
            {"iban": "DE2310010010123456788"}],
        "transactions":
           [{"iban": "DE2310010010123456789"},
            {"maskedPan": "123456xxxxxx1234"}
           ]
        }   
     "recurringIndicator": "true",
     "validUntil": "2017-11-01",
     "frequencyPerDay" : "4"
    }


*Response in case of a redirect*

Response Code: 

    200


Response Header:

    Location "v1/consents/1234-wertiq-983"


Response Body:

    {
     "transactionStatus" : "Received",
     "consentId":          "1234-wertiq-983",
     "_links" {
         "redirect" : "www.testbank.com/authentication/1234-wertiq-983"
      }
    }

*Response in case of the OAuth2 approach*

Response Code:

    201

Response Header:

    Location "v1/consents/1234-wertiq-983"

Response Body:

    {
     "transactionStatus" : "Received",
     "consentId":          "1234-wertiq-983",
     "_links" {
        "self" : "/v1/consents/1234-wertiq-983",
        "consentId": "1234-wertiq-983"
     }
    }

    
    
#### Consent Request on Account List or without Indication of Accounts

##### AIS_01_01_02 Consent Request on Account List of Available Accounts
This function is supported by the same call as the Consent Request on Dedicated Accounts. The only difference is that the call only contains the "available-accounts" sub attribute within the "access" attribute with value "all-accounts".
In this case the call creates an account information consent resource at the ASPSP to return a list of all available accounts. For this specific Consent Request, no assumptions are made for the SCA Approach by this specification.

##### AIS_01_01_03 Consent Request without Indication of Accounts
This function is supported by the same call as the Consent Request on Dedicated Accounts. The only difference is that the call contains the "accounts", "balances" and/or "transactions" sub attribute within the "access" attribute all with an empty array.
The ASPSP will then agree bilaterally directly with the PSU on which accounts the requested access consent should be supported. The result can be retrieved by the TPP by using the GET Consent Request method. 

##### AIS_01_01_04 Consent Request for Access to all Accounts for all PSD2 defined AIS
This function is supported by the same call as the Consent Request on Dedicated Accounts. The only difference is that the call contains the "allPsd2" sub attribute within the "access" attribute with the value "all-accounts".
If this function is supported, it will imply a consent on all available accounts of the PSU on all PSD2 related account information services. For this specific Consent Request, no assumptions are made for the SCA Approach by this specification.

##### Example Consent on Account List of Available Accounts

*Request*

    POST https://api.testbank.com/v1/consents 
    Content-Encoding:       gzip
    Content-Type:           application/json
    TPP-Transaction-ID:     3dc3d5b3-7023-4848-9853-f5400a64e80g 
    x-request-id:          99391c7e-ad88-49ec-a2ad-99ddcb1f7756 
    PSU-IP-Address:         192.168.8.78
    PSU-Agent:              Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0
    Date:                   Sun, 06 Aug 2017 15:05:37 GMT

    {"access":
       {"available-accounts": "all-accounts"}, 
     "recurringIndicator": "false", 
     "validUntil": "2017-08-06",
     "frequencyPerDay": "1"
    }

##### Example Consent without dedicated Account

*Request*

    POST https://api.testbank.com/v1/consents 
        Content-Encoding:       gzip
        Content-Type:           application/json
        TPP-Transaction-ID:     3dc3d5b3-7023-4848-9853-f5400a64e80g 
        x-request-id:         99391c7e-ad88-49ec-a2ad-99ddcb1f7756 
        PSU-IP-Address:         192.168.8.78
        PSU-Agent:              Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0
        Date:                   Sun, 06 Aug 2017 15:05:37 GMT
    
        {"access":
           {"balances": [],
            "transactions": []},
         "recurringIndicator": "true", 
         "validUntil": "2017-11-01",
         "frequencyPerDay": "4"
        }
    
    
#### AIS_01_02 Get Status Request 

##### Call

    GET /v1/consents/{consentId}/status
Can check the status of an account information consent resource.


##### Path

| Attribute | Type  | Description |
|--------|------------------|----------|
| consentId | String | The consent identification assigned to the created resource. |

##### Query Parameters
No specific query parameters.

##### Request Header
See Request Header under AIS_01_01.


##### Request Body
No body.


##### Response Body

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| transactionStatus |  |  | This is the "authentication status" of the consent. 


##### Example

*Request*
    
    GET https://api.testbank.com/v1/consents/qwer3456tzui7890/status
    
 
 *Response*
 
    Response Code 200
    
    {
     "transactionStatus" : "AcceptedTechnicalValidation",
    }
    


#### AIS_01_03 Get Consent Request 

##### Call

    GET /v1/consents/{consentId}
    
Returns the content of an account information consent object. This is returning the data for the TPP especially in cases, where the consent was directly managed between ASPSP and PSU e.g. in a re-direct SCA Approach.

##### Query Parameters
No specific query parameters.

##### Path

| Attribute | Type  | Description |
|--------|------------------|----------|
| consentId | String | ID of the corresponding consent object as returned by Account Information Consent Request |


##### Request Header
See Request Header under AIS_01_01. 


##### Request Body
No body.


##### Response Body

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| access | Account Access | Mandatory | Requested access service. Only the sub attributes with the tags "accounts", "balances" and "transactions" are accepted for this request. |
| recurringIndicator | Boolean | Mandatory | "true", if the consent is for recurring access to the account data or "false", if the consent is for one access to the account data |
| validUntil | ISODate | Mandatory | This parameter is a valid until referenceDate for the requested consent. The content is the local ASPSP referenceDate in ISODate Format, e.g. 2017-10-30 |
| frequencyPerDay | Integer | Mandatory | This field indicates the requested maximum frequency for an access per day. For a once-off access, this attribute is set to "1". |
| lastActionDate | ISODate | Mandatory | This referenceDate is containing the referenceDate of the last action on the consent object either trough the XS2A interface or the PSU/ASPSP interface having an impact on the status. |
| transactionsStatus | Transaction Status | Mandatory | |
| consentStatus | String | Mandatory | The following code values are permitted "empty", "valid", "blocked", "expired", "deleted". These values might be extended by ASPSP by more values. |


##### Example

*Request*
    
    GET https://api.testbank.com/v1/consents/qwer3456tzui7890?
    
*Response* 

    {
     "access": 
        {"balances":
           [{"iban": "DE2310010010123456789"}]
        {"transactions" :
           [{"iban": "DE2310010010123456789"},
            {"pan": "123456xxxxxx3457"}]

     "recurringIndicator": "true",
     "validUntil": "2017-11-01",
     "frequencyPerDay" : "4",
     "transactionStatus" : "AcceptedTechnicalValidation",
     "consentStatus": "valid",
     "_links": {"viewAccounts": "/v1/accounts"}
    }

**Remark:** This specification supports no detailed links to AIS service endpoints corresponding to this account. This is due to the fact, that the /accounts endpoint will deliver all detailed information, including the hyperlinks e.g. to the balances or transactions of certain accounts. Still due to the guiding principles, the ASPSP may deliver more links in addition, which then will be documented in the ASPSPs XS2A API documentation. 


#### AIS_01_04 Delete Account Information Consent Object

##### Call

    DELETE /v1/consents/{consentId}
Deletes a given consent.


##### Path

| Attribute | Type  | Description |
|--------|------------------|----------|
| consentId | String | Contains the resource id of the consent to be deleted. |

##### Query Parameters
No specific query parameters.

##### Request Header

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| TPP-Transaction-ID | UUID | Mandatory | ID of the transaction as determined by the initiating party. |
| x-request-id | UUID | Mandatory | |
| Authorization Bearer | String | Conditional | Is contained only, if an OAuth2 based SCA was performed in the corresponding consent transaction. |


##### Request Body
No body.


##### Response Body
No body.


##### Example

*Request*
    
    DELETE https://api.testbank.com/v1/consents/qwer3456tzui7890
    Content-Encoding      gzip
    Content-Type          application/json
    TPP-Transaction-ID     3dc3d5b3-7023-4848-9853-f5400a64e812
    Request-ID            99391c7e-ad88-49ec-a2ad-99ddcb1f7757
    Date                  Sun, 13 Aug 2017 17:05:37 GMT
    
 
 *Response*
 
    Response Code 204
    
#### AIS_01_05 Read Account Data Request

#### AIS_01_05_01 Read Account List

##### Call

    GET /v1/accounts/{query-parameters}
Reads a list of accounts. It is assumed that a consent of the PSU to this access is already given and stored on the ASPSP system. The adressed list of accounts depends then on the PSU ID and the stored consent addressed by consent-id, respectively the OAuth2 Token.

**Note:** If the consent is granted only to show the list of available accounts, much less details are displayed about the accounts. Specifically hyperlinks to balances or transaction endpoint should not delivered then.
  
**Note:** If the details returned in this call with the access rights "accounts", "balances", "transactions" or "allPsd2" are not sufficient, then more details can be retrieved by addressing the /accounts/{account-id} endpoint.
  
  
##### Query Parameters

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| withBalance | Boolean | [Optional] | If contained this function reads the list of accessible payment accounts including the booking balance. This call will be rejected if the withBalance parameter is used in a case, where the access right on balances is not granted in the related consent or if the ASPSP does not support the withBalance parameter. |
| psuInvolved | Boolean | Conditional | It must be contained if the PSU has asked for this account access in real-time. This flag is then set to "true". The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient. |


##### Request Header

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| TPP-Transaction-ID	| UUID	|Mandatory|	ID of the transaction as determined by the initiating party|
| x-request-id	| UUID|  	Mandatory	| ID of the request, unique to the call, as determined by the initiating party.|
| Consent-ID | String | Mandatory | Shall be contained if "Establish Consent Transaction" was performed via this API before. |
| Authorization Bearer |	String |	Conditional |	Is contained only, if the optional OAuth2 Pre-Step was performed or an OAuth2 based SCA was performed in the related consent authorization. |
| Signature | String | Conditional | A signature of the request by the TPP on application level. This might be mandated by ASPSP. |
| TPP-Certificate | String | Conditional | The certificate used for signing the request in base64 encoding. It shall be contained if a signature is used, see above. |


##### Response Body

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| accountList | Array of Account Details | Mandatory | |


##### Example

*Response in case of an example where the consent has been given on two different IBANs*
   
    {“account-list”: 
       [
          {“id”:	“3dc3d5b3-7023-4848-9853-f5400a64e80f”, 
          "iban": "DE2310010010123456789",
          "currency": "EUR", 
          "accountType": "Girokonto",
          “cashAccountType”: “CurrentAccount”, 
          “name”: “Main Account”,
          “_links” : {
             “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/balances”,
             “transactions”	:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/transactions”}
          },
          {“id” : “3dc3d5b3-7023-4848-9853-f5400a64e81g”, 
          "iban": "DE2310010010123456788",
          “currency” : “USD”,
          “accountType”: “Fremdwährungskonto”, 
          “cashAccountType” : “CurrentAccount”, 
          “name” : “US Dollar Account”, 
          “_links” : {
              “balances”	:	“/v 1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e81g/balances” }
          }
       ]
    }
     
*Response in case of an example where consent on transactions and balances has been given to a multicurrency account which has two sub-accounts with currencies EUR and USD, and where the ASPSP is giving the data access only on sub-account level:*
     
     {“account-list”: 
        [
           {“id”:	“3dc3d5b3-7023-4848-9853-f5400a64e80f”, 
            "iban": "DE2310010010123456788",
            "currency": "EUR", 
            “accountType”: “Girokonto”,
            “cashAccountType”: “CurrentAccount”, 
            “name”: “Main Account”,
            “_links”: {
              “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/balances”,
              “transactions”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/transactions”}
            },
            {“id” : “3dc3d5b3-7023-4848-9853-f5400a64e81g”, 
            "iban": "DE2310010010123456788",
            “currency” : “USD”,
            “accountType”: “Fremdwährungskonto”, 
            “cashAccountType” : “CurrentAccount”, 
            “name” : “US Dollar Account”, 
            “_links” : {
                “balances”	:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e81g/balances”,
                “transactions”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e81g/transactions” }
            }
        ]
     }

*Response in case of an example where consent on balances and transactions has been given to a multicurrency account which has two sub-accounts with currencies EUR and USD and where the ASPSP is giving the data access on aggregation level and on sub-account level:*

    {“account-list”: 
       [
           {“id”:	“3dc3d5b3-7023-4848-9853-f5400a64e80f”, 
           "iban": "DE2310010010123456788",
           "currency": "XXX",
           “accountType”: “Multi currency account”, 
           “cashAccountType”: “CurrentAccount”, 
           “name”: “Aggregation Account”,
           “_links”: {
               “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e333/balances”,
               “transactions”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e333/transactions”}
           },
           {“id”:		“3dc3d5b3-7023-4848-9853-f5400a64e80f”, 
           "iban": "DE2310010010123456788",
           "currency": "EUR", 
           “accountType”: “Girokonto”,
           “cashAccountType”: “CurrentAccount”, 
           “name”: “Main Account”,
           “_links”: {
               “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/balances”,
               “transactions”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/transactions”}
           },
           {“id” : “3dc3d5b3-7023-4848-9853-f5400a64e81g”, 
           "iban": "DE2310010010123456788",
           “currency” : “USD”,
           “accountType”: “Fremdwährungskonto”, 
           “cashAccountType” : “CurrentAccount”, 
           “name” : “US Dollar Account”, 
           “_links” : {
               “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e81g/balances”,
               “transactions”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e81g/transactions” }
           }
       ]
    }

### AIS_01_05_02 Read Account Details
    
##### Call

    GET /v1/accounts/{account-id} {query-parameters}
    
Reads details about an account, with balances where required. It is assumed that a consent of the PSU to this access is already given and stored on the ASPSP system. The addressed details of this account depends then on the stored consent addressed by consentId, respectively the OAuth2 access token. NOTE: The account-id can represent a multicurrency account. In this case the currency code is set to "XXX".

##### Query Parameters

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| withBalance | Boolean | [Optional] | If contained this function reads the list of accessible payment accounts including the booking balance. This call will be rejected if the withBalance parameter is used in a case, where the access right on balances is not granted in the related consent or if the ASPSP does not support the withBalance parameter. |
| psuInvolved | Boolean | Conditional | It must be contained if the PSU has asked for this account access in real-time. This flag is then set to "true". The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient. |

##### Request Header

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| TPP-Transaction-ID	| UUID	|Mandatory|	ID of the transaction as determined by the initiating party|
| x-request-id	| UUID|  	Mandatory	| ID of the request, unique to the call, as determined by the initiating party.|
| Consent-ID | String | Mandatory | Shall be contained if "Establish Consent Transaction" was performed via this API before. |
| Authorization Bearer |	String |	Conditional |	Is contained only, if the optional OAuth2 Pre-Step was performed or an OAuth2 based SCA was performed in the related consent authorization. |
| Signature | String | Conditional | A signature of the request by the TPP on application level. This might be mandated by ASPSP. |
| TPP-Certificate | String | Conditional | The certificate used for signing the request in base64 encoding. It shall be contained if a signature is used, see above. |


##### Response Body

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| account | Account Details | Mandatory | |


##### Example

*Response for a regular account*
   
    {“account”: 
       {“id”:	“3dc3d5b3-7023-4848-9853-f5400a64e80f”, 
       "iban": "DE2310010010123456789",
       "currency": "EUR", 
       "accountType": "Girokonto",
       “cashAccountType”: “CurrentAccount”, 
       “name”: “Main Account”,
       “_links” : {
          “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/balances”,
          “transactions”	:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/transactions”}
       }
    }

*Response for a multi-currency account*
   
    {“account”: 
       {“id”:	“3dc3d5b3-7023-4848-9853-f5400a64e80f”, 
       "iban": "DE2310010010123456789",
       "currency": "XXX", 
       "accountType": "Multicurrency Account",
       “cashAccountType”: “CurrentAccount”, 
       “name”: “Aggregation Account”,
       “_links” : {
          “balances”:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/balances”,
          “transactions”	:	“/v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/transactions”}
       }
    }



### AIS_01_05_03 Read Balance

##### Call

    GET /v1/accounts/{account-id}/balances
Reads account data from a given account addressed by "account-id". 

Remark: This account-id can be a tokenized identification due to data protection reason.


##### Path

| Attribute | Type  | Description |
|--------|------------------|----------|
| account-id | String | This identification is denoting the addressed account. The account-id is retrieved by using a "Read Account List" call. The account-id is the “id” attribute of the account structure. Its value is constant at least throughout the lifecycle of a given consent.


##### Query Parameters

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| psuInvolved | Boolean | Conditional | It must be contained if the PSU has asked for this account access in real-time. This flag is then set to "true". The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient. |


##### Request Header

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| TPP-Transaction-ID	| UUID	|Mandatory|	ID of the transaction as determined by the initiating party|
| x-request-id	| UUID|  	Mandatory	| ID of the request, unique to the call, as determined by the initiating party.|
| Consent-ID | String | Mandatory | Shall be contained if "Establish Consent Transaction" was performed via this API before. |
| Authorization Bearer |	String |	Conditional |	Is contained only, if the optional OAuth2 Pre-Step was performed or an OAuth2 based SCA was performed in the related consent authorization. |
| Signature | String | Conditional | A signature of the request by the TPP on application level. This might be mandated by ASPSP. |
| TPP-Certificate | String | Conditional | The certificate used for signing the request in base64 encoding. It shall be contained if a signature is used, see above. |


##### Response Body

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| balances | Balances | Mandatory | A list of balances regarding this account, e.g. the current balance or the last booked balance. |


##### Example

*Response in case of a regular account:*

    {
       “balances” :
          [{“closingBooked”:
             {
              “balanceAmount”: {“currency” : “EUR”, “content”: “500.00”}, 
              “referenceDate” : “2017-10-25”
             },
          “expected”:
             {
              “balanceAmount”: {“currency” : “EUR” ,“content” : “900.00”}, 
              “lastChangeDateTime” : “2017-10-25T15:30:35.035Z”
             }
          }]
    }

*Response in case of a multicurrency account with one account in EUR, one in USD, where the ASPSP has delivered a link to the balance endpoint relative to the aggregated multicurrency account (aggregation level):*

    {
       “balances” :
          [{“closingBooked”:
             {
              “balanceAmount”: {“currency” : “EUR”, “content”: “500.00”}, 
              “referenceDate” : “2017-10-25”
             },
          “expected”:
             {
              “balanceAmount”: {“currency” : “EUR” ,“content” : “900.00”}, 
              “lastChangeDateTime” : “2017-10-25T15:30:35.035Z”
             }
          },
          {“closingBooked”:
             {
              “balanceAmount”: {“currency” : “USD”, “content”: “350.00”}, 
              “referenceDate” : “2017-10-25”
             },
          “expected”:
             {
              “balanceAmount”: {“currency” : “USD” ,“content” : “350.00”}, 
              “lastChangeDateTime” : “2017-10-25T15:30:35.035Z”
             }]
    }
     

### AIS_01_05_04 Read Transaction List

##### Call

     GET /v1/accounts/{account-id}/transactions{query-parameters}
Reads account data from a given account addressed by "account-id".

Remark: If the ASPSP is not providing the "GET Account List" call, then the ASPSP must accept e.g. the PSU IBAN as account-id in this call.


##### Path

| Attribute | Type  | Description |
|--------|------------------|----------|
| account-id | String | This identification is denoting the addressed account. The account-id is retrieved by using a "Read Account List" call. The account-id is the “id” attribute of the account structure. Its value is constant at least throughout the lifecycle of a given consent.


##### Query Parameters

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| dateFrom | ISODate | Conditional | Starting referenceDate of the transaction list, mandated if no delta access is required. |
| dateTo | ISODate | Optional | End referenceDate of the transaction list, default is now if not given. |
| transactionId | String | Optional | This data attribute is indicating that the AISP is in favour to get all transactions after the transaction with identification transactionId alternatively to the above defined period. This is a implementation of a delta access. <br><br> If this data element is contained, the entries "dateFrom" and "dateTo" might be ignored by the ASPSP if a delta report is supported. |
| psuInvolved | Boolean | Conditional | It must be contained if the PSU has asked for this account in realtime. This flag is then set to "true". The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient. |
| bookingStatus | String | Mandatory | Permitted codes are "booked", "pending" and "both" <br><br> "booked" and "both" are to be supported mandatorily by the ASPSP <br><br> To support the "pending" feature is optional for the ASPSP, Error code if not supported in the online banking frontend. |
| withBalance | Boolean | [Optional] | If contained the TPP is requiring to add the balance to the transaction list. This call will be rejected if the withBalance parameter is used in a case where the access right on balances is not granted in the corresponding consens or if the ASPSP does not support the withBalance parameter. |
| deltaList | Boolean | [Optional] | This data attribute is indicating that the AISP is in favour to get all transactions after the last report access for this PSU on the addressed account. This is another implementation of a delta access-report. This delta indicator might be rejected by the ASPSP if this function is not supported. |

##### Request Header

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| TPP-Transaction-ID	| UUID	|Mandatory|	ID of the transaction as determined by the initiating party. In case of a once off read data request, this Process-ID equals the Process-ID of the corresponding Account Information Consent Request.|
| x-request-id	| UUID|  	Mandatory	|  |
| Consent-ID | String | Mandatory |  |
| Authorization Bearer |	String |	Conditional |	Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in the related consent authorization. |
| Accept | String | Conditional | The TPP can indicate the formats of account reports supported together with a priorisation following the http header definition. The supported formats are XML, JSON and text. |   
| Signatue | String | Conditional | A signature of the request by the TPP on application level. This might be mandated by ASPSP. |
| Tpp-Certificate | String | Conditional | The certificate used for signing the request in base64 encoding. It shall be contained if a signature is used, see above. |


##### Response Header

Content-Type: application/json or application/xml or application/


##### Response Body

In case the ASPSP returns a camt.05x XML structure, the response body consists of either a camt.052 or camt.053 format. The camt.052 may include pending payments which are not yet finally booked. The ASPSP will decide on the format due to the chosen parameters, specifically on the chosen dates relative to the time of the request.

In case the ASPSP returns a MT94x content, the response body consists of an MT940 or MT942 format in a text structure. The camt.052 may include pending payments which are not yet finally booked. The ASPSP will decide on the format due to the chosen parameters, specifically on the chosen dates relative to the time of the request.

A JSON response is defined as follows:

| Attribute | Type  | Condition | Description |
|--------|------------------|----------|---------|
| _links | links | Optional | A list of hyperlinks to be recognized by the TPP. <br> Admitted types of links are "download" (link to a resource, where the transaction report might be downloaded from). <br><br> Remark: This feature shall be only used where camt-data is requested which has a huge size. <br><br> Pagination links where transaction reports have a huge size and the ASPSP is only providing one page, where page size is defined by the ASPSP. <br><br> "first", "next", "previous", "last" |
| transactions | Account Report | Optional | JSON based account report. |


##### Example 

*Request*

    GET https://api.testbank.com/v1/accounts/qwer3456tzui7890/transactions?date_from="2017-07-01"&date_to= “2017-07-30”&psu-involved
    Accept: application/json, application/text;q=0.9, application/xml;q=0.8 
 
*Response in JSON format for an access on a regular account*

Response Code 200

    {“transactions” :
       {“booked” :
          [{
             "transactioId" : "1234567" ,
             "creditorName" : "John Miles" ,
             "creditorAccount" : {"iban" : "DE43533700240123456900"},
             "balanceAmount” : {"currency" : "EUR", "content" : "-256,67"} ,
             "bookingDate" : "2017-10-25" ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 1"
          },
          {
             "transactionId" : "1234568",
             "debtorName" : "Paul Simpson" ,
             "debtorAccount" : {"iban" : "NL354543123456900"} ,
             "balanceAmount" : {"currency" : "EUR", content: "343,01"} ,
             "bookingDate" : "2017-10-25" ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 2"
          }],
       },
       {"pending" :
          [{
             "transactionId" : "1234569" ,
             "creditorName" : "Claude Renault" ,
             "creditorAccount : {"iban" : "FR33554543123456900"},
             "balanceAmount” : {"currency" : "EUR", "content" : "-100,03"} ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 3"
          }]
       },
       {"_links":
          {"viewAccount" : "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f"}
       }
    }
    
*Response in case of a huge data balanceAmount as download*

    {
       "_links : {"download" : www.testapi.com/xs2a/v1/accounts/12345678999/transactions/download/}
    }
    
*Response in JSON format for an access on a multicurrency account on aggregation level*

    {“transactions” :
       {“booked” :
          [{
             "transactioId" : "1234567" ,
             "creditorName" : "John Miles" ,
             "creditorAccount" : {"iban" : "DE43533700240123456900"},
             "balanceAmount” : {"currency" : "EUR", "content" : "-256,67"} ,
             "bookingDate" : "2017-10-25" ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 1"
          },
          {
             "transactionId" : "1234568",
             "debtorName" : "Paul Simpson" ,
             "debtorAccount" : {"iban" : "NL354543123456900"} ,
             "balanceAmount" : {"currency" : "EUR", content: "343,01"} ,
             "bookingDate" : "2017-10-25" ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 2"
          },
          {
             "transactionId" : "1234569",
             "debtorName" : "Pepe Martin" ,
             "debtorAccount" : {"iban" : "SE1234567891234"} ,
             "balanceAmount" : {"currency" : "USD", content: "100"} ,
             "bookingDate" : "2017-10-25" ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 3"
          }],
       },
       {"pending" :
          [{
             "transactionId" : "1234570" ,
             "creditorName" : "Claude Renault" ,
             "creditorAccount : {"iban" : "FR33554543123456900"},
             "balanceAmount” : {"currency" : "EUR", "content" : "-100,03"} ,
             "valueDate" : "2017-10-26" ,
             "remittanceInformationUnstructured" : "Example 4"
          }]
       },
       {"_links":
          {"viewAccount" : "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f"}
       }
    }
