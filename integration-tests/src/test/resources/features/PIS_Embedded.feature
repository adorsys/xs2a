Feature: Payment Initiation Service - Embedded approach


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


#    ####################################################################################################################
#    #                                                                                                                  #
#    # Start of Authorisation                                                                                           #
#    #                                                                                                                  #
#    ####################################################################################################################
    Scenario Outline: Successful start of authorisation (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU wants to start the authorisation using the authorisation data <authorisation-data>
        When PSU sends the start authorisation request
        Then PSU checks if a link is received and the SCA status is correct
        Examples:
            | authorisation-data               |
            | startAuth-successful.json        |


    Scenario Outline: Failed start of authorisation (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU prepares the errorful data <authorisation-data> with the payment service <payment-service>
        When PSU sends the errorful start authorisation request
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service | authorisation-data                     |
#            | payments                  | startAuth-not-existing-paymentId.json  |
            | payments                  | startAuth-no-request-id.json           |
#           | payments                  | startAuth-wrong-format-request-id.json |
#            | recurring-payments        | startAuth-wrong-payment-service.json   |


    ####################################################################################################################
    #                                                                                                                  #
    # Update of the identification data of PSU                                                                         #
    #                                                                                                                  #
    ####################################################################################################################
    #   REMARK: Update Identification with one sca only works when email server is running
    Scenario Outline: Successful update of identification data (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU wants to update the resource with his identification data <identification-data>
        When PSU sends the update identification data request
        Then PSU checks if the correct SCA status and response code is received
        Examples:
            | identification-data                              |
            | updateIdentificationNoSca-successful.json        |
#            | updateIdentificationOneSca-successful.json       |
            | updateIdentificationMultipleSca-successful.json  |


    Scenario Outline: Errorful update of identification data (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU prepares the errorful data <identification-data> with the payment service <payment-service>
        When PSU sends the errorful update authorisation data request
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service               |  identification-data                               |
            | payments                      |  updateIdentification-no-request-id.json           |
#            | payments                      |  updateIdentification-wrong-authorisation-id.json  |
#            | payments                      |  updateIdentification-wrong-format-request-id.json |
#            | recurring-payments            |  updateIdentification-wrong-payment-service.json   |
#            | recurring-payments            |  updateIdentification-not-existing-paymentId.json   |


#    ####################################################################################################################
    #                                                                                                                  #
    # Selection of the sca method of PSU                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
    #   REMARK: Selection of sca method only works when email server is running
    @ignore
    Scenario Outline: Successful selection of PSU sca method (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU updates his identification data
        And PSU wants to select the authentication method using the <selection-data>
        When PSU sends the select sca method request
        Then PSU checks if the selection was successful and a correct SCA status and response code is received
        Examples:
            | selection-data                                |
            | selectAuthenticationMethod-successful.json    |


        Scenario Outline: Errorful selection of PSU sca method (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        And PSU updates his identification data
        And PSU prepares the errorful data <selection-data> with the payment service <payment-service>
        When PSU sends the errorful update authorisation data request
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service    | selection-data                           |
#             | payments           | selectAuth-not-existing-paymentId.json   |
            | payments           | selectAuth-no-request-id.json            |
#            | payments           | selectAuth-wrong-format-request-id.json  |
#            | payments            | selectAuth-wrong-sca-method.json         |
#            | recurring-payments  | selectAuth-wrong-payment-service.json    |
#            | payments            | selectAuth-wrong-authorisation-id.json   |


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
  ####################################################################################################################
    #                                                                                                                  #
    # Get Authorisations                                                                                              #
    #                                                                                                                  #
    ####################################################################################################################

    Scenario Outline: get authorisations (embedded)
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU sends the start authorisation request and receives the authorisationId
        When PSU sends the successful authorisation IDs data request
        Then a successful response code and the appropriate list of authorisation Ids are received
        Examples:

