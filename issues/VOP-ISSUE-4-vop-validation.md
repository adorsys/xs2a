# Add VOP validation interceptors

## Description
Add path, header, and body validation interceptors for VOP (Verification of Payee) endpoints following existing validation patterns in the codebase.

## Acceptance Criteria
- [ ] Path validation implemented for vopBulkId parameters in bulk endpoints
- [ ] Header validation for required headers (following PSD2 standards)
- [ ] Body validation for request payloads in POST endpoints
- [ ] Validation interceptors follow FundsConfirmationValidation patterns
- [ ] Proper error responses for validation failures
- [ ] Validation rules comply with Berlin Group PSD2 API requirements
- [ ] All validation logic is properly tested

## Technical Details
- **Files to modify/create:** 
  - Validation interceptor classes (following FundsConfirmationValidation patterns)
  - Path validation for `vopBulkId` parameters
  - Header validation interceptors
  - Body validation interceptors
- **Dependencies:** VopController Implementation (endpoints must exist)
- **Estimated effort:** Medium (1-2 hours)
- **Priority:** Medium

## Implementation Notes
- Reference FundsConfirmationValidation for implementation patterns
- Include validation for:
  - UUID format validation for vopBulkId
  - Required headers (PSU-ID, etc.)
  - Request body structure and required fields
  - Content-Type and Accept headers
- Ensure validation errors return appropriate HTTP status codes and error messages
- Integrate with existing validation framework in the application

## Labels
backend, validation, vop, security, enhancement


## LLM inplimentation instruction


# VOP Validation Requirements Extracted from Berlin Group Specifications

## Overview
This document extracts relevant details from the provided Berlin Group openFinance API Framework documents for the implementation of validation interceptors in VOP (Verification of Party/Payee) endpoints. The focus is on path, header, and body validation, as specified in the ticket. Data is primarily drawn from the OpenAPI specification ("BG_oFA_VOP_Version_1.1_20250821.openapi.yaml"), with supplementary context from "Verification of Party (VOP).pdf" and "Data-dictionary-2.2.6.pdf".

The validation should follow patterns similar to existing `FundsConfirmationValidation` in the codebase, ensuring compliance with PSD2 standards (e.g., required headers for security, digest signatures, and error handling). Key principles:
- **Path Validation**: Ensure parameters like `vopBulkId` conform to UUID format.
- **Header Validation**: Check for required/optional headers, including PSD2-mandated security headers (e.g., digest, signatures).
- **Body Validation**: Validate JSON structure, required fields, and data types for POST requests.
- **General Rules**: 
  - Content-Type and Accept must be `application/json`.
  - Validation failures should return appropriate HTTP status codes (e.g., 400 for bad request, 401 for unauthorized) with error bodies compliant with RFC7807 or NextGenPSD2 formats.
  - Assume Berlin Group PSD2 requirements: All requests must include anti-replay protections (e.g., `X-Request-ID` as UUID), and signatures where applicable.
- **Scopes**: Covers dedicated VOP API and bulk endpoints (sections 3.4.x in VOP.pdf). Integration with payment APIs (section 4) is noted but not primary focus unless endpoints overlap.

## 1. Endpoints Requiring Validation
From the OpenAPI spec, the following VOP endpoints are defined (under `/v1/` base path). Validation interceptors should be applied to these:

- **POST /v1/{party-verification-endpoint}**  
  Operation: `verificationOfParty`  
  Description: Single party verification request.  
  Tags: Verification of Party (VOP) API.

- **POST /v1/{bulk-party-verification-endpoint}**  
  Operation: `bulkVerificationOfParty`  
  Description: Bulk party verification request.  
  Tags: Verification of Party (VOP) API.

- **GET /v1/{bulk-party-verification-endpoint}/{vopBulkId}**  
  Operation: `getBulkVerificationOfParty`  
  Description: Retrieve bulk verification results.  
  Tags: Verification of Party (VOP) API.

- **GET /v1/{bulk-party-verification-endpoint}/{vopBulkId}/status**  
  Operation: `getBulkVerificationOfPartyStatus`  
  Description: Get status of bulk verification.  
  Tags: Verification of Party (VOP) API.

