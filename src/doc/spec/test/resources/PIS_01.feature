Feature: Payment Initiation Service

  Scenario: Payment Initiating Request with redirect approach
    Given PISP is logged in
    And redirect approach is used
    And wants to initiates a payment  with data
      | instructedAmount | debtorAccount | creditorName | creditorAccount | remittanceInformationUnstructured |
      | {currency: EUR, content: 123.50} | iban: DE2310010010123456789 | Merchant123 | iban: DE23100120020123456789 | Ref Number Merchant |
    When PISP sends the payment initiating request
    Then a payment resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the PISP:
      | transactionStatus | paymentId | links |
      | Received | 1234-wertiq-983    | {redirect: www.testbank.com/asdfasdfasdf, self: /v1/payments/sepa-credit-transfers/1234-wertiq-983} |

  Scenario: Payment Initiating Request with OAuth2
    Given PISP is logged in
    And OAuth2 approach is used
    And wants to initiates a payment  with data
      | instructedAmount | debtorAccount | creditor | creditorAccount | remittanceInformationUnstructured |
      | {currency: EUR, content: 123.50} | iban: DE2310010010123456789 | Merchant123 | iban: DE23100120020123456789 | Ref Number Merchant |
    When PISP sends the payment initiating request
    Then a payment resource is created at the aspsp mock
    And response code 201
    And the following data is delivered to the PISP:
      | transactionStatus | paymentId | links |
      | Received | 1234-wertiq-983    | {oAuth: “https://www.testbank.com/oauth/.well-known/oauth-authorization-server, self: /v1/payments/sepa-credit-transfers/1234-wertiq-983} |

  Scenario: Payment Status Request
    Given PISP is logged in
    And created payment status request with resource-id qwer3456tzui7890
    When PISP requests status about the payment
    Then The payment status request by a PISP should be seen in the aspsp mock
    And response code 200
    And Transaction status "AcceptedCustomerProfile" is delivered to the PISP
