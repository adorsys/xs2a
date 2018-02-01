Feature: Account Information Service

  Scenario: Create consent request
    Given gateway user is logged in
    And wants to create a consent resource with data
      | access_accounts | recurring_indicator | valid_until | frequency_per_day |
      | {iban: DE2310010010123456789, access: [balance, transactions]}, {iban: DE2310010010123456788, access: [balance]} | true | 2017-11-01 | 4 |
    When gateway user sends the create consent request
    Then a consent resource is created at the aspsp mock
    And response code 200
    And the following data is delivered to the gateway user:
      | transaction_status | links |
      | Received | /v1/consents/qwer3456tzui7890 |

  Scenario: Get Status Request
    Given Gateway user is logged in
    And created a consent resource with consent-id qwer3456tzui7890
    When Gateway user requests consent status
    Then Transaction status "AcceptedTechnicalValidation" is delivered to the gateway user

  Scenario: Get Consent Request
    Given gateway user is logged in
    And created a consent resource with consent-id qwer3456tzui7890
    When gateway user requests information about the consent resource
    Then  the following data is delivered to the gateway user:
      | access_accounts | recurring_indicator | valid_until | frequency_per_day | transaction_status | consent_status |
      | {iban: DE2310010010123456789, access: [balance, transactions]}, {iban: DE2310010010123456788, access: [balance]} | true | 2017-11-01 | 4 | AcceptedTechnicalValidation | valid |

  Scenario: Delete Consent Request
    Given gateway user is logged in
    And created a consent resource with consent-id qwer3456tzui7890
    When gateway user sends delete consent request
    Then the consent resource is deleted at the aspsp mock
    And response code 204 is delivered to the gateway user

  Scenario: Read Account List
    Given gateway user is logged in
    And created a consent resource with consent-id qwer3456tzui7890
    When gateway user requests the list of accounts
    Then the following data is delivered to the gateway user
      | id | iban | account_type | currency | links |
      | 3dc3d5b3-7023-4848-9853-f5400a64e80f | DE2310010010123456789 | Main Account | EUR | /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances, /v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions |
      | 3dc3d5b3-7023-4848-9853-f5400a64e81g | DE2310010010123456788 | US Dollar Account | USD | “/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e81g/balances                                                           |

  Scenario: Read Balance
    Given Gateway user is logged in
    And created a consent resource with consent-id qwer3456tzui7890
    And Gateway user knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e80f of the required account
    When Gateway user requests the balance
    Then a list of balances with data
      | closed_booked | expected |
      | amount {EUR, 500.00}, date 2017-10-25 | amount {900.00}, last_action_date_time 2017-10-25T15:30:35.035Z |

  Scenario: Read Transaction List
    Given Gateway user is logged in
    And created a consent resource with consent-id qwer3456tzui7890
    And Gateway user knows the account-id 3dc3d5b3-7023-4848-9853-f5400a64e80f of the required account
    When Gateway user requests transaction list
    Then response code 200
    And the following data
      | transaction_id | creditor_name | creditor_account | amount | booking_date | value_date | remittance_information_unstructured |
      | 1234567        | John Miles    | DE43533700240123456900 | EUR, -256,67 | 2017-10-25 | 2017-10-26 | Example for Remittance Information |
      | 1234568        | Paul Simpsons    | NL354543123456900 | EUR, 343.01 | 2017-10-25 | 2017-10-26 | Example for Remittance Information |
    And "account_link"=/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f is delivered to the gateway user

