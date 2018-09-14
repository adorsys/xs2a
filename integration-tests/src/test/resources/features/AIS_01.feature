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
            | consent-resource                          |
            | consent-dedicated-successful.json         |
            | consent-all-psd2-accounts-successful.json |
            | consent-all-accounts-successful.json      |

#    #TODO Errorful Request

    Scenario Outline: Failed consent request creation (redirect)
        Given PSU wants to create a consent <consent-resource>
        When PSU sends the create consent request with error
        Then an error response code is displayed the appropriate error response
        Examples:
            | consent-resource                      |
            | consent-all-psd2-no-psu-id.json       |
            | consent-all-psd2-wrong-psu-id.json    |
            #| consent-all-psd2-wrong-value.json          |
            | consent-dedicated-incorrect-iban.json |

#
#    Scenario Outline: Successful consent status request (redirect)
#        Given AISP wants to get the status of a consent <consent-id> and the data <consent-resource>
#        When AISP requests consent status
#        Then a successful response code and the appropriate consent status gets returned
#        Examples:
#            | consent-resource                       | consent-id    |
#            | consent-status-expired.json            | to-be-defined |
#            | consent-status-missing-consent-id.json | to-be-defined |
#            | consent-status-received.json           | to-be-defined |
#            | consent-status-rejected.json           | to-be-defined |
#            | consent-status-revoked-by-psu.json     | to-be-defined |
#            | consent-status-terminated-by-tpp.json  | to-be-defined |
#            | consent-status-valid.json              | to-be-defined |
#
#    Scenario Outline: Successful consent request (redirect)
#        Given PSU wants to get the content of a consent <consent-resource>
#        When PSU requests consent
#        Then a successful response code and the appropriate consent gets returned
#        Examples:
#            | consent-resource        |
#            | consent-successful.json |
#
#
    Scenario Outline: Successful deletion of consent (redirect)
        Given PSU wants to delete the consent <consent-resource>
        When PSU deletes consent
        Then a successful response code and the appropriate messages get returned
        Examples:
            | consent-resource                 |
            | consent-deletion-successful.json |
#
#
#
    ####################################################################################################################
    #                                                                                                                  #
    # Account Request                                                                                                  #
    #                                                                                                                  #
    ####################################################################################################################
    Scenario Outline: Request account list successfully
        Given PSU already has an existing consent <consent-resource>
        When PSU requests the list of accounts
        Then a successful response code and the appropriate list of accounts get returned
        Examples:
            | consent-resource            |
            | accountList-successful.json |
            #| accountList-with-more-accounts-successful.json |
    # ToDo: Account with more than one account: Ticket PSD-103 needs to be done first


    Scenario Outline: Request account list errorful
        Given PSU already has an existing consent <consent-resource>
        When PSU sends get request
        Then an error response code is displayed the appropriate error response
        Examples:
            | consent-resource                         |
            | accountList-no-request-id.json           |
            | accountList-wrong-format-request-id.json |
            | accountList-invalid-request-id.json      |
            | accountList-no-consent.json              |
            #| accountList-with-expired-consent.json    |
    # ToDo: expired consent: Ticket PSD-179 needs to be done first

    Scenario Outline: Request account details successfully
        Given PSU already has an existing consent <consent-resource> with account id <account-id>
        When PSU requests the account details
        Then a successful response code and the appropriate details of accounts get returned
        Examples:
            | consent-resource              | account-id                           |
            | accountDetail-successful.json | 42fb4cc3-91cb-45ba-9159-b87acf6d8add |
    #ToDo: Check if account ID is the correct one

