Feature: Confirmation of Funds Service

  Scenario: Confirmation of Funds Request
    Given PSU initiated card based payment transaction at a PSU – TPP interface
    And TPP requests a confirmation on the availability of funds for the related account of the PSU wants to create a consent resource with data
      | cardNumber | psuAccount | instructedAmount |
      | 12345678901234 | iban: DE2310010010123456787 | currency: EUR, content: 123.00 |
    When TPP sends the confirmation of funds request
    Then response code 200
    And TPP Message Information {fundsAvailable: "true"} is delivered to TPP
