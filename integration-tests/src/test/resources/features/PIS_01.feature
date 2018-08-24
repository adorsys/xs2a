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
#        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
#        When PSU sends the single payment initiating request with error
#        Then an error response code is displayed the appropriate error response
#        Examples:
#            | payment-product               | single-payment                                 |
#            | sepa-credit-transfers         | singlePayInit-incorrect-syntax.json            |
#            | sepa-credit-trans             | singlePayInit-incorrect-payment-product.json   |
#            | sepa-credit-transfers         | singlePayInit-no-request-id.json               |
#            | sepa-credit-transfers         | singlePayInit-no-ip-address.json               |
#            | sepa-credit-transfers         | singlePayInit-wrong-format-request-id.json     |
#            | sepa-credit-transfers         | singlePayInit-wrong-format-psu-ip-address.json |
#            | sepa-credit-transfers         | singlePayInit-exceeding-amount.json            |
#            | sepa-credit-transfers         | singlePayInit-expired-exec-date.json           |
#            | instant-sepa-credit-transfers | singlePayInit-unavailable-product-for-psu.json |



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
#            | sepa-credit-transfers | recPayInit-expired-exec-date.json           |
#            | sepa-credit-transfers | recPayInit-start-date-in-past.json          |
#            | sepa-credit-transfers | recPayInit-end-date-before-start-date.json  |



    ####################################################################################################################
    #                                                                                                                  #
    # Payment Status                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Successful Payment Status Request
#        Given PSU initiated a single payment with the payment-id <payment-id>
#        And created a payment status request with of that payment
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status <payment-status> is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-status                |
#            | 529e0507-7539-4a65-9b74-bdf87061e99b | paymentStatus-successful.json |

#    Scenario Outline: Payment Status Request with not existing Payment-ID
#        Given PSU created a payment status request with of a not existing payment-id <payment-id>
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status <payment-status> is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-status                     |
#            | 529e0507-7539-4a65-9b74-bdf87061e99b | paymentStatus-not-existing-id.json |
