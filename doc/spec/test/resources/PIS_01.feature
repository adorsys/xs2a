Feature: Payment Initiation Service

  Scenario: Payment initiation request for single payments
    Given PISP is logged in
    And redirect approach is used
    And PISP wants to initiate a payment with data
      | instructedAmount | debtorAccount | creditorName | creditorAccount | remittanceInformationUnstructured |
      | {currency: EUR, content: 123.50} | iban: DE2310010010123456789 | Merchant123 | iban: DE23100120020123456789 | Ref Number Merchant |
    When PISP sends the payment initiating request
    Then a payment resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the PISP:
      | transactionStatus | paymentId | links |
      | Received | 1234-wertiq-983    | {redirect: www.testbank.com/asdfasdfasdf, self: /v1/payments/sepa-credit-transfers/1234-wertiq-983} |

  Scenario: Payment initiation request for bulk payments
    Given PISP is logged in
    And redirect approach is used
    And PISP wants to initiate multiple payments with data
      | instructedAmount | debtorAccount | creditorName | creditorAccount | remittanceInformationUnstructured |
      | currency: EUR, content: 123.50 | iban: DE2310010010123456789 | Merchant123 | iban: DE23100120020123456777 | Ref Number Merchant |
      | currency: EUR, content: 500 | iban: DE2310010010123456789 | Merchant111 | iban: DE23100120020123456888 | Ref Number Merchant |
    When PISP sends the payment initiating request
    Then a payment resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the PISP:
      | transactionStatus | paymentId | links |
      | Received | 1234-wertiq-988    | {redirect: www.testbank.com/asdfasdfasdf, self: /v1/payments/sepa-credit-transfers/1234-wertiq-988} |

  Scenario: Payment initiation request for standing orders
    Given PISP is logged in
    And redirect approach is used
    And PISP wants to initiate a standing order with data
      | instructedAmount | debtorAccount | creditorName | creditorAccount | remittanceInformationUnstructured | startDate | executionRule | frequency | dayOfExecution
      | currency: EUR, content: 123.50 | iban: DE2310010010123456789 | Merchant123 | iban: DE23100120020123456789 | Ref Number Abonnement | 2018-03-01 | latest | monthly | 01 |
    Then a payment resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the PISP:
      | transactionStatus | paymentId | links |
      | Received | 1234-wertiq-999    | {redirect: www.testbank.com/asdfasdfasdf, self: /v1/periodic-payments/sepa-credit-transfers/1234-wertiq-999} |

  Scenario: Payment Status Request
    Given PISP is logged in
    And created payment status request with resource-id qwer3456tzui7890
    When PISP requests status about the payment
    Then The payment status request by a PISP should be seen in the aspsp mock
    And response code 200
    And Transaction status "AcceptedCustomerProfile" is delivered to the PISP
