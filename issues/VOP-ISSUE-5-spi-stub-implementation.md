# Create VopSpi stub implementation

## Description
Create a default VopSpi implementation in xs2a-impl for testing purposes, providing mock responses for all VOP operations.

## Acceptance Criteria
- [ ] VopSpiStub class is created in the SPI stub package
- [ ] Implements all 4 VopSpi interface methods:
  - Single party verification
  - Bulk party verification initiation
  - Bulk verification status
  - Bulk verification results
- [ ] Returns appropriate mock responses for testing
- [ ] Handles edge cases and error scenarios
- [ ] Follows existing SPI stub patterns in the codebase
- [ ] Can be configured as the default SPI implementation for testing

## Technical Details
- **Files to create:** `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/spi/stub/VopSpiStub.java`
  e.g 
  
  spi-stub/src/main/java/de/adorsys/psd2/stub/impl/FundsConfirmationSpiMockImpl.java:35-36
```
public class FundsConfirmationSpiMockImpl implements FundsConfirmationSpi {

```
- **Dependencies:** None (SPI interface already exists)
- **Estimated effort:** Low (30-60 minutes)
- **Priority:** Medium

## Implementation Notes
- Reference existing SPI stubs (like FundsConfirmationSpiStub) for patterns
- Provide realistic mock data for party verification responses
- Include proper error handling and status responses
- Support both successful and failed verification scenarios
- Ensure thread-safe implementation for concurrent testing

## Labels
backend, spi, stub, testing, vop, enhancement