/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.body.vop;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Specialized validator for VOP-specific fields and business rules.
 * This validator handles UETR validation, party matching validation,
 * and other VOP-specific validation requirements.
 */
@Component
public class VopFieldValidator {

    private final ErrorBuildingService errorBuildingService;

    public VopFieldValidator(ErrorBuildingService errorBuildingService) {
        this.errorBuildingService = errorBuildingService;
    }

    /**
     * Validates UETR (Unique End-to-End Transaction Reference) format.
     * UETR must be a valid UUID v4 format according to VOP specification.
     */
    public void validateUetr(String uetr, MessageError messageError, String fieldPrefix) {
        if (uetr == null || uetr.trim().isEmpty()) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, fieldPrefix + ".uetr is required"));
            return;
        }

        // Validate UUID format
        try {
            UUID.fromString(uetr);
        } catch (IllegalArgumentException e) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, fieldPrefix + ".uetr must be a valid UUID"));
        }

        // Additional UETR business rules can be added here
        if (uetr.length() != 36) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, fieldPrefix + ".uetr must be 36 characters long"));
        }
    }

    /**
     * Validates party account structure for VOP requests.
     * Party account must contain either IBAN or BBAN.
     */
    public void validatePartyAccount(Object partyAccount, MessageError messageError, String fieldPrefix) {
        if (partyAccount == null) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, fieldPrefix + ".partyAccount is required"));
            return;
        }

        if (partyAccount instanceof Map) {
            Map<?, ?> accountMap = (Map<?, ?>) partyAccount;
            validatePartyAccountFields(accountMap, messageError, fieldPrefix);
        }
    }

    private void validatePartyAccountFields(Map<?, ?> accountMap, MessageError messageError, String fieldPrefix) {
        String iban = extractStringValue(accountMap, "iban");
        String bban = extractStringValue(accountMap, "bban");
        String currency = extractStringValue(accountMap, "currency");

        validateIbanOrBbanPresence(iban, bban, messageError, fieldPrefix);
        validateIbanFormat(iban, messageError, fieldPrefix);
        validateBbanFormat(bban, messageError, fieldPrefix);
        validateCurrencyFormat(currency, messageError, fieldPrefix);
    }

    private void validateIbanOrBbanPresence(String iban, String bban, MessageError messageError, String fieldPrefix) {
        if ((iban == null || iban.trim().isEmpty()) && (bban == null || bban.trim().isEmpty())) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE,
                    fieldPrefix + ".partyAccount must contain either iban or bban"));
        }
    }

    private void validateIbanFormat(String iban, MessageError messageError, String fieldPrefix) {
        if (iban != null && !iban.trim().isEmpty() && !isValidIban(iban)) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE,
                    fieldPrefix + ".partyAccount.iban format is invalid"));
        }
    }

    private void validateBbanFormat(String bban, MessageError messageError, String fieldPrefix) {
        if (bban != null && !bban.trim().isEmpty() && !isValidBban(bban)) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE,
                    fieldPrefix + ".partyAccount.bban format is invalid"));
        }
    }

    private void validateCurrencyFormat(String currency, MessageError messageError, String fieldPrefix) {
        if (currency != null && !isValidCurrencyCode(currency)) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE,
                    fieldPrefix + ".partyAccount.currency must be a valid ISO 4217 code"));
        }
    }

    /**
     * Validates party structure for VOP requests.
     * Party must contain either name or identification (or both).
     */
    public void validateParty(Object party, MessageError messageError, String fieldPrefix) {
        if (party == null) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, fieldPrefix + ".party is required"));
            return;
        }

        if (party instanceof Map) {
            Map<?, ?> partyMap = (Map<?, ?>) party;
            
            String name = extractStringValue(partyMap, "name");
            Object identification = partyMap.get("identification");

            // At least name or identification must be present
            if ((name == null || name.trim().isEmpty()) && identification == null) {
                errorBuildingService.enrichMessageError(messageError, 
                    TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, 
                        fieldPrefix + ".party must contain either name or identification"));
            }

            // Validate name length if present
            if (name != null && name.length() > 140) {
                errorBuildingService.enrichMessageError(messageError, 
                    TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, 
                        fieldPrefix + ".party.name exceeds 140 characters"));
            }

            // Validate identification structure if present
            if (identification != null) {
                validatePartyIdentification(identification, messageError, fieldPrefix + ".party");
            }
        }
    }

    /**
     * Validates bulk verification request size limits.
     */
    public void validateBulkSize(List<?> bulkRequests, MessageError messageError) {
        if (bulkRequests == null || bulkRequests.isEmpty()) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "partyVerificationRequests is required and cannot be empty"));
            return;
        }

        // Check maximum bulk size (configurable, default 1000)
        int maxBulkSize = getMaxBulkSize();
        if (bulkRequests.size() > maxBulkSize) {
            errorBuildingService.enrichMessageError(messageError, 
                TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, 
                    "Bulk request exceeds maximum size of " + maxBulkSize + " items"));
        }
    }

    /**
     * Validates unstructured remittance information array.
     */
    public void validateUnstructuredRemittanceInformation(List<?> remittanceInfo, MessageError messageError, String fieldPrefix) {
        if (remittanceInfo != null) {
            for (int i = 0; i < remittanceInfo.size(); i++) {
                Object item = remittanceInfo.get(i);
                if (item instanceof String) {
                    String text = (String) item;
                    if (text.length() > 140) {
                        errorBuildingService.enrichMessageError(messageError, 
                            TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, 
                                fieldPrefix + ".unstructuredRemittanceInformation[" + i + "] exceeds 140 characters"));
                    }
                }
            }
        }
    }

    private void validatePartyIdentification(Object identification, MessageError messageError, String fieldPrefix) {
        if (identification instanceof Map) {
            Map<?, ?> idMap = (Map<?, ?>) identification;
            
            Object organisationId = idMap.get("organisationId");
            Object privateId = idMap.get("privateId");

            // At least one identification type must be present
            if (organisationId == null && privateId == null) {
                errorBuildingService.enrichMessageError(messageError, 
                    TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, 
                        fieldPrefix + ".identification must contain either organisationId or privateId"));
            }
        }
    }

    private String extractStringValue(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }

    private boolean isValidIban(String iban) {
        // Basic IBAN validation - should start with 2 letters followed by 2 digits and up to 30 alphanumeric characters
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        return iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$");
    }

    private boolean isValidBban(String bban) {
        // Basic BBAN validation - alphanumeric string up to 30 characters
        if (bban == null || bban.length() > 30) {
            return false;
        }
        return bban.matches("^[A-Z0-9]+$");
    }

    private boolean isValidCurrencyCode(String currency) {
        // Basic ISO 4217 currency code validation - 3 uppercase letters
        if (currency == null || currency.length() != 3) {
            return false;
        }
        return currency.matches("^[A-Z]{3}$");
    }

    private int getMaxBulkSize() {
        // This could be configurable via application properties
        // For now, using a reasonable default based on typical VOP implementations
        return 1000;
    }
}
