Feature: Account Information Service
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Consent Requests                                                                                                 #
#    #                                                                                                                  #
#    ####################################################################################################################
    Scenario Outline: Successful consent request creation (redirect)
        Given PSU wants to create a consent <consent-resource>
        When PSU sends the create consent request
        Then a successful response code and the appropriate consent response data is delivered to the PSU
        Examples:
            | consent-resource                     |
            | consent-dedicated-successful.json    |
#            | consent-all-psd2-accounts-successful.json  |
            | consent-all-accounts-successful.json |

    Scenario Outline: Failed consent request creation (redirect)
        Given PSU wants to create an erroful consent <consent-resource>
        When PSU sends the create consent request with error
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent-resource                      |
            #| consent-all-psd2-no-psu-id.json       |
            #| consent-all-psd2-wrong-psu-id.json    |
            #| consent-all-psd2-wrong-value.json          |
            | consent-dedicated-incorrect-iban.json |

    Scenario Outline: Expired consent request creation (redirect)
        Given PSU wants to create an expired consent <consent-resource>
        When PSU sends the create consent request with error
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent-resource                      |
            | consent-dedicated-expired-consent.json|

    Scenario Outline: Successful consent status request (redirect)
        Given PSU created a consent resource <consent-id>
        And AISP wants to get the status <consent-status> of that consent
        When AISP requests consent status
        Then a successful response code and the appropriate consent status gets returned
        Examples:
            | consent-status                      | consent-id                             |
            | consent-status-expired.json           | consent-dedicated-successful.json    |
            | consent-status-received.json          | consent-dedicated-successful.json    |
            | consent-status-rejected.json          | consent-dedicated-successful.json    |
            | consent-status-revoked-by-psu.json    | consent-dedicated-successful.json    |
            | consent-status-terminated-by-tpp.json | consent-dedicated-successful.json    |
            | consent-status-valid.json             | consent-dedicated-successful.json    |
            | consent-status-expired.json           | consent-all-accounts-successful.json |
            | consent-status-received.json          | consent-all-accounts-successful.json |
            | consent-status-rejected.json          | consent-all-accounts-successful.json |
            | consent-status-revoked-by-psu.json    | consent-all-accounts-successful.json |
            | consent-status-terminated-by-tpp.json | consent-all-accounts-successful.json |
            | consent-status-valid.json             | consent-all-accounts-successful.json |


    Scenario Outline: Errorful consent status request (redirect)
        Given AISP wants to get the status of that consent and the data <consent-resource> with not existing consent id
        When AISP requests errorFull consent status
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent-resource                                 |
            | consent-status-with-not-existing-consent-id.json |

    @ignore
    Scenario Outline: Successful consent request (redirect)
        Given PSU wants to get the content of a consent <consent-resource>
        When PSU requests consent
        Then a successful response code and the appropriate consent gets returned
        Examples:
            | consent-resource        |
            | consent-successful.json |


    Scenario Outline: Successful deletion of consent (redirect)
        Given PSU wants to create a consent <consent-id>
        And PSU sends the create consent request
        And PSU wants to delete the consent <consent-resource>
        When PSU deletes consent
        Then a successful response code and the appropriate messages get returned
        Examples:
            | consent-resource                 |        consent-id                 |
            | consent-deletion-successful.json | consent-dedicated-successful.json |

    @ignore
    Scenario Outline: Errorful deletion of consent (redirect)
        Given PSU created a consent resource <consent-id>
        And PSU wants to delete the consent <consent-resource>
        When PSU deletes consent
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent-id                   | consent-resource                              |
            | consents-create-consent.json | consent-deletion-no-request-id.json           |
            | consents-create-consent.json | consent-deletion-wrong-format-request-id.json |

    @ignore
    Scenario Outline: Errorful deletion of consent with no consent-id (redirect)
        Given PSU wants to delete the consent <consent-resource> without consent id
        When PSU deletes consent
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent-resource                 |
            | consent-deletion-no-consent.json |

    @ignore
    Scenario Outline: Errorful deletion of consent with expired consent-id (redirect)
        Given PSU created consent <consent> which is expired
        And PSU wants to delete the consent <consent-resource>
        When PSU deletes consent
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent                              | consent-resource                           |
            | consents-create-expired-consent.json | consent-deletion-with-expired-consent.json |


    ####################################################################################################################
    #                                                                                                                  #
    # Account Request                                                                                                  #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Request account list successfully
        Given PSU already has an existing <status> consent <consent-id>
        And wants to get a list of accounts using <account-resource>
        When PSU requests the list of accounts
        Then a successful response code and the appropriate list of accounts get returned
        Examples:
            | account-resource                               | consent-id                           | status |
            | accountList-successful.json                    | account/accounts-create-consent.json | valid  |


    Scenario Outline: Request account list errorful
        Given PSU already has an existing <status> consent <consent-id>
        And PSU wants to get a list of accounts errorful using <account-resource>
        When PSU sends get request
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | account-resource                                       | consent-id                                    | status           |
            | accountList-no-request-id.json                         | account/accounts-create-consent.json          |  valid           |
            | accountList-wrong-format-request-id.json               | account/accounts-create-consent.json          |  valid           |
            | accountList-with-expired-consent.json                  | account/accounts-create-consent.json          |  expired         |
            | accountList-with-received-consent.json                 | account/accounts-create-consent.json          |  received        |
            | accountList-with-rejected-consent.json                 | account/accounts-create-consent.json          |  rejected        |
            | accountList-with-revokedByPsu-consent.json             | account/accounts-create-consent.json          |  revokedByPsu    |
            | accountList-with-terminatedByTpp-consent.json          | account/accounts-create-consent.json          |  terminatedByTpp |
            | accountList-with-exceeded-frequencePerDay-consent.json | account/accounts-create-consent-exceeded.json |  valid           |



    Scenario Outline: Request account list with no consent errorful
        Given PSU wants to get a list of accounts errorful using <account-resource>
        When PSU sends get request
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | account-resource            |
            | accountList-no-consent.json |

    Scenario Outline: Request account details successfully
        Given PSU already has an existing <status> consent <consent-id>
        And account id <account-id>
        And wants to get account details using <account-resource>
        When PSU requests the account details
        Then a successful response code and the appropriate details of accounts get returned
        Examples:
            | account-resource              | account-id      | consent-id                           | status |
            | accountDetail-successful.json | 11111-999999999 | account/accounts-create-consent.json | valid  |


    Scenario Outline: Request account details errorful
        Given PSU already has an existing <status> consent <consent-id>
        And account id <account-id>
        And PSU wants to get a detail of accounts erroful using <account-resource>
        When PSU requests the account details erroful
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | account-resource                                         | consent-id                                    | status           | account-id                           |
            | accountDetail-with-accountid-nomatch-consent.json        | account/accounts-create-consent.json          |  valid           | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | accountDetail-wrong-format-request-id.json               | account/accounts-create-consent.json          |  valid           | 11111-999999999                      |
            | accountDetail-with-expired-consent.json                  | account/accounts-create-consent.json          |  expired         | 11111-999999999                      |
            | accountDetail-with-received-consent.json                 | account/accounts-create-consent.json          |  received        | 11111-999999999                      |
            | accountDetail-with-rejected-consent.json                 | account/accounts-create-consent.json          |  rejected        | 11111-999999999                      |
            | accountDetail-with-revokedByPsu-consent.json             | account/accounts-create-consent.json          |  revokedByPsu    | 11111-999999999                      |
            | accountDetail-with-terminatedByTpp-consent.json          | account/accounts-create-consent.json          |  terminatedByTpp | 11111-999999999                      |
            | accountDetail-with-exceeded-frequencePerDay-consent.json | account/accounts-create-consent-exceeded.json |  valid           | 11111-999999999                      |


    Scenario Outline: Request account details with no consent errorful
        Given PSU wants to get a detail of accounts erroful using <account-resource>
        When PSU requests the account details erroful
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | account-resource              |
            | accountDetail-no-consent.json |

