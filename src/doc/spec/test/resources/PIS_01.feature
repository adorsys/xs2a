Feature: Payment Initiation Service

  Scenario: Payment Initiating Request
    Given PSU is logged in
    And wants to initiates a payment  with data
      | instructed_amount | debtor_account | creditor | creditor_account | remittance_information_unstructured |
      | {currency: EUR, amount: 123} | iban: DE2310010010123456789 | name: Merchant123 | iban: DE23100120020123456789 | Ref Number Merchant- 123456 |
    When PSU sends the payment initiating request
    Then a payment resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the PSU:
      | transaction_status | links |
      | Received | {redirect: www.testbank.com/asdfasdfasdf, self: /v1/payments/sepa-credit-transfers/1234-wertiq-983} |

  Scenario: Payment Status Request
    Given PSU is logged in
    And created payment status request with resource-id qwer3456tzui7890
    When PSU requests status about the payment
    Then The payment status request by a PSU should be seen in the aspsp mock
    And response code 200
    And Transaction status "AcceptedCustomerProfile" is delivered to the PSU
