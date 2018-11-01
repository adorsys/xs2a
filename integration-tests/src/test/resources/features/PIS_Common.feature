Feature: Payment Initiation Service - Common

    ####################################################################################################################
    #                                                                                                                  #
    # Single Payment                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################

    Scenario Outline: Failed payment initiation request for single payments (redirect)
        Given PSU initiates an errorful single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service     | payment-product               | single-payment                                 |
#            | payments            | sepa-credit-transfers         | singlePayInit-incorrect-syntax.json            |
            | payments            | sepa-credit-trans             | singlePayInit-incorrect-payment-product.json   |
            | payments            | sepa-credit-transfers         | singlePayInit-no-request-id.json               |
            | payments            | sepa-credit-transfers         | singlePayInit-no-ip-address.json               |
#            | payments            | sepa-credit-transfers         | singlePayInit-wrong-format-request-id.json     |
            | payments            | sepa-credit-transfers         | singlePayInit-wrong-format-psu-ip-address.json |
#            | recurring-payments  | sepa-credit-transfers         | singlePayInit-wrong-payment-service.json       |


    ####################################################################################################################
    #                                                                                                                  #
    # Bulk Payment                                                                                                     #
    #                                                                                                                  #
    ####################################################################################################################
# Bulk Payment is currently not considered in the xs2a interface, hence the tests are commented. The response of the
# interface needs to be adapted to the new specification v1.2 see issue: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/294
    @ignore
    Scenario Outline: Failed payment initiation request for bulk payments (redirect)
        Given PSU loads errorful multiple payments <bulk-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the bulk payment initiating request with error
        Then an error response code and the appropriate error response are received
        Examples:
          |  payment-service  | payment-product       | bulk-payment                                 |
          |  bulk-payments    | sepa-credit-trans     | bulkPayInit-incorrect-payment-product.json   |
          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-no-request-id.json               |
          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-no-ip-address.json               |
          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-wrong-format-request-id.json     |
          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-wrong-format-psu-ip-address.json |
          |  bulk-payments    | sepa-credit-transfers | bulkPayInit-one-incorrect-syntax.json        |


    ####################################################################################################################
    #                                                                                                                  #
    # Recurring Payments                                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Failed payment initiation request for recurring payments (redirect)
        Given PSU loads an errorful recurring payment <recurring-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the recurring payment initiating request with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-service   | payment-product       | recurring-payment                           |
#            | periodic-payments | sepa-credit-transfers | recPayInit-incorrect-syntax.json            |
            | periodic-payments | sepa-credit-trans     | recPayInit-incorrect-payment-product.json   |
            | periodic-payments | sepa-credit-transfers | recPayInit-no-frequency.json                |
            | periodic-payments | sepa-credit-transfers | recPayInit-not-defined-frequency.json       |
            | periodic-payments | sepa-credit-transfers | recPayInit-no-request-id.json               |
            | periodic-payments | sepa-credit-transfers | recPayInit-no-ip-address.json               |
#            | periodic-payments | sepa-credit-transfers | recPayInit-wrong-format-request-id.json     |
            | periodic-payments | sepa-credit-transfers | recPayInit-wrong-format-psu-ip-address.json |
#            | periodic-payments | sepa-credit-transfers | recPayInit-start-date-in-past.json          |
#            | periodic-payments | sepa-credit-transfers | recPayInit-end-date-before-start-date.json  |


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Status                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful payment status request
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU wants to request the payment status by using a set of data <payment-status-data>
        When PSU requests the status of the payment
        Then a successful response code and the correct payment status is delivered to the PSU
        Examples:
            | payment-status-data                     |
            | paymentStatus-RCVD-successful.json      |


    Scenario Outline: Failed payment status request
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU prepares the errorful payment status request data <payment-status-data> with the payment service <payment-service>
        When PSU requests the status of the payment with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-status-data                       | payment-service |
#            |paymentStatus-not-existing-paymentId.json  | payments               |
            |paymentStatus-no-request-id.json           | payments               |
#            |paymentStatus-wrong-format-request-id.json | payments               |
#            |paymentStatus-wrong-payment-service.json   | recurring-payments     |


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Information                                                                                              #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful Single Payment Information Request
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU wants to request the payment information by using a set of data <payment-information-data>
        When PSU requests the information of the payment
        Then a successful response code and the payment information is delivered to the PSU
        Examples:
           | payment-information-data             |
           | singlePayInformation-successful.json |


    Scenario Outline: Failed Payment Information Request
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU prepares the errorful payment information request data <payment-information-data> with the payment service <payment-service>
        When PSU requests the information of the payment with error
        Then an error response code and the appropriate error response are received
        Examples:
           | payment-information-data                          | payment-service |
#           | singlePayInformation-not-existing-paymentId.json  | payments                    |
#           | singlePayInformation-wrong-format-request-id.json | payments                    |
           | singlePayInformation-no-request-id.json           | payments                    |
#           | singlePayInformation-wrong-payment-service.json   | recurring-payments          |


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Cancellation                                                                                             #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful payment cancellation request
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU wants to cancel the payment by using a set of data <payment-cancellation-data>
        When PSU initiates the cancellation of the payment
        Then an successful response code and the appropriate transaction status is delivered to the PSU
        Examples:
            | payment-cancellation-data           |
            | paymentCancellation-successful.json |


   Scenario Outline: Failed payment cancellation request
        Given PSU sends the single payment initiation request and receives the paymentId
        And PSU prepares the errorful cancellation request data <payment-cancellation-data> with the payment service <payment-service>
        When PSU initiates the cancellation of the payment with error
        Then an error response code and the appropriate error response are received
        Examples:
            | payment-cancellation-data                        | payment-service |
#            | paymentCancellation-not-existing-paymentId.json  | payments                    |
#            | paymentCancellation-wrong-format-request-id.json | payments                    |
            | paymentCancellation-no-request-id.json           | payments                    |
#           | paymentCancellation-wrong-payment-service.json   | recurring-payments          |
