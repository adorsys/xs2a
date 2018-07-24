# XS2A interface –> Core services-> Payment Initiation Service

## PIS_01 Payment Initiation Service (PIS).
This service may be used by a PISP to initiate a single payment on behalf of a PSU using a given account of that PSU. The account is managed by the ASPSP providing the XS2A Interface.


    With Redirect SCA Approach
    If the ASPSP supports the Redirect SCA Approach, the message flow within the payment
	   initiation service is simple. The Payment Initiation Request is followed by a redirection to the
	   ASPSP SCA authorization site. A status request might be requested by the TPP after the
	   session is re-redirected to the TPP’s system.

![Payment Initiation Service Redirect Approach](img/PIS_Redirect.png)


    With OAuth2 SCA Approach
    If the ASPSP supports the OAuth2 SCA Approach, the flow is very similar to the Redirect SCA Approach. 
    Instead of redirecting the PSU directly on an authentication server, the OAuth2 protocol is used for the transaction authorization process.

![Payment Initiation Service OAuth2 Approach](img/PIS_OAuth.png)


### Data Overview Payment Initiation Service

The following table defines the technical description of the abstract data model for the Payment Initiation service. The columns give an overview on the API protocols as follows:


* The **"Data element"** column is using the abstract data elements to deliver the connection to rules and role definitions. 

* The **"Attribute encoding"** is giving the actual encoding definition within the XS2A API.

* The **"Location"** columns define, where the corresponding data elements are transported as http parameters on path, header or body level, resp. are taken from e-IDas certificates. <br>
**Remark:** Please note that website authentication certificate related data elements are not elements of the actual API call. They are indicated here, since they are mandated in the backend processing and might be transported from the API endpoint internally to the backend on the application layer. Please note, that in difference to this, the certificate data for the electronic seal can be transported within a dedicated http header field.
* The **"Usage"** column gives an overview on the usage of data elements in the different services and API Calls. These calls will be technically realised as HHTPS POST, PUT and GET commands. There are:
    * Init Req.
    * Init Resp.
    * Upd. Req.
    * Upd. Resp.
    * Stat. Req.
    * Stat. Resp

The calls are divided into the following calls for Payment Initiation:
* The Initiation Request which shall be the first API Call for every
      transaction within the corresponding XS2A service Payment Initiation. This
      call generates the corresponding resource within the Payment Initiation
      Service. The Payment Initiation can address a single payment, bulk payments and recurring payments. The latter are implemented as an initiation of a standing order.
* The Update Data Call is a call, where the TPP needs to add PSU related
      data, which is requested in the return of the first call. This call might be
      repeated.
* The Status Request is used e.g. in cases, where the SCA control is taken
      over by the ASPSP and the TPP needs later information about the
      outcome.

The following usage of abbreviations in the Location and Usage columns is defined:
* x: This data element is transported on the corresponding level.
* m: Mandatory
* o : Optional for the TPP to use
* c: Conditional. The Condition is described in the API Calls, condition defined by
  the ASPSP

