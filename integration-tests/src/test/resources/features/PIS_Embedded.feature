Feature: Payment Initiation Embedded approach

    Scenario Outline: Successful payment initiation request (embedded)
        Given PSU wants to initiate a payment using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the payment initiating request
        Then a successful response code and the appropriate authentication URL is delivered to the PSU
        Examples:
            | payment-service | payment-product       | single-payment                |
            | payments        | sepa-credit-transfers | singlePayInit-successful.json |
