@startuml
actor PSU
entity TPP
entity XS2A
entity SPI
entity SPI
entity CMS_PSU_API
entity Online_banking
entity Core_banking
PSU->TPP: Initiate payment
activate TPP
activate PSU
TPP -> XS2A: REST Call Initiate payment\nPOST "/v1/payments/{payment-product}"
activate XS2A
XS2A->SPI: Java Call SPIinitiatePayment
activate SPI
|||
SPI-->XS2A: SpiSinglePaymentInitiationResponse
deactivate SPI
|||
XS2A-->TPP:Payment initiation response
deactivate XS2A
|||
TPP->XS2A: REST Call Initiate authorithation implicitly \nPOST"/v1/{payment-service }/{payment-product}/\n{paymentId}/authorisations"
activate XS2A
note left
         in case of Multilevel SCA
         the authorisation steps
         should be repeated for other PSU
end note
|||
XS2A-->TPP:Start authorisation response\n"link_SCARedirect "
deactivate XS2A
TPP-->PSU: Feedback to the customer\nAuthorise the transaction
deactivate TPP
|||
PSU->Online_banking: PSU re-direct to an authorisation webpage of the ASPSP
deactivate PSU
activate Online_banking
|||
Online_banking->CMS_PSU_API: REST Call GetPaymentByRedirectId\n"GET/psu-api/v1/payment/redirect/{redirect-id}"
activate CMS_PSU_API
|||
CMS_PSU_API-->Online_banking: CmsPaymentResponse
|||
Online_banking->CMS_PSU_API: REST Call UpdatePsuIdPayment\n"PUT/psu-api/v1/payment/authorithation/\n{authorisation-id}/psu-data"
|||
CMS_PSU_API-->Online_banking: Response "http Codes: 200 successful or 400 error"
deactivate CMS_PSU_API
|||
Online_banking-->PSU: Authorisation Process Response
deactivate Online_banking
activate PSU
|||
PSU->Online_banking: The PSU Authorises the Payment
activate Online_banking
|||
Online_banking->CMS_PSU_API: REST Call UpdateAuthorisationStatus\n"PUT/psu-api/v1/payment/{payment-id}/authorisation/\n{authorisation-id}/status/{status}"
activate CMS_PSU_API
|||
CMS_PSU_API-->Online_banking: Response "http Codes: 200 successful or 400 error"
deactivate CMS_PSU_API
Online_banking->Core_banking: Execute Payment
activate Core_banking
|||
Core_banking-->Online_banking: Response PaymentStatus
deactivate Core_banking
Online_banking->CMS_PSU_API: REST Call UpdatePaymentStatus\n"PUT/psu-api/v1/payment/{payment-id}/status/{status}"
activate CMS_PSU_API
|||
CMS_PSU_API-->Online_banking: Response "http Codes: 200 successful or 400 error"
deactivate CMS_PSU_API
|||
Online_banking-->PSU: Payment confirmed
deactivate Online_banking
|||
PSU->TPP: Redirect back to TPP
deactivate PSU
activate TPP
|||
TPP->XS2A: REST Call get payment status\n"GET/v1/{payment-service}/{paymentId}/status"
activate XS2A
|||
XS2A->CMS_PSU_API: REST Call GetPaymentById 
activate CMS_PSU_API
|||
CMS_PSU_API-->XS2A: Payment Object Response
deactivate CMS_PSU_API
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
XS2A->CMS_PSU_API: REST Call UpdateTransactionStatus
activate CMS_PSU_API
|||
CMS_PSU_API-->XS2A: UpdateTransactionStatus Response
deactivate CMS_PSU_API
|||
XS2A-->TPP: Transaction Status Response
deactivate XS2A
|||
TPP->XS2A: REST Call Get Payment Request "GET/v1/\n{payment-service}/{paymentId}"
deactivate TPP
activate XS2A
XS2A->CMS_PSU_API: REST Call GetPaymentById 
activate CMS_PSU_API
|||
CMS_PSU_API-->XS2A: Payment Object Response
deactivate CMS_PSU_API
|||
XS2A->SPI: Java Call SPIGetPaymentbyId
activate SPI
|||
SPI->Core_banking: GetPaymentById
activate Core_banking
|||
Core_banking-->SPI: Payment Object Response
deactivate Core_banking
SPI-->XS2A: SpiSinglrPayment /\nSpiPeriodicPayment /\nSpiBulkPayment /\nSpiPaymentInfo
deactivate SPI
|||
XS2A->CMS_PSU_API: REST Call UpdatePayment
activate CMS_PSU_API
|||
CMS_PSU_API-->XS2A: UpdatePayment Response
deactivate CMS_PSU_API
|||
XS2A-->TPP: Get Payment Response
deactivate XS2A
activate TPP
|||
TPP->XS2A: REST Call Get Authorisation Sub-resources\n"GET/v1/{payment-service}/{payment-product}\n/{paymentId/authorisations}"
activate XS2A
|||
XS2A->CMS_PSU_API: GetAuthorisationsByPaymentId
activate CMS_PSU_API
|||
CMS_PSU_API-->XS2A: GetAuthorisationsByPaymentId Response
deactivate CMS_PSU_API
XS2A-->TPP: Authorisation Ids List
|||
TPP->XS2A: REST Call Get Authorisation\n"GET/v1/{payment-service}/{payment-product}/\n{paymentId/authorisations}/{authorithationId}"
XS2A->CMS_PSU_API: GetAuthorisationScaStatus
activate CMS_PSU_API
|||
CMS_PSU_API-->XS2A: GetAuthorisationScaStatus Response
deactivate CMS_PSU_API
XS2A-->TPP: Sca Status
deactivate XS2A
deactivate TPP
@enduml

