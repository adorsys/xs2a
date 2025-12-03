# Integrate xs2a-vop-api module into Maven reactor

## Description
Add the xs2a-vop-api module to the main Maven reactor to enable access to generated API model classes required for VOP (Verification of Payee) implementation.

## Acceptance Criteria
- [ ] xs2a-vop-api module is included in the modules list in root pom.xml
- [ ] Maven build compiles the xs2a-vop-api module successfully
- [ ] Generated API model classes are accessible to dependent modules (xs2a-impl)
- [ ] No compilation errors in the Maven reactor build

## Technical Details
- **Files to modify:** `pom.xml` (root)
- **Dependencies:** None
- **Estimated effort:** Low (5-10 minutes)
- **Priority:** High

## Implementation Notes
- Add `<module>xs2a-vop-api</module>` to the modules section in the root pom.xml
- The xs2a-vop-api module contains OpenAPI-generated model classes for VOP endpoints
- This integration is required before implementing VopModelMapper and VopController

## Labels
backend, build, vop, enhancement