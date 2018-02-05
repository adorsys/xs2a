# XS2A interface –> Core services-> Confirmation of Funds Service

## FCS_01 Confirmation of Funds Service (FCS).
This service may be used by a PIISP to request a confirmation
of the availability of specific funds on the account of a PSU. The
account is managed by the ASPSP providing the XS2A
Interface.

## Confirmation of Funds Service Flows
The TPP can use transactions according to this use case to receive confirmation about the
availability of the requested funds on a specific account. As a result the TPP will only receive
the answer **YES** or **NO**. No further information about the account will be returned.

While the transaction at the XS2A interface is initiated by the TPP, it must first be initiated by
the PSU by means of a card based payment transaction at a PSU – TPP interface, for example
at a checkout point.

PSU has to inform the ASPSP about its consent to a
specific request of the TPP prior to the transaction.

The ASPSP will reject the transaction if the TPP cannot be identified correctly at the XS2A
interface and/or if it does not have the role PIISP.

The ASPSP will also reject the transaction if the PSU has not previously informed the ASPSP
about its consent to the corresponding transaction of the TPP.

The following figure shows only the very top level information flow:


![Confirmation of Funds Service Flows](img/FCS_Flow.png)





### Data Overview Confirmation of Funds Service

The following table defines the technical description of the abstract data model for the three PSD2 services. The columns give an overview on the API protocols
as follows:
* The **"Data element"** column is using the abstract data elements  to deliver the connection to rules and role definitions.
* The **"Attribute encoding"** is giving the actual encoding definition within the XS2A
API.
* The **"Location"** columns (Path, Header, Body) define, where the corresponding data elements are
transported as https parameters, resp. are taken from e-Idas certificates.
* The **"Usage"**  column (Conf. Req., Conf Resp.) gives an overview on the usage of data elements in the
different services and API Calls. The XS2A calls are described
as abstract API calls. These calls will be technically realised as HTTPS POST
command. The calls are divided into the following calls:
    * Confirmation Request, which is the only API Call for every transaction
within the Confirmation of Funds service.

The following usage of abbreviations in the Location and Usage columns is defined:
* x: This data element is transported on the corresponding level.
* m: Mandatory
* o : Optional for the TPP to use
* c: Conditional. The Condition is described in the API Calls, condition defined by
  the ASPSP

| Data element               | Attribute encoding | Path | Header | Body | Certificate | Conf.  Req. | Conf.  Resp. |
|----------------------------|--------------------|:----:|:------:|:----:|:-----------:|:-----------:|:------------:|
|  Provider Identification   |                    |   x  |        |      |             |      m      |              |
| TPP Registration Number    |                    |      |        |      |      x      |      m      |              |
| TPP Name                   |                    |      |        |      |      x      |      m      |              |
| TPP Roles                  |                    |      |        |      |      x      |      m      |              |
| Transaction Identification | transaction_id     |      |        |   x  |             |             |              |
| Request Timestamp          | DateTime           |      |    x   |      |             |      m      |              |
| TPP Certificate Data       | certificate        |      |        |   x  |             |      c      |              |
| TPP Electronic Signature   | signature          |      |        |   x  |             |      c      |              |
| Service Type               |                    |   x  |        |      |             |      m      |              |
| Response Code              |                    |      |    x   |      |             |             |       m      |
| TPP Message Information    | tpp_message        |      |        |   x  |             |             |       o      |
| Card Number                | card_number        |      |        |   x  |             |      c      |              |
| Account Number             | psu_account        |      |        |   x  |             |      c      |              |
| Name Payee                 | payee              |      |        |   x  |             |      o      |              |
| Transaction Amount         | amount             |      |        |   x  |             |      m      |              |

### Confirmation of Funds Request
#### FCS_01_01 Confirmation of Funds Request

##### Call
    POST /v1/confirmation-of-funds
Creates a confirmation of funds request at the ASPSP.

##### Path
    No specific path parameters.


##### Request Header

| Attribute            | Type     | Condition   | Description                                                                                |
|----------------------|----------|-------------|--------------------------------------------------------------------------------------------|
| Process-ID           | UUID     | Mandatory   | ID of the transaction as determined by the initiating party.                               |
| Request-ID           | UUID     | Mandatory   | ID of the request, unique to the call, asdetermined by theinitiating party.                |
| Authorization Bearer | String   | Conditional | Is contained only, if the optional Oauth Pre-Step was performed.                           |
| Signature            | String   | Conditional | A signature of the request by the TPP onapplication level. This might be mandated byASPSP. |
| Certificate          | String   | Conditional | The certificate used for signing the request.                                              |
| Date                 | DateTime | Mandatory   | Standard https header element date and time.                                               |


##### Request Body

| Attribute         | Type              | Condition   | Description                                                                  |
|-------------------|-------------------|-------------|------------------------------------------------------------------------------|
| card_number       | String            | Conditional | Card Number of the card issued by the PIISP. Must be delivered if available. |
| psu_account       | account reference | Mandatory   | PSU’s account number.                                                        |
| payee             | String            | Optional    | The merchant where the card is acceptedas an information to the PSU.         |
| instructed_amount | Amount            | Mandatory   | Transaction amount to be checked withinthe funds check mechanism.            |