| Data element                        | Attribute encoding                   | Path | Query | Header | Body | Certificate | Init Req. | Init Resp. | Upd Req. | Upd Resp. | Stat Req. | Stat Resp. |
|-------------------------------------|--------------------------------------|:----:|:-----:|:------:|:----:|:-----------:|:---------:|:----------:|:--------:|:---------:|:---------:|:----------:|
| TPP RegistrationNumber              |                                      |      |       |        |      |      x      |     m     |            |     m    |           |     m     |            |
| TPP Name                            |                                      |      |       |        |      |      x      |     m     |            |     m    |           |     m     |            |
| TPP Roles                           |                                      |      |       |        |      |      x      |     m     |            |     m    |           |     m     |            |
| TPP NationalCompetent Authority     |                                      |      |       |        |      |      x      |     m     |            |     m    |           |     m     |            |
| Transaction Identification          | TPP-Transaction-ID (unique ID of TPP)|      |       |    x   |      |             |     m     |            |     m    |           |     m     |            |
| Request Identification              | x-request-id                       |      |       |    x   |      |             |     m     |            |     m    |           |     m     |            |
| Resource ID                         | paymentId                            |   x  |       |        |   x  |             |           |      m     |     m    |           |     m     |            |
| Access Token (fromoptional OAuth 2) | Authorization Bearer                 |      |       |    x   |      |             |     c     |            |     c    |           |     c     |            |
| TPP Signing                         | TPP-Certificate                      |      |       |    x   |      |             |     c     |            |     c    |           |     c     |            |
| Certificate                         |                                      |      |       |    x   |      |             |     c     |            |     c    |           |     c     |            |
| TPP Electronic Signature            | Signature                            |      |       |    x   |      |             |     c     |            |     c    |           |     c     |            |
| Service Type                        |                                      |   x  |       |        |      |             |     m     |            |     m    |           |     m     |            |
| Response Code                       |                                      |      |       |    x   |      |             |           |      m     |          |     m     |           |      m     |
| Transaction Status                  | transactionStatus                    |      |       |        |   x  |             |           |      m     |          |     m     |           |      m     |
| PSU Message Information             | psuMessage                           |      |       |        |   x  |             |           |      o     |          |     o     |           |      o     |
| TPP Message Information             | tppMessages                          |      |       |        |   x  |             |           |      o     |          |     o     |           |      o     |
| PSU Identification                  | PSU-ID                               |      |       |    x   |      |             |     c     |            |     c    |           |           |            |
| PSU Identification Type             | PSU-ID-Type                          |      |       |    x   |      |             |     c     |            |     c    |           |           |            |
| Corporate Identification            | PSU-Corporate-ID                     |      |       |    x   |      |             |     c     |            |     c    |           |     c     |            |
| Corporate ID Type                   | PSU-Corporate-ID-Type                |      |       |    x   |      |             |     c     |            |     c    |           |     c     |            |
| IP Address PSU                      | PSU-IP-Address                       |      |       |    x   |      |             |     m     |            |          |           |           |            |
| PSU User Agent                      | PSU-User-Agent*                      |      |       |    x   |      |             |     o     |            |          |           |           |            |
| GEO Information                     | PSU-Geo-Location                     |      |       |    x   |      |             |     o     |            |          |           |           |            |
| Redirect URL ASPSP                  | _links.redirect                      |      |       |        |   x  |             |           |      c     |          |           |           |            |
| Redirect Preference                 | tppRedirectPreferred                 |      |   x   |        |      |             |     o     |            |          |           |           |            |
| Redirect URI TPP*                   | TPP-Redirect-URI                     |      |       |    x   |      |             |     c     |            |          |           |           |            |
| Payment Product                     | payment-product                      |   x  |       |        |      |             |     m     |            |     m    |           |     m     |            |


PSU-User-Agent* - This field transports key information for risk management like browser type or PSU device operating system. The forwarding of further http header fields might be supported in future versions of the specification to transport other device related information.

Redirect URI TPP* - This redirect link must be contained, if the tppRedirectPreferred flag is contained and equals "true" or if the tppRedirectPreferred flag is not used.

**Remark:** The request timestamp of every call is contained in the mandatory http header “Date”. This timestamp is not contained in the data tables below because it is a mandatory http header field anyhow and because incompatibilities could appear otherwise with future more formalised specification procedures.



## Payment Initiation with JSON encoding of the Payment Instruction
### PIS_01_01 Call

**Endpoint POST**
   * 	/v1/payments/{product-name}

**Product-name**
*	sepa-credit-transfers
*	instant-sepa-credit-transfers
*	target-2-payments
*	cross-border-credit-transfers



    POST /v1/payments/{payment-product}
Creates a payment initiation request at the ASPSP.

### Path

Attribute | Type  | Description |
------- | ---------------- |  :---------
Payment-product  | string | The addressed payment product endpoint, e.g. for SEPA Credit Transfers (SCT). The default list of products supported in this standard is:  sepa-credit-transfers, instant-sepa-credit-transfers, target-2-payments, cross-border-credit-transfers |


