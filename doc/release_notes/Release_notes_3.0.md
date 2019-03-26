# Release notes v.3.0

## ASPSP Mock Server and the corresponding connector are removed
ASPSP Mock Server and corresponding connector were replaced by the XS2A started and stub SPI implementation.
Integration tests were moved from `spi-mock` package to `xs2a-standalone-starter`. `QwacCertificateFilterMock.java` 
class was moved from `spi-mock` to `xs2a-impl` package.
