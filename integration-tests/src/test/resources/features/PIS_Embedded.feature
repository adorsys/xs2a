Feature: Payment Initiation Service Embedded Approach


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Initiation                                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful payment initiation request for single payments (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request
        Then a successful response code and the appropriate authentication URL is delivered to the PSU
        Examples:
            | payment-service | payment-product       | single-payment                |
            | payments        | sepa-credit-transfers | singlePayInit-successful.json |


    Scenario Outline: Failed payment initiation request for single payments (embedded)
        Given PSU initiates an errorful single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service     | payment-product       | single-payment                                 |
            | payments            | sepa-credit-trans     | singlePayInit-incorrect-payment-product.json   |
            | payments            | sepa-credit-transfers | singlePayInit-no-request-id.json               |
            | payments            | sepa-credit-transfers | singlePayInit-no-ip-address.json               |
            | payments            | sepa-credit-transfers | singlePayInit-wrong-format-request-id.json     |
#            | payments            | sepa-credit-transfers | singlePayInit-wrong-format-psu-ip-address.json |
#            | recurring-payments  | sepa-credit-transfers | singlePayInit-wrong-payment-service.json       |

#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Start of Authorisation                                                                                           #
#    #                                                                                                                  #
#    ####################################################################################################################
    Scenario Outline: Successful start of authorisation (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU wants to start the authorisation using the <authorisation-data>
        When PSU sends the start authorisation request
        Then PSU checks if a link is received and the SCA status is correct
        Examples:
            | payment-service                          | authorisation-data               | payment-product          | single-payment                |
            | payments	                               | startAuth-successful.json        | sepa-credit-transfers    | singlePayInit-successful.json |


    Scenario Outline: Failed start of authorisation (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <paymentInitiation-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU prepares the errorful authorisation data <authorisation-data> with the payment service <startAuth-payment-service>
        When PSU sends the errorful start authorisation request
        Then an error response code and the appropriate error response are received
        Examples:
            | paymentInitiation-service | startAuth-payment-service | authorisation-data                     | payment-product          | single-payment                |
#            | payments	                | payments                  | startAuth-not-existing-paymentId.json  | sepa-credit-transfers    | singlePayInit-successful.json |
            | payments	                | payments                  | startAuth-no-request-id.json           | sepa-credit-transfers    | singlePayInit-successful.json |
            | payments	                | payments                  | startAuth-wrong-format-request-id.json | sepa-credit-transfers    | singlePayInit-successful.json |
#            | payments                  | recurring-payments        | startAuth-wrong-payment-service.json   | sepa-credit-transfers    | singlePayInit-successful.json |


    ####################################################################################################################
    #                                                                                                                  #
    # Update of the identification data of PSU                                                                         #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful update of identification data (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        When PSU sends the update identification data request
        Then PSU checks if the correct SCA status and response code is received
        Examples:
            | payment-service                          | payment-product         | single-payment                | identification-data                              |
            | payments	                               | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationNoSca-successful.json        |
#   REMARK: Update Identification with one sca only works when email server is running
#            | payments	                               | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationOneSca-successful.json       |
#            | payments	                               | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  |


    Scenario Outline: Errorful update of identification data (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <paymentInitiation-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU prepares the errorful identification data <identification-data> with the payment service <updateAuth-payment-service>
        When PSU sends the errorful update authorisation data request
        Then an error response code and the appropriate error response are received
        Examples:
            | paymentInitiation-service                | updateAuth-payment-service    |  identification-data                               | payment-product         | single-payment                |
            | payments	                               | payments                      |  updateIdentification-no-request-id.json           | sepa-credit-transfers   | singlePayInit-successful.json |
#            | payments	                               | payments                      |  updateIdentification-wrong-authorisation-id.json  | sepa-credit-transfers   | singlePayInit-successful.json |
            | payments	                               | payments                      |  updateIdentification-wrong-format-request-id.json | sepa-credit-transfers   | singlePayInit-successful.json |
#            | payments	                               | recurring-payments            |  updateIdentification-wrong-payment-service.json   | sepa-credit-transfers   | singlePayInit-successful.json |
#            | payments	                               | recurring-payments            |  updateIdentification-not-existing-paymentId.json   | sepa-credit-transfers   | singlePayInit-successful.json |

#    ####################################################################################################################
    #                                                                                                                  #
    # Selection of the sca method of PSU                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
    @ignore
    Scenario Outline: Successful selection of PSU sca method (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        And PSU sends the update identification data request
        And PSU wants to select the authentication method using the <selection-data>
        When PSU sends the select sca method request
        Then PSU checks if the correct SCA status and response code is received for the selection
        Examples:
            | payment-service                          | payment-product         | single-payment                | identification-data                              | selection-data                                |
            | payments	                               | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuthenticationMethod-successful.json    |
#   REMARK: Selection of sca method only works when email server is running

        Scenario Outline: Errorful selection of PSU sca method (embedded)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        And PSU sends the single payment initiating request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his <identification-data>
        And PSU sends the update identification data request
        And PSU prepares the errorful selection data <selection-data> with the payment service <selectionSCAPayment-service>
        When PSU sends the errorful update authorisation data request
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service                          | selectionSCAPayment-service    | payment-product         | single-payment                | identification-data                              | selection-data                           |
#            | payments	                               | payments                       | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuth-not-existing-paymentId.json   |
            | payments	                               | payments                       | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuth-no-request-id.json            |
            | payments	                               | payments                       | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuth-wrong-format-request-id.json  |
#            | payments	                               | payments                       | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuth-wrong-sca-method.json         |
#            | payments	                               | recurring-payments             | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuth-wrong-payment-service.json    |
#            | payments	                               | payments                       | sepa-credit-transfers   | singlePayInit-successful.json | updateIdentificationMultipleSca-successful.json  | selectAuth-wrong-authorisation-id.json   |


    ####################################################################################################################
    #                                                                                                                  #
    # Provision of TAN                                                                                                 #
    #                                                                                                                  #
    ####################################################################################################################
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
