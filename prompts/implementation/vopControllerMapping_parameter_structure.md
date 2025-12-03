# VOP Controller Mapping Implementation - Task 2: Parameter Structure Classes

## Overview
This document details the implementation of parameter structure classes for the VOP Controller, following the patterns established in PaymentController.

## Completed Tasks

### Task 2.1: Created VopRequestParameters Class
- **File**: `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/domain/vop/VopRequestParameters.java`
- **Purpose**: Master container for all VOP API request parameters, similar to `PaymentInitiationParameters`
- **Structure**:
  - `endpoint`: String - The API endpoint being called
  - `xRequestId`: UUID - Request identifier
  - `signingMsg`: SigningMsg - Signature-related parameters
  - `signingBody`: SigningBody - Body signing/encryption parameters
  - `otherParams`: VopOtherParams - Additional parameters
  - `instanceId`: String - Instance identifier (nullable)

### Task 2.2: Created SigningMsg DTO
- **File**: `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/domain/vop/SigningMsg.java`
- **Purpose**: Encapsulates signature-related parameters
- **Fields**:
  - `digest`: String (nullable) - Message digest for signature validation
  - `xJwsSignature`: String (nullable) - JWS signature header

### Task 2.3: Created SigningBody DTO
- **File**: `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/domain/vop/SigningBody.java`
- **Purpose**: Encapsulates body signing and encryption parameters
- **Fields**:
  - `bodySigProfile`: String (nullable) - Body signature profile
  - `bodyEncProfile`: String (nullable) - Body encryption profile
  - `bodyEncList`: String (nullable) - Body encryption list

### Task 2.4: Created VopOtherParams DTO
- **File**: `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/domain/vop/VopOtherParams.java`
- **Purpose**: Encapsulates remaining API parameters
- **Fields**:
  - `acceptLanguage`: String (nullable) - Client language preference
  - `apiContractId`: UUID (nullable) - API contract identifier
  - `clientAsynchronousProcessPreferred`: Boolean (nullable) - Async processing preference

### Task 2.5: Created VopModelMapperPsd2
- **File**: `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/mapper/VopModelMapperPsd2.java`
- **Purpose**: Maps API request parameters to domain objects, following PaymentModelMapperPsd2 patterns
- **Methods**:
  - `mapToSigningMsg()`: Maps digest and xJwsSignature to SigningMsg
  - `mapToSigningBody()`: Maps body signing/encryption parameters to SigningBody
  - `mapToVopOtherParams()`: Maps remaining parameters to VopOtherParams
  - `mapToVopRequestParameters()`: Master method combining all mappings

### Task 2.6: Updated VopController
- **File**: `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/VopController.java`
- **Changes**:
  - Added `VopModelMapperPsd2` dependency injection
  - Updated all four API methods to use parameter mapping:
    - `verificationOfParty()`
    - `bulkVerificationOfParty()`
    - `getBulkVerificationOfPartyStatus()`
    - `getBulkVerificationOfParty()`

## Implementation Details

### Pattern Consistency with PaymentController
The implementation follows the established patterns from PaymentController:

1. **Parameter Grouping**: Similar to how PaymentController groups parameters into `TppAttributes`, `PsuIdData`, etc., VOP parameters are grouped into `SigningMsg`, `SigningBody`, and `VopOtherParams`.

2. **Mapper Pattern**: Uses the same mapper pattern as `PaymentModelMapperPsd2.mapToPaymentRequestParameters()`.

3. **Optional Parameter Handling**: Properly handles `Optional<T>` parameters by extracting values or defaulting to null.

4. **Dependency Injection**: Follows the same constructor injection pattern as PaymentController.

### Parameter Categorization Logic

#### SigningMsg Parameters
- **digest**: Used for message integrity verification
- **xJwsSignature**: Contains the JWS signature for authentication

#### SigningBody Parameters
- **bodySigProfile**: Specifies the signature profile for request body
- **bodyEncProfile**: Specifies the encryption profile for request body
- **bodyEncList**: Contains encryption-related list data

#### VopOtherParams Parameters
- **acceptLanguage**: Client language preference for responses
- **apiContractId**: Contract identifier for API usage tracking
- **clientAsynchronousProcessPreferred**: Indicates client's preference for async processing

## Current Limitations and Future Tasks

### TODO Items
1. **Service Layer Integration**: The VopService currently doesn't accept VopRequestParameters. This will need to be updated in future iterations.

2. **Parameter Usage**: The mapped parameters are currently created but not fully utilized in the service layer. Integration with business logic is pending.

3. **Validation**: Additional validation logic for the structured parameters may be needed.

## Code Quality
- All classes follow project coding standards
- Proper use of Lombok annotations for boilerplate reduction
- Nullable annotations for optional fields
- Comprehensive JavaDoc documentation
- Consistent naming conventions with existing codebase

## Next Steps
1. Update VopService to accept VopRequestParameters
2. Implement parameter validation logic
3. Integrate mapped parameters with business logic
4. Add unit tests for the new mapper classes
