# VOP Controller Mapping Implementation Checklist

## Overview
This document outlines the step-by-step implementation plan for mapping API requests to domain requests in the VopController, following the patterns established in PaymentController.

## Analysis of PaymentController Patterns

### Key Findings:
1. **Parameter Mapping**: PaymentController uses `PaymentInitiationParameters` object to encapsulate all request parameters
2. **Mapper Pattern**: Uses `PaymentModelMapperPsd2.mapToPaymentRequestParameters()` to transform API request parameters into domain objects
3. **TppAttributes Structure**: Groups related parameters (certificates, URIs, preferences) into structured objects
4. **PsuIdData Structure**: Consolidates PSU-related parameters into a single data object
5. **Service Layer Integration**: Passes structured parameters to service layer for business logic processing

### Parameter Categories in PaymentController:
- **Security Parameters**: `digest`, `signature`, `tpPSignatureCertificate`
- **TPP Parameters**: `tpPRedirectURI`, `tpPNokRedirectURI`, `tpPExplicitAuthorisationPreferred`, etc.
- **PSU Parameters**: `psuId`, `psUIDType`, `psUCorporateID`, `psUIPAddress`, etc.
- **Notification Parameters**: `tpPNotificationURI`, `tpPNotificationContentPreferred`

## Implementation Checklist

### Task 1: Analyze Current VopController Structure
- [x] Review current VopController implementation
- [x] Identify missing parameter mapping patterns
- [x] Compare with PaymentController approach

### Task 2: Create VOP Parameter Structure Classes
- [x] Create `VopRequestParameters` class (similar to `PaymentInitiationParameters`)
- [x] Create `SigningMsg` DTO for signature-related parameters
- [x] Create `SigningBody` DTO for body signing parameters
- [x] Create `VopOtherParams` DTO for remaining parameters

### Task 3: Create VOP Parameter Mappers
- [x] Create `VopModelMapperPsd2` class (similar to `PaymentModelMapperPsd2`)
- [x] Implement `mapToSigningMsg()` method
- [x] Implement `mapToSigningBody()` method
- [x] Implement `mapToVopOtherParams()` method
- [x] Implement `mapToVopRequestParameters()` master method

### Task 4: Implement Signature Message Mapping
- [x] Map `xJwsSignature` parameter to SigningMsg
- [x] Map `digest` parameter to SigningMsg
- [x] Handle Optional<String> types properly

### Task 5: Implement Signing Body Mapping
- [x] Map `bodySigProfile` parameter to SigningBody
- [x] Map `bodyEncProfile` parameter to SigningBody
- [x] Map `bodyEncList` parameter to SigningBody

### Task 6: Implement Other Parameters Mapping
- [x] Map `acceptLanguage` parameter
- [x] Map `apIContractID` parameter
- [x] Map `clientAsynchronousProcessPreferred` parameter (for bulk operations)
- [x] Map common headers (`xRequestID`, endpoint parameters)

### Task 7: Update VopController Methods
- [x] Modify `verificationOfParty()` method to use new parameter mapping
- [x] Modify `bulkVerificationOfParty()` method to use new parameter mapping
- [x] Modify `getBulkVerificationOfPartyStatus()` method to use new parameter mapping
- [x] Modify `getBulkVerificationOfParty()` method to use new parameter mapping

### Task 8: Integration and Testing
- [x] Ensure proper dependency injection of new mappers
- [x] Update constructor and field declarations
- [x] Verify compilation and basic functionality
- [x] Document implementation decisions

## Success Criteria
- [x] All VopController methods use structured parameter mapping similar to PaymentController
- [x] Parameters are properly categorized into `signingMsg`, `signingBody`, and `otherParams`
- [x] Code follows established patterns and conventions from PaymentController
- [x] Implementation is documented and maintainable

## Implementation Status: COMPLETED ✅

### Core Implementation Complete
All major tasks have been successfully implemented:
- Parameter structure classes created and functional
- Mapper classes implemented following PaymentController patterns
- All VopController methods updated to use structured parameter mapping
- Comprehensive documentation created for implementation details

### Future Integration Tasks (Outside Current Scope)
- Service layer integration (VopService interface updates)
- Parameter utilization in business logic
- Unit test coverage
- End-to-end integration testing

## Notes
- Follow the same architectural patterns as PaymentController
- Maintain consistency with existing codebase structure
- Document any deviations or specific VOP requirements
- Ensure proper handling of Optional parameters
