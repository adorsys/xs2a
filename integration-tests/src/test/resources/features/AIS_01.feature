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
            | consent-resource                           |
            | consent-dedicated-successful.json |
            | consent-all-psd2-accounts-successful.json  |
            | consent-all-accounts-successful.json       |

#    #TODO Errorful Request
#
#    Scenario Outline: Successful consent status request (redirect)
#        Given AISP wants to get the status of a consent <consent-resource>
#        When AISP requests consent status
#        Then a successful response code and the appropriate consent status gets returned
#        Examples:
#            | consent-resource         |
#            | consent-status-RCVD.json |
#
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
#    Scenario Outline: Successful deletion of consent (redirect)
#        Given PSU wants to delete the consent <consent-resource>
#        When PSU deletes consent
#        Then a successful response code and the appropriate messages get returned
#        Examples:
#            | consent-resource                 |
#            | consent-deletion-successful.json |
#
#
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Account Request                                                                                                  #
#    #                                                                                                                  #
#    ####################################################################################################################
#    Scenario: Read account list of regular accounts
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                                                  | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456770}, {iban: DE2310010010123456780}], transactions: [{iban: DE2310010010123456770}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        When AISP requests the list of accounts
#        Then the following data is delivered to the AISP
#            | id                                   | iban                  | currency | accountType    | cashAccountType | name            | links                                                                                                                                              |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e80f | DE2310010010123456770 | EUR      | Girokonto      | CurrentAccount  | Main Account    | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e81g | DE2310010010123456780 | EUR      | Tagesgeldkonto | CurrentAccount  | Savings Account | balances /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e81g/balances                                                                                |
#
#    Scenario: Read account list of a multicurrency account with data-access on sub-account level
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                                                                                                                                               | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456760, currency: EUR}, {iban: DE2310010010123456760, currency: USD}], transactions: [{iban: DE2310010010123456760, currrency: EUR}, {iban: DE2310010010123456760, currrency: USD}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        When AISP requests the list of accounts
#        Then the following data is delivered to the AISP
#            | id                                   | iban                  | currency | accountType        | cashAccountType | name             | links                                                                                                                                              |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e809 | DE2310010010123456760 | EUR      | Girokonto          | CurrentAccount  | Main Account     | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/transactions |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e810 | DE2310010010123456760 | USD      | Fremdwährungskonto | CurrentAccount  | US Dolar Account | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e810/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e810/transactions |
#
#    Scenario: Read account list of a multicurrency account with data-access on aggregation and sub-account level
#        Given A consent resource with the following data exists at the ASPSP
#            | access                                                                                                                                                                                                                                                                             | recurringIndicator | validUntil | frequencyPerDay | transactionStatus           | consentStatus | links                      |
#            | balances: [{iban: DE2310010010123456760}, {iban: DE2310010010123456760, currency: EUR}, {iban: DE2310010010123456760, currency: USD}], transactions: [{iban: DE2310010010123456760}, {iban: DE2310010010123456760, currrency: EUR}, {iban: DE2310010010123456760, currrency: USD}] | true               | 2017-11-01 | 4               | AcceptedTechnicalValidation | valid         | viewAccounts: /v1/accounts |
#        When AISP requests the list of accounts
#        Then the following data is delivered to the AISP
#            | id                                   | iban                  | currency | accountType            | cashAccountType | name                | links                                                                                                                                              |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e809 | DE2310010010123456760 | XXX      | Multi Currency Account | CurrentAccount  | Aggregation Account | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/transactions |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e809 | DE2310010010123456760 | EUR      | Girokonto              | CurrentAccount  | Main Account        | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e809/transactions |
#            | 3dc3d5b3-7023-4848-9853-f5400a64e810 | DE2310010010123456760 | USD      | Fremdwährungskonto     | CurrentAccount  | US Dolar Account    | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e810/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e810/transactions |
#
#
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
