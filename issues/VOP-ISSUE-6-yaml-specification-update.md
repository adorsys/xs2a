# Update OpenAPI specification to version 1.3.15 with VOP endpoints

## Description
Update the OpenAPI specification to version 1.3.15 and include the VOP (Verification of Payee) endpoints following Berlin Group PSD2 API standards.

## Acceptance Criteria
- [ ] OpenAPI specification version updated to 1.3.15
- [ ] All 4 VOP endpoints are documented:
  - `POST /v1/verification-of-payee`
  - `POST /v1/verification-of-payee/bulk`
  - `GET /v1/verification-of-payee/bulk/{vopBulkId}/status`
  - `GET /v1/verification-of-payee/bulk/{vopBulkId}`
- [ ] Request/response schemas match implemented API models
- [ ] Proper parameter definitions and validation rules
- [ ] Security schemes and authentication requirements defined
- [ ] OpenAPI specification validates successfully
- [ ] Documentation includes examples and descriptions

## Technical Details
- **Files to modify:** OpenAPI YAML specification files (likely in xs2a-server-api or similar)
- **Dependencies:** VopController Implementation (to ensure endpoint specifications match)
- **Estimated effort:** Low (30-60 minutes)
- **Priority:** Low

## Implementation Notes
- Reference Berlin Group PSD2 API specification for VOP endpoint definitions
- Ensure consistency with existing API documentation patterns
- Include proper error response definitions
- Update version number and changelog information
- Validate the specification using OpenAPI tools

## Labels
documentation, api, vop, openapi, enhancement