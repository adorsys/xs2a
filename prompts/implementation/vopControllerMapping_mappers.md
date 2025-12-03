# VOP Controller Mapping Implementation - Task 3: Parameter Mappers

## Overview
This document details the implementation of VOP parameter mappers, specifically focusing on the mapping logic that transforms API request parameters into structured domain objects.

## Completed Implementation

### Task 3.1: SigningMsg Mapper
**Method**: `mapToSigningMsg(Optional<String> digest, Optional<String> xJwsSignature)`

```java
public SigningMsg mapToSigningMsg(Optional<String> digest, Optional<String> xJwsSignature) {
    SigningMsg signingMsg = new SigningMsg();
    signingMsg.setDigest(digest.orElse(null));
    signingMsg.setXJwsSignature(xJwsSignature.orElse(null));
    return signingMsg;
}
```

**Implementation Details**:
- Extracts signature-related parameters from API request
- Handles Optional<String> types by extracting values or defaulting to null
- Creates structured SigningMsg object for downstream processing
- Follows the same pattern as PaymentController's parameter extraction

### Task 3.2: SigningBody Mapper
**Method**: `mapToSigningBody(Optional<String> bodySigProfile, Optional<String> bodyEncProfile, Optional<String> bodyEncList)`

```java
public SigningBody mapToSigningBody(Optional<String> bodySigProfile,
                                   Optional<String> bodyEncProfile,
                                   Optional<String> bodyEncList) {
    SigningBody signingBody = new SigningBody();
    signingBody.setBodySigProfile(bodySigProfile.orElse(null));
    signingBody.setBodyEncProfile(bodyEncProfile.orElse(null));
    signingBody.setBodyEncList(bodyEncList.orElse(null));
    return signingBody;
}
```

**Implementation Details**:
- Groups body signing and encryption parameters
- Maintains separation of concerns between signature verification and body processing
- Allows for future extension of body-related security features

### Task 3.3: VopOtherParams Mapper
**Method**: `mapToVopOtherParams(Optional<String> acceptLanguage, Optional<UUID> apiContractId, Optional<Boolean> clientAsynchronousProcessPreferred)`

```java
public VopOtherParams mapToVopOtherParams(Optional<String> acceptLanguage,
                                         Optional<UUID> apiContractId,
                                         Optional<Boolean> clientAsynchronousProcessPreferred) {
    VopOtherParams otherParams = new VopOtherParams();
    otherParams.setAcceptLanguage(acceptLanguage.orElse(null));
    otherParams.setApiContractId(apiContractId.orElse(null));
    otherParams.setClientAsynchronousProcessPreferred(clientAsynchronousProcessPreferred.orElse(null));
    return otherParams;
}
```

**Implementation Details**:
- Handles miscellaneous parameters that don't fit security categories
- Supports different parameter types (String, UUID, Boolean)
- Provides flexibility for future parameter additions

### Task 3.4: Master Mapping Method
**Method**: `mapToVopRequestParameters(String endpoint, UUID xRequestId, SigningMsg signingMsg, SigningBody signingBody, VopOtherParams otherParams)`

```java
public VopRequestParameters mapToVopRequestParameters(String endpoint,
                                                    UUID xRequestId,
                                                    SigningMsg signingMsg,
                                                    SigningBody signingBody,
                                                    VopOtherParams otherParams) {
    VopRequestParameters parameters = new VopRequestParameters();
    parameters.setEndpoint(endpoint);
    parameters.setXRequestId(xRequestId);
    parameters.setSigningMsg(signingMsg);
    parameters.setSigningBody(signingBody);
    parameters.setOtherParams(otherParams);
    parameters.setInstanceId(requestProviderService.getInstanceId());
    return parameters;
}
```

**Implementation Details**:
- Combines all parameter categories into a single VopRequestParameters object
- Uses RequestProviderService to get instanceId (following PaymentController pattern)
- Provides a clean interface for controller methods to use

## Parameter Mapping Strategy

### Design Decisions

1. **Separation of Concerns**: Parameters are grouped by their functional purpose:
   - **Security/Signing**: `digest`, `xJwsSignature`
   - **Body Processing**: `bodySigProfile`, `bodyEncProfile`, `bodyEncList`
   - **General/Other**: `acceptLanguage`, `apiContractId`, `clientAsynchronousProcessPreferred`

2. **Optional Parameter Handling**:
   - All API parameters are Optional<T> types from the generated API
   - Mappers extract values using `.orElse(null)` for consistent handling
   - Null values are preserved to maintain API contract semantics

3. **Method Signature Optimization**:
   - Initially created methods with many parameters (>7)
   - Refactored to use pre-mapped objects to reduce parameter count
   - Maintains clean code standards and readability

### Integration with VopController

Each VopController method now follows this pattern:

```java
// Map API parameters to structured domain objects
vopModelMapperPsd2.mapToVopRequestParameters(
    endpoint,
    xRequestId,
    vopModelMapperPsd2.mapToSigningMsg(digest, xJwsSignature),
    vopModelMapperPsd2.mapToSigningBody(bodySigProfile, bodyEncProfile, bodyEncList),
    vopModelMapperPsd2.mapToVopOtherParams(acceptLanguage, apiContractId, asyncPreferred)
);
```

This provides:
- **Consistency**: Same pattern across all VOP API methods
- **Maintainability**: Clear separation of parameter mapping logic
- **Extensibility**: Easy to add new parameters to appropriate categories

## Comparison with PaymentController

### Similarities
- Uses mapper class to transform API parameters to domain objects
- Groups related parameters into logical structures
- Handles Optional types consistently
- Uses RequestProviderService for instanceId

### Differences
- PaymentController has more complex parameter structures (TppAttributes, PsuIdData)
- VOP parameters are simpler but follow the same organizational principles
- VOP focuses more on signing/encryption parameters vs payment-specific data

## Testing Considerations

### Unit Test Requirements
1. **Mapper Method Tests**: Test each mapping method individually
2. **Optional Parameter Tests**: Verify proper handling of present/absent Optional values
3. **Integration Tests**: Test full parameter mapping flow
4. **Edge Cases**: Test with all null/empty parameters

### Test Data Examples
```java
// Test with all parameters present
Optional<String> digest = Optional.of("SHA-256=abc123");
Optional<String> signature = Optional.of("eyJ0eXAi...");
// ... other parameters

// Test with all parameters absent
Optional<String> emptyDigest = Optional.empty();
Optional<String> emptySignature = Optional.empty();
// ... other empty parameters
```

## Future Enhancements

### Service Layer Integration
The mapped VopRequestParameters will eventually be passed to:
- VopService methods for business logic processing
- Validation layers for parameter verification
- Audit/logging systems for request tracking

### Additional Parameter Categories
Future API changes might require new parameter categories:
- **Audit Parameters**: Request tracking and compliance data
- **Performance Parameters**: Timeout and priority settings
- **Feature Flags**: Optional feature enablement parameters

## Conclusion
The mapper implementation successfully follows PaymentController patterns while adapting to VOP-specific requirements. The structured approach provides a solid foundation for future service layer integration and maintains consistency with existing codebase patterns.