#    ####################################################################################################################
#    #                                                                                                                  #
#    # Balance Request                                                                                                  #
#    #                                                                                                                  #
#    ####################################################################################################################
  @ignore
    Scenario Outline: Read balances successfully
        Given PSU already has an existing consent <consent>
        And account id <account-id>
        And wants to read all balances using <balance-resource>
        When PSU requests the balances
        Then a successful response code and the appropriate list of accounts get returned
        Examples:
            | consent                     | account-id                           | balance-resource            |
            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-successful.json |
            | balance-create-consent.json | 868beafc-ef87-4fdb-ac0a-dd6c52b77ee6 | readBalance-successful.json |

    @ignore
    Scenario Outline: Read balances errorful
        Given PSU already has an existing consent <consent>
        And account id <account-id>
        And wants to read all balances using <balance-resource>
        When PSU requests the balances
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent                     | account-id                           | balance-resource                         |
            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-no-request-id                |
            | balance-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-wrong-format-request-id.json |

    @ignore
    Scenario Outline: Read balances with no consent errorful
        Given PSU wants to read all balances using <balance-resource>
        And account id <account-id>
        When PSU requests the balances
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | balance-resource            | account-id                           |
            | readBalance-no-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |

    @ignore
    Scenario Outline: Read balances with expired consent errorful
        Given PSU created consent <consent> which is expired
        And account id <account-id>
        And wants to read all balances using <balance-resource>
        When PSU requests the balances
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent                             | account-id                           | balance-resource                      |
            | balance-create-expired-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | readBalance-with-expired-consent.json |
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Transaction Request                                                                                              #
#    #                                                                                                                  #
#    ####################################################################################################################
#
    Scenario Outline: Read transaction list successfully
        Given PSU already has an existing <status> consent <consent-id>
        And account id <account-id>
        And balance <balance> dateFrom <dateFrom> dateTo <dateTo> bookingStatus <bookingStatus> entryReferenceFrom <entryReferenceFrom> deltaList <deltaList>
        And wants to read all transactions using <transaction-resource>
        When PSU requests the transactions
        Then a successful response code and the appropriate list of transaction get returned
        Examples:
            | consent-id                                   | account-id                           | transaction-resource            | balance | dateFrom   | dateTo     | bookingStatus | entryReferenceFrom | deltaList | status |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json | false   | 2008-09-15 |            | booked        |                    |           | valid  |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json |         | 2008-09-15 | 2028-09-15 | booked        |                    |           | valid  |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json | true    | 2008-09-15 |            | booked        |                    |           | valid  |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json |         | 2008-09-15 |            | pending       |                    |           | valid  |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json |         | 2008-09-15 |            | both          |                    |           | valid  |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json |         | 2008-09-15 |            | booked        |                    | false     | valid  |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionList-successful.json |         | 2008-09-15 |            | booked        |                    | true      | valid  |


    @TestTag
    Scenario Outline: Read transaction list erroful
        Given PSU already has an existing <status> consent <consent-id>
        And account id <account-id>
        And wants to read all transactions errorful using <transaction-resource>
        When PSU requests the transactions erroful
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | transaction-resource                                       | consent-id                                                        | status           | account-id                           |
            | transactions-with-wrong-access.json                      | transaction/transactions-create-consent-with-account-access.json  |  valid           | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-wrong-access.json                      | transaction/transactions-create-consent-with-balance-access.json  |  valid           | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-accountid-nomatch-consent.json        | transaction/transactions-create-consent.json                      |  valid           | 11111-999999999 |
            | transactions-wrong-format-request-id.json               | transaction/transactions-create-consent.json                      |  valid           | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-expired-consent.json                  | transaction/transactions-create-consent.json                      |  expired         | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-received-consent.json                 | transaction/transactions-create-consent.json                      |  received        | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-rejected-consent.json                 | transaction/transactions-create-consent.json                      |  rejected        | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-revokedByPsu-consent.json             | transaction/transactions-create-consent.json                      |  revokedByPsu    | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-terminatedByTpp-consent.json          | transaction/transactions-create-consent.json                      |  terminatedByTpp | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
            | transactions-with-exceeded-frequencePerDay-consent.json | transaction/transactions-create-consent-exceeded.json             |  valid           | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |


    Scenario Outline: Read transaction details successfully
        Given PSU already has an existing <status> consent <consent-id>
        And account id <account-id>
        And wants to read the transaction details using <transaction-resource>
        And resource id <resource-id>
        When PSU requests the transaction details
        Then a successful response code and the appropriate detail of transaction get returned
        Examples:
            | consent-id                                   | account-id                           | transaction-resource              | resource-id                          | status |
            | transaction/transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionDetail-successful.json | ba8f7012-bdaf-4ada-bbf7-4c004d046ffe | valid  |

    @ignore
    Scenario Outline: Read transaction details errorful
        Given PSU already has an existing consent <consent-id>
        And account id <account-id>
        And wants to read the transaction details using <transaction-resource>
        And resource id <resource-id>
        When PSU requests the transaction details
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent-id                       | account-id                           | transaction-resource                           | resource-id                          |
            | transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionDetail-no-request-id.json           | ba8f7012-bdaf-4ada-bbf7-4c004d046ffe |
            | transactions-create-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionDetail-wrong-format-request-id.json | ba8f7012-bdaf-4ada-bbf7-4c004d046ffe |

    @ignore
    Scenario Outline: Read transaction detail with no consent errorful
        Given PSU wants to read the transaction details using <transaction-resource>
        And account id <account-id>
        And resource id <resource-id>
        When PSU requests the transaction details
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | transaction-resource              | account-id                           | resource-id                          |
            | transactionDetail-no-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | ba8f7012-bdaf-4ada-bbf7-4c004d046ffe |

    @ignore
    Scenario Outline: Read transaction details with expired consent errorful
        Given PSU created consent <consent> which is expired
        And account id <account-id>
        And wants to read the transaction details using <transaction-resource>
        And resource id <resource-id>
        When PSU requests the transaction details
        Then an error response code is displayed and an appropriate error response is shown
        Examples:
            | consent                                  | account-id                           | transaction-resource                        | resource-id                          |
            | transactions-create-expired-consent.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add | transactionDetail-with-expired-consent.json | ba8f7012-bdaf-4ada-bbf7-4c004d046ffe |

