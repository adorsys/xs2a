Feature: Payment Initiation Service

    ####################################################################################################################
    #                                                                                                                  #
    # Single Payment                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Successful payment initiation request for single payments (redirect)
#        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
#        When PSU sends the single payment initiating request
#        Then a successful response code and the appropriate single payment response data
#        And a redirect URL is delivered to the PSU
#        Examples:
#            | payment-service | payment-product       | single-payment                |
#            | payments        | sepa-credit-transfers | singlePayInit-successful.json |

#    Scenario Outline: Failed payment initiation request for single payments (redirect)
#        Given PSU wants to initiate a single payment <single-payment> using the payment product <payment-product>
#        When PSU sends the single payment initiating request with error
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | payment-product               | single-payment                                 |
#            | sepa-credit-transfers         | singlePayInit-incorrect-syntax.json            |
#            #| sepa-credit-trans             | singlePayInit-incorrect-payment-product.json   |
#            | sepa-credit-transfers         | singlePayInit-no-request-id.json               |
#            | sepa-credit-transfers         | singlePayInit-no-ip-address.json               |
#            | sepa-credit-transfers         | singlePayInit-wrong-format-request-id.json     |
#            | sepa-credit-transfers         | singlePayInit-wrong-format-psu-ip-address.json |
#            | sepa-credit-transfers         | singlePayInit-exceeding-amount.json            |



#    Scenario Outline: Successful payment initiation request for single payments (oauth)
#        Given PSU request access token for oauth approach
#        Given PSU wants to initiate a single payment <single-payment> using the payment product <payment-product>
#        When PSU sends the single payment initiating request
#        Then a successful response code and the appropriate single payment response data
#        Examples:
#            | payment-product       | single-payment                |
#            | sepa-credit-transfers | singlePayInit-successful.json |


    ####################################################################################################################
    #                                                                                                                  #
    # Bulk Payment                                                                                                     #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Payment initiation request for bulk payments (redirect)
#        Given PSU wants to initiate multiple payments <bulk-payment> using the payment product <payment-product>
#        When PSU sends the bulk payment initiating request
#        Then a successful response code and the appropriate bulk payment response data
#        And a redirect URL for every payment of the Bulk payment is delivered to the PSU
#        Examples:
#            | payment-product       | bulk-payment                |
#            | sepa-credit-transfers | bulkPayInit-successful.json |


#    Scenario Outline: Failed payment initiation request for bulk payments (redirect)
#        Given PSU wants to initiate a multiple payments <recurring-payment> using the payment product <payment-product>
#        When PSU sends the bulk payment initiating request with error
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | payment-product       | recurring-payment                            |
#            | sepa-credit-transfers | bulkPayInit-incorrect-syntax.json            |
#            | sepa-credit-trans     | bulkPayInit-incorrect-payment-product.json   |
#            | sepa-credit-transfers | bulkPayInit-no-request-id.json               |
#            | sepa-credit-transfers | bulkPayInit-no-ip-address.json               |
#            | sepa-credit-transfers | bulkPayInit-wrong-format-request-id.json     |
#            | sepa-credit-transfers | bulkPayInit-wrong-format-psu-ip-address.json |
#            | sepa-credit-transfers | bulkPayInit-exceeding-amount.json            |



    ####################################################################################################################
    #                                                                                                                  #
    # Recurring Payments                                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Payment initiation request for recurring payments (redirect)
#        Given PSU wants to initiate a recurring payment <recurring-payment> using the payment product <payment-product>
#        When PSU sends the recurring payment initiating request
#        Then a successful response code and the appropriate recurring payment response data
#        And a redirect URL is delivered to the PSU
#        Examples:
#            | payment-product       | recurring-payment          |
#            | sepa-credit-transfers | recPayInit-successful.json |


#    Scenario Outline: Failed payment initiation request for recurring payments (redirect)
#        Given PSU wants to initiate a recurring payment <recurring-payment> using the payment product <payment-product>
#        When PSU sends the recurring payment initiating request with error
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | payment-product       | recurring-payment                           |
#            | sepa-credit-transfers | recPayInit-incorrect-syntax.json            |
#            #| sepa-credit-trans     | recPayInit-incorrect-payment-product.json   |
#            | sepa-credit-transfers | recPayInit-no-frequency.json                |
#            | sepa-credit-transfers | recPayInit-not-defined-frequency.json       |
#            | sepa-credit-transfers | recPayInit-no-request-id.json               |
#            | sepa-credit-transfers | recPayInit-no-ip-address.json               |
#            | sepa-credit-transfers | recPayInit-wrong-format-request-id.json     |
#            | sepa-credit-transfers | recPayInit-wrong-format-psu-ip-address.json |
#            | sepa-credit-transfers | recPayInit-exceeding-amount.json            |
#            | sepa-credit-transfers | recPayInit-start-date-in-past.json          |
#            | sepa-credit-transfers | recPayInit-end-date-before-start-date.json  |



    ####################################################################################################################
    #                                                                                                                  #
    # Payment Status                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
#    -> is not working until the url is adapted according to the new version of the specification
#    Scenario Outline: Successful Payment Status Request
#        Given Psu wants to request the payment status of a payment with payment-id <payment-id> by using the payment-service <payment-service>
#        And the set of data <payment-status>
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-service | payment-status                |
#            | a9115f14-4f72-4e4e-8798-202808e85238 | payments        | paymentStatus-RCVD-successful.json |
#            | 68147b90-e4ef-41c6-9c8b-c848c1e93700 | payments        | paymentStatus-PDNG-successful.json |
#            | 97694f0d-32e2-43a4-9e8d-261f2fc28236 | payments        | paymentStatus-RJCT-successful.json |

#    Scenario Outline: Payment Status Request with not existing Payment-ID
#        Given PSU created a payment status request with of a not existing payment-id <payment-id>
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status <payment-status> is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-status                     |
#            | 529e0507-7539-4a65-9b74-bdf87061e99b | paymentStatus-not-existing-id.json |