- **PUT /v1/{payment-service}/{payment-product}/{paymentId}/creditor-confirmation**  
  Operation: `creditorConfirmation`  
  Description: Confirmation of creditor name in payment context (integrated VOP).  
  Tags: Verification of Party (VOP) API.  
  *Note*: This integrates VOP with payments (per VOP.pdf section 4); validate similarly but reference payment API standards if needed.

Security: All endpoints support BearerAuthOAuth (OAuth2 token) or no auth ({}). Validate presence of token if SCA is required.

## 2. Path Parameters and Validation
Path parameters must be validated for format, presence, and PSD2 compliance (e.g., no invalid characters). Follow UUID patterns for IDs.

- **{party-verification-endpoint}** (POST single verification):  
  - Type: String (required).  
  - Validation: ASPSP-defined endpoint (e.g., regex for safe strings). Ensure it's present and non-empty.  
  - Error: 404 if not found/mismatched.

- **{bulk-party-verification-endpoint}** (All bulk endpoints):  
  - Type: String (required).  
  - Validation: Similar to above; ASPSP-defined.  
  - Error: 404 if invalid.

- **{vopBulkId}** (GET bulk endpoints):  
  - Type: Integer (in OpenAPI), but referenced as UUID in schemas (string, format: uuid).  
  - Validation: Must be a valid UUID (e.g., regex `^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$`).  
    - From schemas: `vopBulkId` is `allOf: $ref: '#/components/schemas/UUID'`.  
    - Ticket-specific: Implement path validation for this parameter in bulk endpoints.  
  - Error: 400 if invalid format; 404 if not found.

- **{payment-service}, {payment-product}, {paymentId}** (PUT creditor-confirmation):  
  - Types: Strings (required).  
  - Validation: Follow payment API patterns (e.g., `paymentId` as UUID). Ensure integration with existing payment validations.  
  - Error: 404 if resource not found.

**Implementation Note**: Use interceptors to check path params before controller invocation. Return 400/404 with error bodies (see section 5).

## 3. Header Parameters and Validation
Headers are referenced via `$ref` in OpenAPI. All endpoints share common headers; validate for presence (required unless noted). PSD2 requires headers for integrity (digest), non-repudiation (signatures), and anti-replay (`X-Request-ID`).

Common Required Headers (across all endpoints):
- **Digest**: Required. Type: String. Validation: SHA-256 hash of request body (PSD2 standard). Error: 400 if missing/mismatched.
- **X-JWS-Signature** (aliased as `xJwsSignature`): Required. Type: String. Validation: JWS signature of request. Error: 401 if invalid.
- **X-Request-ID**: Required. Type: String (UUID format). Validation: Valid UUID; unique per request. Error: 400 if invalid/missing.
- **Body-Sig-Profile** (aliased as `bodySigProfile`): Required (for signed bodies). Type: String. Validation: Profile identifier.
- **Body-Enc-Profile** (aliased as `bodyEncProfile`): Required (if encrypted). Type: String.
- **Body-Enc-List** (aliased as `bodyEncList`): Optional/Required in some. Type: String.
- **API-Contract-ID** (aliased as `apiContractId`): Required. Type: String. Validation: Contract identifier for API access.

Endpoint-Specific Headers:
- **POST /v1/{bulk-party-verification-endpoint}**:  
  - **Client-Asynchronous-Process-Preferred** (aliased as `clientAsynchronousProcessPreferred`): Optional. Type: Boolean/String. Validation: If present, must be valid boolean.
  - **Accept-Language**: Optional. Type: String (e.g., en-US).

- **All GET Endpoints**: No additional specifics; same as common.

Other PSD2 Headers (implied from Berlin Group standards, not explicitly in OpenAPI but recommended):
- **PSU-ID**: Optional/Required for PSU context. Validation: String, if present.
- **TPP-Redirect-Preferred**: Optional.
- **Content-Type**: Must be `application/json`. Error: 415 if unsupported.
- **Accept**: Must include `application/json`. Error: 406 if not acceptable.

**Validation Rules**:
- Required headers: Fail with 400/401 if missing.
- Format: UUID for IDs; hashes for digest.
- Follow `FundsConfirmationValidation` patterns: Pre-controller checks.