### Query Parameters

Attribute | Type  | Description |
------- | ---------------- |  :---------
| tppRedirectPreferred | Boolean | If it equals "true" the TPP prefers a redirect over an embedded SCA approach. <br><br> If it equals "false" the TPP prefers not to be redirected for SCA. The ASPSP will then choose between the Embedded or the Decoupled SCA approach, depending on the choice of the SCA procedure by the TPP/PSU. <br><br> If the parameter is not used the ASPSP will choose the SCA approach to be applied depending on the SCA method chosen by the TPP/PSU. |
| 


### Request Header

Attribute | Type  | Condition | Description |
------- | ---------------- |  :---------  |  :---------
| Content-Type	| string |	Mandatory |	application/json |
| TPP-Transaction-ID	| UUID	|Mandatory|	ID of the transaction as determined by the initiating party|
| x-request-id	| UUID|	Mandatory	| ID of the request, unique to the call, as determined by the initiating party.|
| PSU-ID|	String	|Conditional	| Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP’s documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session.|
| PSU-ID-Type | String | Conditional | Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. |
| PSU-Corporate-ID |	String |	Conditional |	Might be mandated in the ASPSP's documentation. Only used in a corporate context. |
| PSU-Corporate-ID-Type | String | Conditional | Might be mandated in the ASPSP's documentation. Only used in a corporate context. |
| Authorization Bearer |	String |	Conditional |	Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. | |
| PSU-Consent-ID |	String |	Optional |	This data element may be contained, if the payment initiation transaction is part of acombined AIS/PIS service. This then contains the consent id of the related AIS consent. |
| PSU-Agent |	String	| Optional |	The forwarded Agent header field of the http request between PSU and TPP. |
| PSU-IPAddress | String	| Mandatory |	The forwarded IP Address header field consists of the corresponding http request IP Address field between PSU and TPP. |
| PSU-Geo-Location	| Geo Location	| Optional |	The forwarded Geo Location header field of the corresponding http request between PSU and TPP if available. |
| TPP-Redirect-URI | String | Conditional | URI of the TPP where the transaction flow shall be redirected to after a Redirect. |
| Signature	| String |	Conditional |	A signature of the request by the TPP on application level. This might be mandated by ASPSP. |
| TPP-Certificate	|String |	Conditional	| The certificate used for signing the request in base64 encoding. Must be contained if a signature is contained, see above. |

###  Request Body
The payment data to be transported in the request body are dependent of the chosen API
endpoint. Some standard definitions related to the above mentioned standard products are
defined below. Further definitions might be done community or ASPSP specific. ASPSP or
community definitions should reuse standard attribute names.


For core payment products in the European market, this document is defining JSON
structures, which will be supported by all ASPSPs
* offering the corresponding payment products to their customers and
* providing JSON based payment endpoints

At the same time, the ASPSP may offer in addition more extensive JSON structures for the
same payment products since they might offer these extensions also in their online banking
system.

![Payment schemes](img/PIS_JSON_usage.png)


The following table first gives an overview on the generic Berlin Group defined JSON
structures of standard SEPA payment products for single payments.

