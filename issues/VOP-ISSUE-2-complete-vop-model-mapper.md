# Complete VopModelMapper implementation

## Description
Implement the complete VopModelMapper with API-to-domain conversions for all 4 VOP (Verification of Payee) operations following the FundsConfirmationModelMapper pattern.

## Acceptance Criteria
- [ ] VopModelMapper contains mappings for all 4 VOP operations:
  - Single party verification request/response
  - Bulk party verification initiation request/response
  - Bulk verification status request/response
  - Bulk verification results request/response
- [ ] Proper conversion between generated API models and XS2A domain objects
- [ ] Follows existing mapper patterns and conventions in the codebase
- [ ] All mappings handle null values and edge cases appropriately
- [ ] Unit tests pass for the mapper functionality

## Technical Details
- **Files to modify:** `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/service/mapper/VopModelMapper.java`
- **Dependencies:** xs2a-vop-api Integration (API model classes must be accessible)
- **Estimated effort:** Medium (1-2 hours)
- **Priority:** High

## Implementation Notes
- Reference FundsConfirmationModelMapper for pattern consistency
- API models are generated from OpenAPI specifications in xs2a-vop-api module
- Ensure proper handling of bulk operations with IDs and status responses
- Include proper error handling and validation in mappings

## Labels
backend, vop, mapper, enhancement