## 4. Body Validation (Request Payloads)
Only POST/PUT endpoints have bodies. Validate JSON structure, required fields, data types, and constraints (e.g., minItems). Use schemas from OpenAPI and Data Dictionary.

- **POST /v1/{party-verification-endpoint}** (Single Verification):  
  - Content-Type: `application/json` (required).  
  - Schema: Object with required fields: `partyAccount`, `party`.  
    - `partyAccount`: $ref to `accountReference` (from Data Dictionary: IBAN/BBAN/proxy; restricted to IBAN by ASPSP). Validation: Required; type object with `iban` (string, format IBAN).  
    - `party`: $ref to `partyDescription4`. Validation: Required; contains name/identifier.  
    - Optional: `partyAgent` ($ref `agentDescription`), `unstructuredRemittanceInformation` (array of Max140Text), `requestingAgent`.  
  - Constraints: Array fields non-empty if present.  
  - Error: 400 if missing required fields or invalid types.

- **POST /v1/{bulk-party-verification-endpoint}** (Bulk Verification):  
  - Content-Type: `application/json` (required).  
  - Schema: Object with required: `partyVerificationRequests` (array, minItems: 1).  
    - Each item: $ref `partyVerificationRequest` (required: `uetr` (UUID), `partyAccount`, `party`).  
      - `uetr`: UUID string. Validation: Required, valid UUID.  
    - Optional: `requestingAgent`.  
  - Constraints: Non-empty array; each entry must validate as single request.  
  - Error: 400 if empty array or invalid nested structures.

- **PUT /v1/{payment-service}/{payment-product}/{paymentId}/creditor-confirmation**:  
  - No requestBody defined in OpenAPI (empty body?). Validation: Ensure no body or empty object; integrate with payment body validations if needed.  
  - Error: 415 if body present but unsupported.

**Data Types from Data Dictionary** (Relevant Extracts):
- `accountReference`: Object (IBAN string, max 34 chars; optional BBAN/proxy).
- `UUID`: String (uuid format).
- `Max140Text`: String, max 140 chars.
- `Max500Text`: String, max 500 chars.
- Validation: Use JSON Schema validators; check min/max lengths, enums (e.g., OrganisationIdentificationCode enum in Data Dictionary).

## 5. Error Responses for Validation Failures
All endpoints define decline responses (400-503) with schemas in `application/json` or `application/problem+json` (RFC7807).

- **Common Error Schemas**:  
  - `error_RFC7807_400_VOP`: Object with `type` (URI), `code` (enum from MessageCode), `title`, `detail`, `status`.  
    - Example Codes: FORMAT_ERROR, PARAMETER_NOT_SUPPORTED.  
  - Similar for 401 (UNAUTHORIZED), 403 (FORBIDDEN), etc.  
  - Additional: `additionalErrors` array for multiple issues.

- **Validation-Specific Errors**:
  - 400 (Bad Request): Invalid body/path/header (e.g., missing required field). Use `code: FORMAT_ERROR`.
  - 401 (Unauthorized): Invalid signature/token.
  - 404 (Not Found): Invalid path param (e.g., bad vopBulkId).
  - 406 (Not Acceptable): Invalid Accept header.
  - 415 (Unsupported Media Type): Wrong Content-Type.
  - 409 (Conflict): Duplicate request (e.g., reused X-Request-ID).

**Implementation Note**: Return RFC7807-compliant bodies; include `_links` for error navigation if applicable.

## 6. Additional Notes
- **Consent Model**: Per VOP.pdf (section 2.2.3), dedicated VOP may require consent. Validate via headers/token.
- **Operational Rules**: From VOP.pdf (section 2.2), support service types and corporate auth. Ensure interceptors check for API-Contract-ID.
- **Testing**: Cover UUID validation for vopBulkId, required body fields, header presence. Use unit tests mimicking FundsConfirmation patterns.
- **Dependencies**: Endpoints must exist (VopController). Reference Data Dictionary for full type details (e.g., complex types like AccountReference).

This extract focuses on ticket needs; full docs should be consulted for edge cases.
