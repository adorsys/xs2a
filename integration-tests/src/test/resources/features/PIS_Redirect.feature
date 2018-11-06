Feature: Payment Initiation Service - Redirect approach

    ####################################################################################################################
    #                                                                                                                  #
    # Single Payment Initiation                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################

    Scenario Outline: Successful payment initiation request for single payments (redirect)
        Given PSU wants to initiate a single payment <single-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the single payment initiating request
        Then a successful response code and the appropriate payment response data are received
        And a redirect URL is delivered to the PSU
        Examples:
            | payment-service | payment-product       | single-payment                |
            | payments        | sepa-credit-transfers | singlePayInit-successful.json |
            | payments        | sepa-credit-transfers | singlePayInit-exceeding-amount.json |

    ####################################################################################################################
    #                                                                                                                  #
    # Bulk Payment                                                                                                     #
    #                                                                                                                  #
    ####################################################################################################################
    # Bulk Payment is currently not considered in the xs2a interface, hence the tests are commented. The response of the
    # interface needs to be adapted to the new specification v1.2 see issue: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/294

    @ignore
    Scenario Outline: Payment initiation request for bulk payments (redirect)
        Given PSU wants to initiate multiple payments <bulk-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the bulk payment initiating request
        Then a successful response code and the appropriate payment response data are received
        And a redirect URL is delivered to the PSU
        Examples:
            | payment-service  | payment-product       | bulk-payment                |
            | bulk-payments     | sepa-credit-transfers | bulkPayInit-successful.json |
            |  bulk-payments    | sepa-credit-transfers | bulkPayInit-one-exceeding-amount.json        |

    ####################################################################################################################
    #                                                                                                                  #
    # Recurring Payments                                                                                               #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Payment initiation request for recurring payments (redirect)
        Given PSU wants to initiate a recurring payment <recurring-payment> using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the recurring payment initiating request
        Then a successful response code and the appropriate payment response data are received
        And a redirect URL is delivered to the PSU
        Examples:
            | payment-service   | payment-product       | recurring-payment          |
            | periodic-payments | sepa-credit-transfers | recPayInit-successful.json |
            | periodic-payments | sepa-credit-transfers | recPayInit-exceeding-amount.json            |
