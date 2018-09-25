Feature: Payment Initiation Embedded approach

    Scenario Outline: Successful payment initiation request (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request
        Then a successful response code and the appropriate authentication URL is delivered to the PSU
        Examples:
            | payment-service | payment-product       | single-payment                |
            | payments        | sepa-credit-transfers | singlePayInit-successful.json |

    Scenario Outline: Successful Start of Authorisation (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU wants to start the authorisation using the <authorisation-data>
        When PSU sends the start authorisation request
        Then PSU checks if a link is received and the SCA status is correct
        Examples:
            | payment-service                          | authorisation-data               | payment-product         | single-payment                |
            | payments	                               | authWithPsuIdent-successful.json | sepa-credit-transfer    | singlePayInit-successful.json |

    Scenario Outline: Successful update of identification data (no Sca method) (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        When PSU sends the update Authorisation request for no sca method
        Then PSU checks if the correct SCA status and response code is received
        Examples:
            | payment-service                          | payment-product         | single-payment                | identification-data                          |
            | payments	                               | sepa-credit-transfer    | singlePayInit-successful.json | updateIdentificationNoSca-successful.json    |

    Scenario Outline: Successful update of identification data (one Sca method) (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        When PSU sends the update Authorisation request for one sca method
        Then PSU checks if the correct SCA status and response code is received
        Examples:
            | payment-service                          | payment-product         | single-payment                | identification-data                          |
            | payments	                               | sepa-credit-transfer    | singlePayInit-successful.json | updateIdentificationOneSca-successful.json   |

    Scenario Outline: Successful update of identification data (multiple Sca methods) (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        When PSU sends the update Authorisation request for multiple sca methods
        Then PSU checks if the correct SCA status, sca methods and response code is received
        Examples:
            | payment-service                          | payment-product         | single-payment                | identification-data                              |
            | payments	                               | sepa-credit-transfer    | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  |

    Scenario Outline: Successful Selection of PSU authentication method (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        And PSU sends the update Authorisation request for multiple sca methods
        And PSU wants to select the authentication method using the <selection-data>
        When PSU sends the select sca method request
        Then PSU checks if the correct SCA status and response code is received
        Examples:
            | payment-service                          | payment-product         | single-payment                | identification-data                              | selection-data                                |
            | payments	                               | sepa-credit-transfer    | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuthenticationMethod-successful.json    |

#   TODO: find a way to test the tan confirmation

#        And PSU needs to authorize and identify using <authorisation-data> and <authorisation-id>
#        And check SCA methods
#        When PSU sends the authorisation request with the payment-id <payment-id> and authorisationId <authorisation-id>
#        And check SCA status
#        And sends the authorisation request with the payment-id <payment-id> and authorisationId <authorisation-id> and the TAN <tan>
#        Then a successful response code and the appropriate authorization data are received
#        Examples:
#            | payment-service                          | authorisation-data               | payment-product         | single-payment
#            | payments	                               | authWithPsuIdent-successful.json | sepa-credit-transfer    | singlePayInit-successful.json
