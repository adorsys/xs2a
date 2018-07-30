Feature: Payment Initiation Service

    ####################################################################################################################
    #                                                                                                                  #
    # Single Payment                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful payment initiation request for single payments (redirect)
        Given PSU is logged in using redirect approach
        And PSU wants to initiate a single payment <single-payment> using the payment product <payment-product>
        When PSU sends the single payment initiating request
        Then a successful response code and the appropriate single payment response data
        And a redirect URL is delivered to the PSU
        Examples:
            | payment-product       | single-payment                |
            | sepa-credit-transfers | singlePayInit-successful.json |

    Scenario Outline: Failed payment initiation request for single payments (redirect)
        Given PSU is logged in using redirect approach
        And PSU wants to initiate a single payment <single-payment> using the payment product <payment-product>
        When PSU sends the single payment initiating request with error
        Then an error response code is displayed the appropriate error response
#        Not implemented yet
#        And a redirect URL is delivered to the PSU
        Examples:
            | payment-product       | single-payment                                 |
            | sepa-credit-transfers | singlePayInit-incorrect-syntax.json            |
            | sepa-credit-trans     | singlePayInit-incorrect-payment-product.json   |
            | sepa-credit-transfers | singlePayInit-no-request-id.json               |
            | sepa-credit-transfers | singlePayInit-no-ip-address.json               |
            | sepa-credit-transfers | singlePayInit-wrong-format-request-id.json     |
            | sepa-credit-transfers | singlePayInit-wrong-format-psu-ip-address.json |
            | sepa-credit-transfers | singlePayInit-exceeding-amount.json            |
            | sepa-credit-transfers | singlePayInit-expired-exec-date.json           |


        # TODO create Scenario for other SCA-Approaches

    # TODO Single payment with not existing tpp-request-id -> 400      (are there not existant id's / not in the system?)
    # TODO Single payment with not existing psu-ip-address -> 400      (are there not existant id's / not in the system?)


    ####################################################################################################################
    #                                                                                                                  #
    # Bulk Payment                                                                                                     #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Payment initiation request for bulk payments (redirect)
        Given PSU is logged in using redirect approach
        And PSU wants to initiate multiple payments <bulk-payment> using the payment product <payment-product>
        When PSU sends the bulk payment initiating request
        Then a successful response code and the appropriate bulk payment response data
        And a redirect URL for every payment of the Bulk payment is delivered to the PSU
        Examples:
            | payment-product       | bulk-payment                |
            | sepa-credit-transfers | bulkPayInit-successful.json |


    ####################################################################################################################
    #                                                                                                                  #
    # Recurring Payments                                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Payment initiation request for recurring payments (redirect)
#        Given PSU is logged in using redirect approach
#        And PSU wants to initiate a recurring payment <recurring-payment> using the payment product <payment-product>
#        When PSU sends the recurring payment initiating request
#        Then a successful response code and the appropriate recurring payment response data
#        And a redirect URL is delivered to the PSU
#        Examples:
#            | payment-product       | recurring-payment          |
#            | sepa-credit-transfers | recPayInit-successful.json |


#    Scenario Outline: Failed payment initiation request for recurring payments (redirect)
#        Given PSU is logged in using redirect approach
#        And PSU wants to initiate a recurring payment <recurring-payment> using the payment product <payment-product>
#        When PSU sends the recurring payment initiating request
#        Then an error response code is displayed the appropriate error response
#        And a redirect URL is delivered to the PSU
#        Examples:
#            | payment-product      | recurring-payment                           |
#            | sepa-credit-transfer | recPayInit-incorrect-syntax.json            |
#            | sepa-credit-trans    | recPayInit-incorrect-payment-product.json   |
#            | sepa-credit-transfer | recPayInit-no-frequency.json                |
#            | sepa-credit-transfer | recPayInit-not-defined-frequency.json       |
#            | sepa-credit-transfer | recPayInit-no-request-id.json               |
#            | sepa-credit-transfer | recPayInit-no-ip-address.json               |
#            | sepa-credit-transfer | recPayInit-wrong-format-request-id.json     |
#            | sepa-credit-transfer | recPayInit-wrong-format-psu-ip-address.json |
#            | sepa-credit-transfer | recPayInit-exceeding-amount.json            |
#            | sepa-credit-transfer | recPayInit-expired-exec-time.json           |
#            | sepa-credit-transfer | recPayInit-start-date-in-past.json          |
#            | sepa-credit-transfer | recPayInit-end-date-before-start-date.json  |


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Status                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
#    Scenario Outline: Successful Payment Status Request
#        Given PSU is logged in
#        And initiated a single payment with the payment-id <payment-id>
#        And created a payment status request with of that payment
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status <payment-status> is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-status                |
#            | 529e0507-7539-4a65-9b74-bdf87061e99b | paymentStatus-successful.json |

#    Scenario Outline: Payment Status Request with not existing Payment-ID
#        Given PSU is logged in
#        And created a payment status request with of a not existing payment-id <payment-id>
#        When PSU requests the status of the payment
#        Then an appropriate response code and the status <payment-status> is delivered to the PSU
#        Examples:
#            | payment-id                           | payment-status                     |
#            | 529e0507-7539-4a65-9b74-bdf87061e99b | paymentStatus-not-existing-id.json |