#    Scenario: Read account details of a regular account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456770}], transactions: [{iban: DE2310010010123456770}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e111 of the required account
#        When AISP requests account details
#        Then the following data is delivered to the AISP
#            | id                                   | iban                  | currency | accountType | cashAccountType | name         | links                                                                                                                                              |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e111 | DE2310010010123456770 | EUR      | Girokonto   | CurrentAccount  | Main Account | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e111/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e111/transactions |
#
#    Scenario: Read account details of a multi-currency account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456760}], transactions: [{iban: DE2310010010123456760}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e809 of the required account
#        When AISP requests account details
#        Then the following data is delivered to the AISP
#            | id                                   | iban                  | currency | accountType           | cashAccountType | name                | links                                                                                                                                              |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e809 | DE2310010010123456760 | XXX      | Multicurrency Account | CurrentAccount  | Aggregation Account | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/transactions |
#
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Balance Request                                                                                                  #
#    #                                                                                                                  #
#    ####################################################################################################################
#    Scenario: Read balance of a regular account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456770}], transactions: [{iban: DE2310010010123456770}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e111 of the required account
#        When AISP requests balance
#        Then the following balances are delivered to the AISP
#            | closingBooked                                              | expected                                                                               |
#            | amount: {currency: EUR, content: 500.00}, date: 2017-10-25 | amount: {currency: EUR, content: 900.00}, lastActionDateTime: 2017-10-25T15:30:35.035Z |
#
#    Scenario: Read balance of a multi-currency account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456760}], transactions: [{iban: DE2310010010123456760}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e809 of the required account
#        When AISP requests balance
#        Then the following balances are delivered to the AISP
#            | closingBooked                                              | expected                                                                               |
#            | amount: {currency: EUR, content: 500.00}, date: 2017-10-25 | amount: {currency: EUR, content: 900.00}, lastActionDateTime: 2017-10-25T15:30:35.035Z |
#            | amount: {currency: USD, content: 350.00}, date: 2017-10-25 | amount: {currency: USD, content: 350.00}, lastActionDateTime: 2017-10-24T14:30:21Z     |
#
#
#
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Transaction Request                                                                                              #
#    #                                                                                                                  #
#    ####################################################################################################################
#    Scenario: Read transaction list of a regular account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456770}], transactions: [{iban: DE2310010010123456770}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e111 of the required account
#        When AISP requests transaction list
#        Then response code 200
#        And an array of booked transactions
#            | transactionId | creditorName  | creditorAccount              | amount                            | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234567       | John Miles    | iban: DE43533700240123456900 | {currency: EUR, content: -256.67} | 2017-10-25  | 2017-10-26 | Example 1                         |
#            | 1234568       | Paul Simpsons | iban: NL354543123456900      | {currency: EUR, content: 343.01}  | 2017-10-25  | 2017-10-26 | Example 2                         |
#        And an array of pending transactions
#            | transactionId | creditorName   | creditorAccount           | amount                           | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234569       | Claude Renault | iban: FR33554543123456900 | {currency: EUR, content: -100.03 | 2017-10-25  | 2017-10-26 | Example 3                         |
#        And the link viewAccount = "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e111" is delivered to the AISP
#
#
#    Scenario: Read transaction list of a multi-currency account
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                   | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456760}], transactions: [{iban: DE2310010010123456760}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e809 of the required account
#        When AISP requests transaction list
#        Then response code 200
#        And an array of booked transactions
#            | transactionId | creditorName  | creditorAccount              | amount                            | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234567       | John Miles    | iban: DE43533700240123456900 | {currency: EUR, content: -256.67} | 2017-10-25  | 2017-10-26 | Example 1                         |
#            | 1234568       | Paul Simpsons | iban: NL354543123456900      | {currency: EUR, content: 343.01}  | 2017-10-25  | 2017-10-26 | Example 2                         |
#            | 1234569       | Pepe Martin   | iban: SE1234567891234        | {currency: USD, content: 100.00}  | 2017-10-25  | 2017-10-26 | Example 3                         |
#        And an array of pending transactions
#            | transactionId | creditorName   | creditorAccount           | amount                           | bookingDate | value_Date | remittanceInformationUnstructured |
#            | 1234569       | Claude Renault | iban: FR33554543123456900 | {currency: EUR, content: -100.03 | 2017-10-25  | 2017-10-26 | Example 3                         |
#        And the link viewAccount = "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809" is delivered to the AISP
#
