Feature: Payment Initiation Embedded approach

    Scenario Outline: Successful payment initiation request (embedded)
        Given PSU wants to initiate a payment using the payment service <payment-service> and the payment product <payment-product>
        When PSU sends the payment initiating request
        Then a successful response code and the appropriate authentication URL is delivered to the PSU
        Examples:
            | payment-service | payment-product       | single-payment                |
            | payments        | sepa-credit-transfers | singlePayInit-successful.json |

    Scenario Outline: Successful Authorisation with PSU Authentication (embedded)
        Given PSU initiated a payment <payment-service> with the payment-id <payment-id>
        And PSU needs to authorize and identify using <authorisation-data> and the <authorisation-id>
        When PSU sends the authorisation request
        Then a successful response code and the appropriate authorization data are received
        Examples:
            | payment-service |	payment-id	                         | authorisation-data               | authorisation-id |
            | payments	      | a9115f14-4f72-4e4e-8798-202808e85238 | authWithPsuIdent-successful.json | a9115f14-4f72-4e4e-8798-202808e85238 |
