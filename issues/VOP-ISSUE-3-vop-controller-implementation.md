# Implement VopController with 4 VOP endpoints

## Description
Create VopController with 4 endpoints following the FundsConfirmationController pattern for VOP (Verification of Payee) operations.

## Acceptance Criteria
- [ ] VopController class is created with proper Spring annotations
- [ ] All 4 VOP endpoints are implemented:
  - `POST /v1/verification-of-payee` - Single party verification
  - `POST /v1/verification-of-payee/bulk` - Bulk party verification initiation
  - `GET /v1/verification-of-payee/bulk/{vopBulkId}/status` - Bulk verification status
  - `GET /v1/verification-of-payee/bulk/{vopBulkId}` - Bulk verification results
- [ ] Proper dependency injection of VopService and VopModelMapper
- [ ] Error handling follows established patterns (PsuIdHeaderException, etc.)
- [ ] Request/response mapping uses VopModelMapper
- [ ] Path parameters are properly validated and extracted
- [ ] Controller compiles without errors

## Technical Details
- **Files to create:** `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/VopController.java`
- **Dependencies:** Complete VopModelMapper (for request/response mapping)
- **Estimated effort:** Medium (1-2 hours)
- **Priority:** High

## Implementation Notes
- Follow FundsConfirmationController as the reference implementation
- Implement VerificationOfPartyApi interface from xs2a-vop-api module
- Include proper logging and event publishing
- Handle bulk operations with proper ID generation and status tracking
- Ensure compliance with Berlin Group PSD2 API standards

## Labels
backend, controller, vop, api, enhancement