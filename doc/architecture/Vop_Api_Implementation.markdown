# Vop API Implementation Plan

### Update ASPSP Profile Parameter

Update the ASPSP profile to support the Vop API implementation by adding the following parameters:

- `X-Reference-API-Name`
- `X-Reference-API-Document`
- `X-Reference-API-Version`
e.g: aspsp-profile/aspsp-profile-lib/src/main/resources/bank_profile.yml:44-47
```
 piis:
    piisConsentSupported: NOT_SUPPORTED
    redirectLinkToOnlineBanking:
      piisRedirectUrlToAspsp: http://localhost:4400/account-information/login?encryptedConsentId={encrypted-consent-id}&redirectId={redirect-id}
```
### Create Settings Class
e.g: 
aspsp-profile/aspsp-profile-lib/src/main/java/de/adorsys/psd2/aspsp/profile/domain/piis/PiisRedirectLinkBankSetting.java:28-28
```
public class PiisRedirectLinkBankSetting {
```
### Create the VOP package and class
e.g: 
aspsp-profile/aspsp-profile-api/src/main/java/de/adorsys/psd2/aspsp/profile/domain/piis/PiisAspspProfileSetting.java:25-25
```
public class PiisAspspProfileSetting 
```
### Update the service.
e.g:
aspsp-profile/aspsp-profile-lib/src/main/java/de/adorsys/psd2/aspsp/profile/service/AspspProfileServiceImpl.java:45-45
```
public class AspspProfileServiceImpl implements AspspProfileService
```

### Reference Documentation

`aspsp-profile/README.adoc`

### Refactor AspspProfileWrappers Class
aspsp-profile/aspsp-profile-lib/src/main/java/de/adorsys/psd2/aspsp/profile/service/AspspProfileServiceImpl.java:45-45
```
public class AspspProfileServiceImpl implements AspspProfileService
```

Refactor the `AspspProfileWrappers` class in the XS2A module to accommodate the new parameters added to the ASPSP profile.

Update the class structure and logic to handle `X-Reference-API-Name`, `X-Reference-API-Document`, and `X-Reference-API-Version`.

## Analyse Sequence Diagrams for Dedicated Vop Endpoint

Analyse sequence diagrams for the dedicated Vop endpoint to illustrate the use case workflows.

### Sequence Diagrams

- `SubmitsVerificationOfParty`: `doc/usecases/diagrams/SubmitsVerificationOfParty.puml`
- `SubmitsBulkOfRelatedPartyVerification`: `doc/usecases/diagrams/SubmitsBulkOfRelatedPartyVerification.puml`


## Build Vop Postman Collection for Functional Testing

Develop a Postman collection for functional testing of the Vop API.

### Reference

Use the existing Postman collection in the codebase: `scripts/tests/postman/XS2A requests(standart configurations).postman_collection.json`

Create a new Postman collection tailored for Vop API endpoints, incorporating relevant requests and configurations for testing.

## Build Project Structure for Vop API Implementation

Define the project structure for the Vop API implementation.

Use an LLM to generate documentation describing the project structure based on the existing codebase.

## Implement Validation for Vop Endpoint

Implement validation for the path, headers, and body of the Vop endpoint requests.

Reference what have been done in the following classes below to implement the VOP validation.

@/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/filter/Xs2aLoggingContextFilter.java 
@/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/interceptor/logging/PaymentLoggingInterceptor.java 
@/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/interceptor/tpp/TppStopListInterceptor.java 
@/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/interceptor/validator/PaymentParametersValidationInterceptor.java 

Use an LLM to extract and produce validation documentation from the existing codebase for the Vop endpoint.

## Implement Vop Dedicated Endpoints

Implement the following dedicated Vop endpoints, dividing each into subtasks and leveraging existing codebase implementations with LLM assistance.

### 1. Submits a Verification of Party Endpoint

##### Endpoint

`/v1/{party-verification-endpoint}`

##### Subtasks

- Following the implemtation that has been done on the payment controller @/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java, through the SPI payment @/spi-api/src/main/java/de/adorsys/psd2/xs2a/spi/service/SpiPayment.java, and do the same for the VOP endpoint implementation with to the `/v1/{party-verification-endpoint}` .

Use LLM to generate implementation based on existing codebase.

### 2. Submits a Bulk of Related Party Verification Endpoint

##### Endpoint

`/v1/{bulk-party-verification-endpoint}`
e.g: xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java:161-161
```
    public ResponseEntity<PaymentInitiationRequestResponse201> initiatePayment
```

##### Subtasks

- Following the implemtation that has been done on the payment controller @/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java, through the SPI payment @/spi-api/src/main/java/de/adorsys/psd2/xs2a/spi/service/SpiPayment.java, and do the same for the VOP endpoint implementation with respect `/v1/{bulk-party-verification-endpoint}`.

Use LLM to generate implementation based on existing codebase.

### 3. Retrieves Detailed Status of a Bulk of Related Party Verification Endpoint

##### Endpoint

`/v1/{bulk-party-verification-endpoint}/{vopBulkId}`
e.g: xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java:105-105
```
 public ResponseEntity getPaymentInformation
```

##### Subtasks

- Following the implemtation that has been done on the payment controller @/xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java, through the SPI payment @/spi-api/src/main/java/de/adorsys/psd2/xs2a/spi/service/SpiPayment.java, and do the same for the VOP endpoint implementation with respect `/v1/{bulk-party-verification-endpoint}/{vopBulkId}`

Use LLM to generate implementation based on existing codebase.

### 4. Retrieves Detailed Status of a Bulk of Related Party Verification (Status-Specific) Endpoint

##### Endpoint

`/v1/{bulk-party-verification-endpoint}/{vopBulkId}/status`

e.g: xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java:82-82
```
    public ResponseEntity getPaymentInitiationStatus
```

##### Subtasks

- Define endpoint routing and request handling.
- Implement validation for `vopBulkId` and status-specific parameters.
- Retrieve and format status-specific data.
- Return appropriate responses.

Use LLM to generate implementation based on existing codebase.

### Implement YAML File Specification Update

Update the YAML file specification from version 1.3.12 to 1.3.14.

| **1.3.13** | **Errata on Version 1.3.12.** Minor technical alignment and clarifications throughout the document, including a clarification for the `bookingStatus` values in the transaction list endpoint. | 2023-03-24 openFinance Taskforce |
| **1.3.14** | **Errata on Version 1.3.13.** Integration of clarifications from EBA on certain technical elements, notably regarding AIS consent models. Inclusion of clarifications on status codes for multi-authorisation payments. | 2024-03-04 openFinance Taskforce |
| **1.3.15** | **Errata on Version 1.3.14.** Inclusion of changes necessary for alignment with the Instant Payment Regulation (IPR) and further clarifications on the application of the `lastActionDate` in payment initiation. | 20 June 2025 |
