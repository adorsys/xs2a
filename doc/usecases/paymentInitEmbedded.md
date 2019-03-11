@startuml
actor PSU
entity TPP
entity XS2A
entity CMS
entity SPI
entity Core_banking
PSU->TPP: Initiate payment
activate PSU
activate TPP
TPP -> XS2A: REST Call Initiate payment\nPOST "/v1/payments/{payment-product}"
activate XS2A
XS2A->SPI: Java Call SPIinitiatePayment
activate SPI
|||
SPI-->XS2A: SpiSinglePaymentInitiationResponse
deactivate SPI
|||
XS2A-->TPP:Payment initiation response
|||
TPP->XS2A: REST Call Initiate authorithation\nPOST"/v1/{payment-service }/{payment-product}/\n{paymentId}/authorisations"
note left
         in case of Multilevel SCA
         the authorisation steps
         should be repeated for other PSU
end note
|||
XS2A->CMS: Create authorisation sub-resource
XS2A-->TPP:Start authorisation response\n"link_StartAuthorisationWithPsuIdentification"
deactivate XS2A
|||
TPP-->PSU: Feedback to the customer\n"Please enter your User-id & password"
|||
PSU->TPP: PSU provide User-id & password
deactivate PSU
|||
TPP->XS2A: REST Call Update PSU data (Identification)\n"PUT/v1/{payment-service}/{payment-id}/\n{payment-product}/authorisations/{authorisationId}"
activate XS2A
|||
XS2A->CMS: REST Call UpdatePisAuthorisation
activate CMS
|||
CMS-->XS2A: UpdatePisAuthorisation Response
deactivate CMS
|||
XS2A-->TPP: UpdatePsuAuthenticationResponse
|||
TPP->XS2A: REST Call Update PSU data (Authentication)\n"PUT/v1/{payment-service}/{payment-id}/\n{payment-product}/authorisations/{authorisationId}"
|||
XS2A->SPI: Java Call PaymentAuthorisationSpi
activate SPI
|||
SPI->Core_banking: AuthorisedPsu
activate Core_banking
|||
Core_banking-->SPI: AuthorisedPsu Response
deactivate Core_banking
|||
SPI-->XS2A: SpiAuthorisationStatus
|||
XS2A->SPI: Java Call PaymentAuthorisationSpi
|||
SPI-->XS2A: List "SpiAuthenticationObject"
deactivate SPI
|||
XS2A->CMS: REST Call UpdatePisAuthorisation
activate CMS
|||
CMS-->XS2A: UpdatePisAuthorisation Response
deactivate CMS
|||
XS2A->TPP: UpdatePsuAuthenticationResponse
deactivate XS2A
|||
TPP-->PSU: Feedback to the customer\n"Please select SCA method"
activate PSU
|||
PSU->TPP: SCA Method
|||
TPP->XS2A: REST Call Update Psu data(Select Authentication Method)\n"PUT/v1/{payment-service}/{payment-id}/\n{payment-product}/authorisations/{authorisationId}"
activate XS2A
|||
XS2A->SPI: Java Call PaymentAuthorisationSpi
activate SPI
|||
SPI->Core_banking: SelectScaMethod
activate Core_banking
|||
Core_banking-->PSU: ScaAuthenticationData
|||
Core_banking-->SPI: SelectScaMethod Respons
deactivate Core_banking
|||
SPI-->XS2A: SpiAuthorisationCodeResult
deactivate SPI
|||
XS2A->CMS: REST Call UpdatePisAuthorisation
activate CMS
|||
CMS-->XS2A: UpdatePisAuthorisation Response
deactivate CMS
|||
XS2A-->TPP:UpdatePsuAuthenticationResponse
deactivate XS2A
|||
PSU->TPP: Provide Authentication Data
|||
TPP->XS2A: REST Call Transaction Authorisation\n"PUT/v1/{payment-service}/{payment-id}/\n{payment-product}/authorisations/{authorisationId}"
deactivate TPP
activate XS2A
|||
XS2A->SPI: Java Call SinglePaymentSpi
activate SPI
|||
SPI-->Core_banking: Authorised Payment
activate Core_banking
|||
Core_banking-->SPI: Authorised Payment Response 
deactivate Core_banking
|||
SPI-->XS2A: SpiPaymentExecution Response
deactivate SPI
|||
XS2A->CMS: REST Call UpdatePisAuthorisation
activate CMS
|||
CMS-->XS2A: UpdatePisAuthorisation Response
deactivate CMS
|||
XS2A-->TPP: Transaction Authorisation Response
deactivate XS2A
activate TPP
|||
TPP-->PSU: Feedback to the customer:\n"Payment Authorised"
deactivate PSU
|||
TPP->XS2A: REST Call get payment status\n"GET/v1/{payment-service}/{paymentId}/status"
activate XS2A
XS2A->CMS: REST Call GetPaymentById 
activate CMS
|||
CMS-->XS2A: Payment Object Response
deactivate CMS
|||
XS2A->SPI: Java Call SPI GetPaymentStatusByid
activate SPI
|||
SPI->Core_banking: GetPaymentStatusById
activate Core_banking
|||
Core_banking-->SPI: Transaction Status Response
deactivate Core_banking
|||
SPI-->XS2A: Transaction Status Response
deactivate SPI
|||
XS2A->CMS: REST Call UpdateTransactionStatus
activate CMS
|||
CMS-->XS2A: UpdateTransactionStatus Response
deactivate CMS
|||
XS2A-->TPP: Transaction Status Response
|||
TPP->XS2A: REST Call Get Payment Request\n"GET/v1/{payment-service}/{paymentId}"
|||
XS2A->CMS: REST Call GetPaymentById 
activate CMS
|||
CMS-->XS2A: Payment Object Response
deactivate CMS
|||
XS2A->SPI: Java Call SpiGetPaymentbyId
activate SPI
|||
SPI->Core_banking: GetPaymentById
activate Core_banking
|||
Core_banking-->SPI: Payment Object Response
deactivate Core_banking
|||
SPI-->XS2A: SpiSinglePayment /\nSpiPeriodicPayment /\nSpiBulkPayment /\nSpiPaymentInfo            Response
deactivate SPI
|||
XS2A->CMS: REST Call UpdatePayment
activate CMS
|||
CMS-->XS2A: UpdatePayment Response
deactivate CMS
|||
XS2A-->TPP: GetPayment Response
|||
TPP->XS2A: REST Call Get Authorisation Sub-resources\n"GET/v1/{payment-service}/{payment-product}\n/{paymentId/authorisations}"
|||
XS2A->CMS: GetAuthorisationsByPaymentId
activate CMS
|||
CMS-->XS2A: GetAuthorisationsByPaymentId Response
deactivate CMS
XS2A-->TPP: AuthorisationByPaymentIdsList Response
|||
TPP->XS2A: REST Call Get Authorisation\n"GET/v1/{payment-service}/{payment-product}/\n{paymentId/authorisations}/{authorithationId}"
XS2A->CMS: GetAuthorisationScaStatus
activate CMS
|||
CMS-->XS2A: GetAuthorisationScaStatus Response
deactivate CMS
|||
XS2A-->TPP: GetAuthorithationScaStatus Response
deactivate TPP
deactivate XS2A
@enduml
