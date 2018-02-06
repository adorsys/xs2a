Feature: Strong Customer Authentication

  Scenario: Payment initiation transaction with SCA based on the redirect approach
    Given PSU is logged in
    And wants to initiates a payment  with data
      | instructed_amount | debtor_account | creditor | creditor_account | remittance_information_unstructured |
      | {currency: EUR, amount: 123} | iban: DE2310010010123456789 | name: Merchant123 | iban: DE23100120020123456789 | Ref Number Merchant- 123456 |
    When PISP sends the payment initiating request
    Then ASPSP decides about SCA "yes" and approach "Redirect approach" to be used
    And response code 201
    And the following data is delivered to the PSU:
      | transaction_status | links |
      | Received | {redirect: www.testbank.com/asdfasdfasdf, self: /v1/payments/sepa-credit-transfers/1234-wertiq-983} |