# Implementing New Features for Payment Initiation and Reporting

## Overview
This document outlines the implementation of new features for instant SEPA credit transfers, bulk payment initiations, and card transaction reporting within the PSD2/XS2A framework. Each feature is detailed with specific tasks, relevant classes, and verification steps to ensure compliance with regulatory and technical requirements.

## Features and Implementation Tasks

### 1. Add `startDateTime` and `endDateTime` as Optional Attributes for Standing Orders in Instant Payments
- **Description**: Introduce `startDateTime` and `endDateTime` as optional attributes for periodic payments (specifically instant SEPA credit transfers). If `endDateTime` is not provided, the standing order is considered infinite.
- **Definition of Done**:
    - Applies only to instant SEPA credit transfers for periodic payments.
    - Ensure proper mapping and validation of `startDateTime` and `endDateTime`.
- **Tasks**:
    - Update mappers and classes related to `startDateTime` and `endDateTime`:
        - `PeriodicPayment`
        - `PeriodicPaymentInitiationJson`
        - `StandardPaymentProductsResolver`
        - `PaymentService`
    - Review YAML files and API documentation to verify or update definitions for these attributes.
    - Ensure compliance with the requirement: "If none of the attributes is given, it is an infinite standing order."
- **Verification**:
    - Confirm that mappers correctly handle optional fields.
    - Validate that API documentation reflects the updated attributes.

### 2. Avoid Local Time Without Offsets in Timestamps
- **Description**: Ensure all timestamps in the codebase use UTC or include appropriate timezone offsets to prevent ambiguity.
- **Tasks**:
    - Scan the codebase for instances of local time without offsets in timestamp fields.
    - Update affected code to enforce UTC or explicit timezone offsets (e.g., ISO 8601 format with offsets like `2025-10-22T13:30:00+01:00`).
- **Verification**:
    - Verify that all timestamp-related operations use UTC or include offsets.
    - Check for consistency across services and data models.

### 3. Add `categoryPurposeCode` Attribute at Bulk Level for JSON-Based Payment Initiation
- **Description**: Introduce a new `categoryPurposeCode` attribute at the bulk level for JSON-based payment initiations, ensuring it is mapped and validated in responses.
- **Tasks**:
    - Add `categoryPurposeCode` to relevant classes:
        - `CategoryPurposeCode`
        - `PaymentService`
        - SPI API: `src/main/java/de/adorsys/psd2/xs2a/spi/service/BulkPaymentSpi.java`
        - SPI Stub: `src/main/java/de/adorsys/psd2/stub/impl/BulkPaymentSpiMockImpl.java`
    - Update mappers and validators to include `categoryPurposeCode` in the response body of bulk payment initiation services.
    - Check for additional classes requiring updates in the SPI or service layers.
- **Verification**:
    - Confirm that `categoryPurposeCode` is included in the JSON response.
    - Validate that the attribute is properly handled by mappers and validators.

### 4. Support Full Structured Address in Payment Data for Regulatory Compliance
- **Description**: Update the `Address` object to support a full structured address for describing involved parties in payment data, as required for regulatory reasons.
- **Tasks**:
    - Update the address structure in:
        - `src/main/java/de/adorsys/psd2/model/Address.java`
        - `src/main/java/de/adorsys/psd2/core/payment/model/Address.java`
        - `src/main/java/de/adorsys/psd2/xs2a/spi/domain/payment/SpiAddress.java`
    - Update the CMS database and related mappers:
        - `src/main/java/de/adorsys/psd2/consent/domain/payment/PisAddress.java`
        - `src/main/java/de/adorsys/psd2/consent/service/mapper/CmsAddressMapper.java`
        - `src/main/java/de/adorsys/psd2/consent/api/CmsAddress.java`
    - Scan for additional classes requiring updates to handle the new address structure.
- **Verification**:
    - Ensure the updated address structure is correctly persisted in the CMS database.
    - Validate that mappers correctly transform the structured address data.

### 5. Add `cardAcceptorName` to Card Transaction Reports and Clarify `townName` Usage in Payment Initiations
- **Description**: Introduce `cardAcceptorName` in card transaction reports and clarify the regulatory usage of `townName` in the `PostalAddress` object. If address lines are not used, `townName` is mandatory.
- **Tasks**:
    - Add `cardAcceptorName` to relevant classes for card transaction reports.
    - Update `src/main/java/de/adorsys/psd2/model/PostalAddress.java` to include `townName` in both the `PostalAddress` object and its address line array.
    - Enforce `townName` as mandatory when address lines are not provided.
    - Check for additional classes requiring updates to support these attributes.
- **Verification**:
    - Confirm that `cardAcceptorName` is included in card transaction report responses.
    - Validate that `townName` is correctly handled and enforced as mandatory when address lines are absent.
