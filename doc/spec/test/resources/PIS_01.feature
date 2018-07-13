Feature: Payment Initiation Service

    ####################################################################################################################
    #                                                                                                                  #
    # Single Payment                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Successful payment initiation request for single payments
        Given PSU is logged in
        And <sca-approach> approach is used
        And PSU wants to initiate a single payment <single-payment> using the payment product <payment-product>
        When PSU sends the payment initiating request
        Then a payment resource is created at the aspsp mock
        And a successful response code and
        And the appropriate single payment response data is delivered to the PSU
        Examples:
            | sca-approach | payment-product      | single-payment                |
            | redirect     | sepa-credit-transfer | singlePayInit-successful.json |


    Scenario Outline: Failed payment initiation request for single payments
        Given PSU is logged in
        And <sca-approach> approach is used
        And PSU wants to initiate a single payment <single-payment> using the payment product <payment-product>
        When PSU sends the payment initiating request
        Then an error response code is displayed
        And the appropriate error response is delivered to the PSU
        Examples:
            | sca-approach | payment-product      | single-payment                                 |
            | redirect     | sepa-credit-trans    | singlePayInit-incorrect-payment-product.json   |
            | redirect     | sepa-credit-transfer | singlePayInit-incorrect-syntax.json            |
            | redirect     | sepa-credit-transfer | singlePayInit-no-transaction-id.json           |
            | redirect     | sepa-credit-transfer | singlePayInit-no-request-id.json               |
            | redirect     | sepa-credit-transfer | singlePayInit-no-ip-address.json               |
            | redirect     | sepa-credit-transfer | singlePayInit-wrong-format-transaction-id.json |
            | redirect     | sepa-credit-transfer | singlePayInit-wrong-format-request-id.json     |
            | redirect     | sepa-credit-transfer | singlePayInit-wrong-format-psu-ip-address.json |



    # TODO Single payment initiation with incorrect body syntax -> 400 //
    # TODO Single payment with not existing tpp-transaction-id -> 400  (are there not existant id's / not in the system?)
    # TODO Single payment with not existing tpp-request-id -> 400      (are there not existant id's / not in the system?)
    # TODO Single payment with not existing psu-ip-address -> 400      (are there not existant id's / not in the system?)
    # TODO Single payment without tpp-transaction-id -> 400 //
    # TODO Single payment without tpp-request-id --> 400 //
    # TODO Single payment without psu-ip-address -> 400 //
    # TODO Single payment with wrong uuid-format for tpp-transaction-id -> 400 //
    # TODO Single payment with wrong uuid-format for tpp-request-id -> 400 //
    # TODO Single payment with wrong uuid-format for psu-ip-address -> 400 //


    ####################################################################################################################
    #                                                                                                                  #
    # Bulk Payment                                                                                                     #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Payment initiation request for bulk payments
        Given PSU is logged in
        And <sca-approach> approach is used
        And PSU wants to initiate multiple payments <bulk-payment>
        When PSU sends the payment initiating request
        Then multiple payment resources are created at the aspsp mock
        And a successful response code and
        And the appropriate bulk payment response data is delivered to the PSU
        Examples:
            | sca-approach | bulk-payment                            |
            | redirect     | successful-bulk-payment-initiation.json |


    ####################################################################################################################
    #                                                                                                                  #
    # Standing Orders                                                                                                  #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Payment initiation request for standing orders
        Given PSU is logged in
        And <sca-approach> approach is used
        And PSU wants to initiate a standing order <recurring-payment>
        Then a payment resource is created at the aspsp mock
        And a successful response code and
        And the appropriate recurring payment response data is delivered to the PSU
        Examples:
            | sca-approach | recurring-payment                            |
            | redirect     | successful-recurring-payment-initiation.json |

    # TODO Recurring payment initiation with not defined frequency -> 400
    # TODO Recurring payment initiation with start date in the past -> 400


    ####################################################################################################################
    #                                                                                                                  #
    # Payment Status                                                                                                   #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Payment Status Request
        Given PSU is logged in
        And created payment status request with resource-id <resource-id>
        When PSU requests the status of a payment
        Then The payment status request by the PSU should be seen in the aspsp mock
        And response code <response-code>
        And Transaction status <transaction-status> is delivered to the PSU
        Examples:
            | resource-id      | transaction-status      | response-code |
            | qwer3456tzui7890 | AcceptedCustomerProfile | 200           |
