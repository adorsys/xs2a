# Create Postman collection for VOP API testing

## Description
Create a Postman collection for VOP (Verification of Payee) API testing with sample requests for all 4 endpoints, including proper headers, request bodies, and test scripts.

## Acceptance Criteria
- [ ] Postman collection JSON file is created
- [ ] Collection includes sample requests for all 4 VOP endpoints:
  - Single party verification request
  - Bulk party verification initiation
  - Bulk verification status check
  - Bulk verification results retrieval
- [ ] Proper headers configured (Content-Type, Accept, PSU-ID, etc.)
- [ ] Sample request bodies with valid data structures
- [ ] Environment variables for dynamic values (vopBulkId, base URL, etc.)
- [ ] Test scripts for response validation
- [ ] Collection follows Postman best practices and organization

## Technical Details
- **Files to create:** Postman collection JSON file (e.g., `VOP_API_Collection.postman_collection.json`)
- **Dependencies:** VopController Implementation (to ensure request formats match)
- **Estimated effort:** Low (30-60 minutes)
- **Priority:** Low

## Implementation Notes
- Reference existing Postman collections in the project for consistency
- Include examples for both successful and error scenarios
- Use environment variables for configurable values
- Include pre-request scripts if needed for authentication
- Add test assertions for response validation
- Document the collection usage in README or comments

## Labels
testing, api, vop, postman, documentation, enhancement