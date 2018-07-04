#AIS Specific Error Codes Use Cases

| Message Code               | http response code | Description                                                                                                                                                                                        |
|----------------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CONSENT_INVALID            | 401                | The consent definition is not complete or invalid. In case of being not complete, the bank is not supporting a completion of the consent towards the PSU. Additional information will be provided. |
| SESSIONS_NOT_SUPPORTED     | 400                | The combined service flag may not be used with this ASPSP.                                                                                                                                         |
| ACCESS_EXCEEDED            | 429                | The access on the account has been exceeding the consented multiplicity per day.                                                                                                                   |
| REQUESTED_FORMATS _INVALID | 406                | The requested formats in the Accept header entry are not matching the formats offered by the ASPSP.                                                                                                |

