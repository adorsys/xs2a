# VOP Implementation - Remaining Tasks

## Overview
This document outlines the remaining tasks to complete the VOP (Verification of Payee) specification implementation following Berlin Group PSD2 API standards.

## Current Status
✅ **Completed (9/16 tasks):**
- VOP Profile Configuration
- VOP Domain Models
- VOP SPI Interface
- SPI Mappers
- VopService Implementation
- VopModelMapper Placeholder
- Event Types
- Service Type
- SPI Bulk Status Response

## Remaining Tasks

### 1. xs2a-vop-api Integration
**Priority:** High
**Description:** Add the xs2a-vop-api module to the main Maven reactor to enable access to generated API model classes.
**Files to modify:**
- `pom.xml` (root) - Add xs2a-vop-api to modules list
**Dependencies:** None
**Estimated effort:** Low

### 2. Complete VopModelMapper
**Priority:** High
**Description:** Implement the complete VopModelMapper with API-to-domain conversions following the FundsConfirmationModelMapper pattern.
**Files to modify:**
- `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/service/mapper/VopModelMapper.java`
**Dependencies:** xs2a-vop-api Integration
**Estimated effort:** Medium

### 3. VopController Implementation
**Priority:** High
**Description:** Create VopController with 4 endpoints following the FundsConfirmationController pattern:
- `POST /v1/verification-of-payee` - Single party verification
- `POST /v1/verification-of-payee/bulk` - Bulk party verification initiation
- `GET /v1/verification-of-payee/bulk/{vopBulkId}/status` - Bulk verification status
- `GET /v1/verification-of-payee/bulk/{vopBulkId}` - Bulk verification results
**Files to create:**
- `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/VopController.java`
**Dependencies:** Complete VopModelMapper
**Estimated effort:** Medium

### 4. VOP Validation
**Priority:** Medium
**Description:** Add path, header, and body validation interceptors for VOP endpoints following existing validation patterns.
**Files to modify/create:**
- Validation interceptors (following FundsConfirmationValidation patterns)
- Path validation for vopBulkId parameters
- Header validation for required headers
- Body validation for request payloads
**Dependencies:** VopController Implementation
**Estimated effort:** Medium

### 5. SPI Stub Implementation
**Priority:** Medium
**Description:** Create a default VopSpi implementation in xs2a-impl for testing purposes.
**Files to create:**
- `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/spi/stub/VopSpiStub.java`
**Dependencies:** None
**Estimated effort:** Low

### 6. YAML Specification Update
**Priority:** Low
**Description:** Update the OpenAPI specification to version 1.3.15 with VOP endpoints.
**Files to modify:**
- OpenAPI YAML specification files
**Dependencies:** VopController Implementation
**Estimated effort:** Low

### 7. Postman Collection
**Priority:** Low
**Description:** Create Postman collection for VOP API testing with sample requests for all 4 endpoints.
**Files to create:**
- Postman collection JSON file
**Dependencies:** VopController Implementation
**Estimated effort:** Low

## Implementation Order
1. **xs2a-vop-api Integration** - Unblocks API model access
2. **Complete VopModelMapper** - Required for controller
3. **VopController Implementation** - Core functionality
4. **VOP Validation** - Enhances controller
5. **SPI Stub Implementation** - Enables testing
6. **YAML Specification Update** - Documentation
7. **Postman Collection** - Testing tools

## Technical Notes
- All implementations should follow the established FundsConfirmationService/Controller patterns
- VOP uses 4 main operations: single verification, bulk initiation, status check, and results retrieval
- API models are generated from OpenAPI specifications in xs2a-vop-api module
- SPI layer provides clean separation between XS2A and ASPSP implementations
- Validation should include path parameters, required headers, and request body validation

## Success Criteria
- All 4 VOP endpoints are implemented and functional
- Proper request/response mapping between API, domain, and SPI layers
- Comprehensive validation for all endpoints
- Clean compilation with no errors
- Following established codebase patterns and conventions</content>
<parameter name="filePath">/Users/esum/Desktop/Main/Projects/AdorsysProjects/ASPSP/aspsp-xs2a/VOP_REMAINING_TASKS.md