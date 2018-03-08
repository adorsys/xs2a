Feature: Account Information Service

  Scenario: Consent Request on Dedicated Accounts with Redirect Approach
    Given AISP wants to create a consent resource with data
      | access | recurringIndicator | validUntil | frequencyPerDay |
      | balances: [{iban: DE2310010010123456789}, {iban: DE2310010010123456790, currency: USD}, {iban: DE2310010010123456788}], transactions: [{iban: DE2310010010123456789}, {maskedPan: 123456xxxxxx1234}]  | true | 2017-11-01 | 4 |
    And the redirect approach is used
    When AISP sends the create consent request
    Then a consent resource is created at the aspsp mock
    And response code 200
    And the following data is delivered to the AISP:
      | transactionStatus | consentId | links |
      | Received | 1234-wertiq-983    | redirect: https://www.testbank.com/authentication/1234-wertiq-983 |

  Scenario: Consent Request on Dedicated Accounts with OAuth2 Approach
    Given AISP wants to create a consent resource with data
      | access | recurringIndicator | validUntil | frequencyPerDay |
      | balances: [{iban: DE2310010010123456789}, {iban: DE2310010010123456790, currency: USD}, {iban: DE2310010010123456788}], transactions: [{iban: DE2310010010123456789}, {maskedPan: 123456xxxxxx1234}]  | true | 2017-11-01 | 4 |
    And the OAuth2 approach is used
    When AISP sends the create consent request
    Then a consent resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the AISP:
      | transactionStatus | consentId | links |
      | Received | 1234-wertiq-983    | {self: /v1/consents/1234-wertiq-983, consentId: 1234-wertiq-983}     |

  Scenario: Get Status Request
    Given AISP created a consent resource with consent-id 1234-wertiq-983
    When AISP requests consent status
    Then Transaction status "AcceptedTechnicalValidation"
    And response Code 200 is delivered to the gateway user

  Scenario: Get Consent Request
    Given AISP created a consent resource with consent-id 1234-wertiq-983
    When AISP requests information about the consent resource
    Then  the following data is delivered to the AISP:
      | access | recurringIndicator | validUntil | frequencyPerDay | transactionStatus | consentStatus | links |
      | balances: [{iban: DE2310010010123456789}], transactions: [{iban: DE2310010010123456789}, {pan: 123456xxxxxx3457}]  | true | 2017-11-01 | 4 | AcceptedTechnicalValidation | valid | viewAccounts: /v1/accounts |

  Scenario: Delete Consent Request
    Given AISP created a consent resource for two different ibans with consent-id 1234-wertiq-983
    When AISP sends delete consent request
    Then the consent resource is deleted at the aspsp mock
    And response code 204 is delivered to the AISP

  Scenario: Read Account List
    Given AISP created a consent resource with consent-id 1234-wertiq-983
    When AISP requests the list of accounts
    Then the following data is delivered to the AISP
      | id | iban | currency | accountType | cashAccountType | name | links |
      | 3dc3d5b3-7023-4848-9853-f5400a64e80f | DE2310010010123456789 | EUR | Girokonto | CurrentAccount | Main Account | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions |
      | 3dc3d5b3-7023-4848-9853-f5400a64e81g | DE2310010010123456788 | USD | Fremdwährungskonto | CurrentAccount | US Dolar Account | balances /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e81g/balances |

  Scenario: Read Account Details of a regular Account
    Given AISP created a consent resource with consent-id 1234-wertiq-983
    And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e80f of the required account
    AND the required account is a regular account
    When AISP requests account details
    Then the following data is delivered to the AISP
      | id | iban | currency | accountType | cashAccountType | name | links |
      | 3dc3d5b3-7023-4848-9853-f5400a64e80f | DE2310010010123456789 | EUR | Girokonto | CurrentAccount | Main Account | balances: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances, transactions: /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions |

  Scenario: Read Transaction List
    Given AISP created a consent resource with consent-id 1234-wertiq-983
    And AISP knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e80f of the required account
    When AISP requests transaction list
    Then response code 200
    And an array of booked transactions
      | transactionId | creditorName | creditorAccount | amount | bookingDate | value_Date | remittanceInformationUnstructured |
      | 1234567        | John Miles    | iban: DE43533700240123456900 | {currency: EUR, content: -256.67} | 2017-10-25 | 2017-10-26 | Example 1 |
      | 1234568        | Paul Simpsons    | iban: NL354543123456900 | {currency: EUR, content: 343.01} | 2017-10-25 | 2017-10-26 | Example 2 |
    And an array of pending transactions
      | transactionId | creditorName | creditorAccount | amount | bookingDate | value_Date | remittanceInformationUnstructured |
      | 1234569       | Claude Renault    | iban: FR33554543123456900 | {currency: EUR, content: -100.03 | 2017-10-25 | 2017-10-26 | Example 3 |
    And the link viewAccount = "/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f" is delivered to the AISP