Data Element | Type  | SCT EU Core | SCT_INST EU Core | Target2  Paym. Core | Cross  Curr CT Core |
------- | ---------------- |  ---------  |  --------- |  --------- |  ---------
| endToEndIdentification	| Max35Text |	Optional |	Optional | Optional | n.a. |
| debtorAccount (incl. type)	| Account Reference |	mandatory |	mandatory | mandatory | mandatory |
| ultimateDebtor	| Max70Text |	n.a. |	n.a. | n.a. | n.a. |
| instructedAmount (inc. Curr.)	| Amount |	mandatory |	mandatory | mandatory | mandatory |
| creditorAccount	| Account Reference  |	mandatory |	mandatory | mandatory | mandatory |
| creditorAgent	| BICFI |	Optional |	Optional | Optional | Optional |
| creditorName	| Max70Text |	mandatory |	mandatory | mandatory | mandatory |
| creditorAddress	| Address |	Optional |	Optional | Optional | mandatory |
| ultimateCreditor | Max70Text |	 n.a. |	 n.a. |  n.a. | n.a. |
| purposeCode	| Purpose Code |	 n.a. |	 n.a. |  n.a. | n.a. |
| remittanceInformationUnstructured	| Max140Text |	Optional |	Optional | Optional | Optional |
| remittanceInformationStructured	| Remittance |	n.a. |	n.a. | n.a. | n.a. |
| requestedExecutionDate	| ISODate |	n.a. |	n.a. | n.a. | n.a. |
| requestedExecutionTime	| ISODateTime |	n.a. |	n.a. | n.a. | n.a. |

**Remark:** Extensions of these tables are permitted by this specification:
* if they are less restrictive (e.g. set the debtor account to optional) or
* if they open up for more data elements (e.g. open up the structured remittance information, or ultimate data fields.


          Attention: The ASPSP may reject a payment initiation request where additional data elements are used which are not specified.



### Response Header
The Location field is used as link to the created resource. No other specific requirements.

###  Response Body

Attribute | Type  | Condition | Description |
------- | ---------------- |  ---------  |  ---------
| transactionStatus | Transaction status  |	Mandatory |	The values defined in the table “Transaction Status” (see "DICT_01_01 Transaction status" in [Dictionary: Business_objects](DICT_01.Business_objects.md) ) might be used. |
| paymentId | String | Mandatory | Resource identification of the generated payment initiation resource. |
| transactionFees | Amount | Optional | Can be used by the ASPSP to transport transaction fees relevant for the underlying payments. |
| transactionFeeIndicator | Boolean | Optional | If equals "true" the transaction will involve specific transaction cost as shown by the ASPSP in their public price list or as agreed between ASPSP and PSU. <br><br> If equals "false" the transaction will not involve additional specific transaction costs to the PSU. |
| scaMethods | Array of authentication objects |	Conditional |	This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods. Depending on the risk management of the ASPSP this choice might be offered before or after the PSU has been identified with the first relevant factor, or if an access token is transported. If this data element is contained, then there is also an hyperlink of type "select_authentication_methods" contained in the response body. These methods shall be presented towards the PSU for selection by the TPP. |
| _links | Links  |	Mandatory |	A list of hyperlinks to be recognized by the TPP. Type of links admitted in this response, (further links might be added for ASPSP defined extensions): <br><br> **"redirect":** In case of an SCA Redirect Approach, the ASPSP is transmitting the link  to which to redirect the PSU browser. <br><br> **"oAuth":** In case of an OAuth2 based Redirect Approach the ASPSP is transmitting the link where the configuration of the OAuth2 Server is defined. <br><br> **"updatePsuIdentification":** The link to the payment initiation resource, which needs to be updated by the psu identification. This  might be used in a redirect or decoupled approach, where the PSU ID was missing in the first request. <br><br> **"updatePsuAuthentication":** The link to the payment initiation resource, which need to be updated by a psu password and  eventually the psu identification if not delivered yet. This is used in a case of the Embedded SCA approach. <br><br> **"selectAuthenticationMethod":** This is a link to a resource, where the TPP can select  the applicable strong customer authentication methods for the PSU, if there  were several available authentication methods. This link contained under exactly the same conditions as the data element “authentication_methods”, see above. <br><br> **“status”:** The link to retrieve the transaction status of the account information consent. |
| psuMessage | String  |	Optional | Text to be displayed to the PSU |
| tppMessages | Array of Message |   Optional	 | Messages to the TPP on operational issues. |



### Example

*Request*

    POST https://api.testbank.com/v1/payments/sepa-credit-transfers
    Content-Encoding gzip
    Content-Type application/json
    TPP-Transaction-ID 3dc3d5b3-7023-4848-9853-f5400a64e80f
    x-request-id 99391c7e-ad88-49ec-a2ad-99ddcb1f7721
    PSU-IP-Address 192.168.8.78
    PSU-GEO-Location:	GEO:52.506931,13.144558
    PSU-Agent Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0
    Date Sun, 06 Aug 2017 15:02:37 GMT
    {
    "instructedAmount” : {“currency” : “EUR” , “content” : "123.50"},
    "debtorAccount" : { "iban":"DE2310010010123456789"},
    “creditor” : { “name” : “Merchant123”} ,
    "creditorAccount”: {“iban”:“DE23100120020123456789”},
    “remittanceInformationUnstructured” : “Ref Number Merchant”
    }

*Response in case of a redirect*

    Response Code 201


*Response Body*

    {
     "transactionStatus" : "Received",
     "paymentId": "1234-wertiq-983",
     “_links" {
        "redirect" : "www.testbank.com/asdfasdfasdf",
        "self" : "/v1/payments/sepa-credit-transfers/1234-wertiq-983"
     }
    }

