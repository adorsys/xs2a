Feature: Payment Initiation Service

  Scenario: Payment Initiating Request
    Given Gateway user is logged in
    When Gateway user initiates a Payment
    Then Transaction status "Received" is delivered to the Gateway user

  Scenario: Payment Status Request Positive
    Given Gateway user is logged in
    And User has <resource-id> value
    When Gateway user send payment status with data "payment-product"=sepa-credittransfers
    And "resource-id"=qwer3456tzui7890
    Then The payment status request by a Gateway user should be seen in the aspsp mock
    And Transaction status "AcceptedCustomerProfile" is delivered to the gateway user

  Scenario: Payment Status Request Negative
    Given Gateway user is logged in
    And User has <resource-id> value
    When Gateway user send payment status with data "payment-product"=sepa-credittransfers
    And "resource-id"=qwer3456tzui7800
    Then The payment status request by a Gateway user should be seen in the aspsp mock
    And "HTTP CODE 404" is delivered to the Gateway user
