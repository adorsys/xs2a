Feature: Account Information Service - Embedded approach
#
#    ####################################################################################################################
#    #                                                                                                                  #
#    # Consent Requests                                                                                                 #
#    #                                                                                                                  #
#    ####################################################################################################################
    Scenario Outline: Successful consent creation with explicit start of authorisation
        Given PSU wants to create a consent <consent-resource>
        And  ASPSP-profile contains parameter signingBasketSupported is <signingBasketSupported>
        And parameter TPP-Explicit-Authorisation-Preferred is <auth-preferred>
        When PSU sends the create consent request
        Then a successful response code and the appropriate consent response data is delivered to the PSU
        And response contains link startAuthorisation
        Examples:
            | consent-resource                     | signingBasketSupported | auth-preferred |
            | consent-dedicated-successful.json    |         true           |     true       |
            | consent-all-accounts-successful.json |         true           |     true       |

    Scenario Outline: Successful consent creation with implicit start of authorisation
        Given PSU wants to create a consent <consent-resource>
        And  ASPSP-profile contains parameter signingBasketSupported is <signingBasketSupported>
        And parameter TPP-Explicit-Authorisation-Preferred is <auth-preferred>
        When PSU sends the create consent request
        Then a successful response code and the appropriate consent response data is delivered to the PSU
        And response contains link startAuthorisationWIthPsuAuthentication
        Examples:
            | consent-resource                     | signingBasketSupported | auth-preferred |
            | consent-dedicated-successful.json    |         false          |     true       |
            | consent-dedicated-successful.json    |         true           |     false      |
            | consent-dedicated-successful.json    |         false          |     false      |
            | consent-dedicated-successful.json    |         true           |     null       |
            | consent-dedicated-successful.json    |         false          |     null       |
            | consent-all-accounts-successful.json |         false          |     true       |
            | consent-all-accounts-successful.json |         true           |     false      |
            | consent-all-accounts-successful.json |         false          |     false      |
            | consent-all-accounts-successful.json |         true           |     null       |
            | consent-all-accounts-successful.json |         false          |     null       |


    Scenario Outline: Successful update of PSU data for Consent creation (embedded)
        Given PSU wants to create a consent <consent-resource>
        And PSU sends the create consent request
        And PSU sends the start consent authorisation request and receives the authorisationId
        And PSU wants to update the resource with his consent identification data <identification-data>
        When PSU sends the update consent identification data request
        Then a successful response code and the appropriate link is delivered to the PSU
        Examples:
            |consent-resource                   | identification-data                            |
            |consent-dedicated-successful.json  | updateIdentificationMultipleSca-successful.json|
            |consent-dedicated-successful.json  | updateIdentificationOneSca-successful.json     |