*Response in case of an OAuth2 response

     Reponse Code 201
     
*Response Body*

    {
     "transactionStatus" : "Received",
     "paymentId": "1234-wertiq-983",
     “_links" {
        "updatePsuIdentification" : "“/v1/payments/sepa-credit-transfers/1234-wertiq-983",
        "self" : "/v1/payments/sepa-credit-transfers/1234-wertiq-983"
     }
    }


## Payment Initiation for Bulk Payments and Multiple Payments 

The Online Banking frontends might support the
* upload of bulks or
* multiple payment functions where a PSU can enter multiple payment data before authorize all the payments with one SCA method.

Both functions are modelled as bulk payment in the XS2A interface. The multiple payment function can be offered by the TPP towards the PSU in its own GUI. In the XS2A interface this function is always supported as bulk payment. This function is an optional function of the ASPSP in the XS2A interface. It can be offered by the ASPSP in JSON or XML modelling of the payment data, i.e. the body content.



## PIS_01_02 Payment Initiation for Bulk Payments and Multiple Payments

### Call

    POST /v1/bulk-payments/{payment-product}
     
Creates a bulk payment initiation request at the ASPSP.

### Path 

| Attribute | Type  | Description |
|------- | ---------------- |  :---------|
| payment-product | String | The addressed payment product endpoint for buk payments e.g. for a bulk SEPA Credit Transfers (SCT). These endpoints are optional. The default list of products supported in this standard is:  sepa-credit-transfers, instant-sepa-credit-transfers, target-2-payments, cross-border-credit-transfers  <br><br> The ASPSP will publish which of the payment products/endpoints will be supported. Further prooducts might be published by the ASPSP wizhin its XS2A documentation. These new product types will end in further endpoints of the XS2A Interface. |

The same query parameter and http header definition as in section PIS_01_01 applies.


## PIS_01_03 Initiation for Standing Orders for Recurring/Periodic Payments

The recurring payments initiation function will be covered in this specification as a specific standing order initiation: The TPP can submit a recurring payment initiation where the starting referenceDate, frequency and conditionally an end referenceDate is provided. Once authorized by the PSU, the payment then will be executed by the ASPSP, if possible, following this “standing order” as submitted by the TPP. No further TPP action is needed. This payment is called a periodic payment in this context to differentiate the payment from recurring payment types, where third parties are initiating the same balanceAmount of money e.g. payees for using credit card transactions or direct debits for reccuring payments of goods or services. These latter types of payment initiations are not part of this interface.


### Call

    POST /v1/periodic-payments/{payment-product}

    
### Path Parameters

The same path parameter to determine the underlying payment type of the recurring payment as in Section PIS_01_01 applies.


### Request Header

For this initiation the same header as in Section PIS_01_01 is used.


### Request Body

| Tag | Type  | Usage | Description |
|-----|-------|-------|-------------|
| startDate | ISODate | Mandatory | The first applicable day of execution starting from this referenceDate is the first payment. |
| executionRule | String | Optional | "following" or "preceeding" supported as values. This data attribute defines the behavior when recurring payment dates falls on a weekend or bank holiday. The payment is then executed either the "preceeding" or the following" working day.  <br><br> ASPSP might reject the request due to the communicated value, if rules in Online-Banking are not supporting this execution rule. |
| endDate | ISODate | Optional | The last applicable day of execution. If not given, it is an infinite standing order. |
| frequency | Frequency Code | Mandatory | The frequency of the recurring payment resulting from this standing order. |
| dayOfExecution | DD | Conditional | "31" is ultimo |


### Response 

The formats of the Payment Initiation Response resp. the subsequent transaction authorization process for standing orders with JSON based payment data equals the corresponding Payment Initiation Response resp. the subsequent transaction authorization process for a single payment containing JSON based payment data.

**Remark:** Please note that for the payment initiation of standing orders, the ASPSP will always mandate an SCA with dynamic linking, exemptions are not permitted.


### Example for Variant with full JSON encoding

    POST https:/v1/periodic-payments/sepa-credit-transfers 
    Content-Encoding:    gzip
    Content-Type:        application/json
    TPP-Transaction-ID:  3dc3d5b3-7023-4848-9853-f5400a64e80f 
    x-request-id:      99391c7e-ad88-49ec-a2ad-99ddcb1f7721 
    PSU-IP-Address:      192.168.8.78
    PSU-Agent:           Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0
    Date:                Sun, 06 Aug 2017 15:02:37 GMT
    {
     “instructedAmount”: {“currency” : “EUR” , “content” : “123”}, 
     “debtorAccount”: {“iban” : “DE2310010010123456789”}, 
     “creditorName”: “Merchant123”,
     “creditorAccount”: {“iban” : “DE23100120020123456789”}, 
     “remittanceInformationUnstructured”: “Ref Number Abonnement”, 
     “startDate”: “2018-03-01”,
     “executionRule”: “latest”, 
     “frequency”: “monthly”, 
     “dayOfExecution” : “01”
    }




## PIS_01_04 Get Status Request

### Call
    GET /v1/payments/{payment-product}/{paymentId}/status

Can check the status of a payment initiation.

### Path

| Attribute | Type | Description |
|-----------|------|-------------|
| payment-product |  String  | Payment product of the related payment. |
| paymentId |  String  | Resource Identification of the related payment. |



### Request Header

Attribute | Type | Condition | Description |
------- | ---------------- | ---------------- | ----------------
| TPP-Transaction-ID |  UUID  | Mandatory | |
| x-request-id |  UUID  | Mandatory | |
| Authorization Bearer |  String  | Conditional | Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in the current PIS transaction or in a preceeding AIS service in the same session, if no such OAuth2 SCA approach was chosen in the current PIS transaction.  |
| Signature |  String | Conditional | A signature of the request by the TPP on  application level. This might be mandated by ASPSP. |
| TPP-Certificate |  String | Conditional | The certificate used for signing the request in base64 encoding. Must be contained if a signature is contained, see above. |

### Query Parameters
No specific query parameters defined.

### Request Body
No body.


###  Response Body
in Case of JSON based endpoint

Attribute | Type | Condition | Description |
------- | ---------------- | ---------------- | ----------------
| transactionStatus |  Transaction Status  | Mandatory | In case where the Payment Initiation Request was JSON encoded, the status is returned in this JSON based encoding |


### Example for JSON based endpoint
*Request*

    GET https://api.testbank.com/v1/payments/sepa-credittransfers/qwer3456tzui7890/status
    Accept application/json
    TPP-Transaction-ID 3dc3d5b3-7023-4848-9853-f5400a64e80f
    x-request-id 99391c7e-ad88-49ec-a2ad-99ddcb1f7721
    Date Sun, 06 Aug 2017 15:04:07 GMT
    
Response Code 200
    
    Content-Type application/json
    {
     “transactionStatus” : "AcceptedCustomerProfile"
    }
