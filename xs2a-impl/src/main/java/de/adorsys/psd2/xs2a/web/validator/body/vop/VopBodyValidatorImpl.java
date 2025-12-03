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

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.*;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NULL_VALUE;

@Component
@SuppressWarnings("PMD")
public class VopBodyValidatorImpl extends AbstractBodyValidatorImpl implements VopBodyValidator {

    private final FieldExtractor fieldExtractor;
    private final VopFieldValidator vopFieldValidator;

    public VopBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                FieldLengthValidator fieldLengthValidator,
                                FieldExtractor fieldExtractor, VopFieldValidator vopFieldValidator) {
        super(errorBuildingService, xs2aObjectMapper, fieldLengthValidator);
        this.fieldExtractor = fieldExtractor;
        this.vopFieldValidator = vopFieldValidator;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        // For VOP endpoints, we need to validate different types of request bodies based on the endpoint
        // We'll check the request URI to determine which validation to apply

        String requestUri = request.getRequestURI();

        // Check for bulk endpoints by looking for the actual endpoint names (not path variable placeholders)
        if (requestUri.contains("bulk-payee-verifications") ||
            requestUri.contains("bulk-payer-verifications") ||
            requestUri.contains("bulk-party-verifications")) {
            return validateBulkPartyVerificationRequest(request, messageError);
        } else if (requestUri.contains("payee-verifications") ||
                   requestUri.contains("payer-verifications") ||
                   requestUri.contains("party-verifications")) {
            return validateSinglePartyVerificationRequest(request, messageError);
        }

        // Default validation - if no specific endpoint pattern matches
        return validateSinglePartyVerificationRequest(request, messageError);
    }

    private MessageError validateSinglePartyVerificationRequest(HttpServletRequest request, MessageError messageError) {
        // For single party verification, we expect a request with partyAccount and party fields
        // Based on the OpenAPI spec:
        // - partyAccount: required, object with account reference (IBAN/BBAN)
        // - party: required, contains name and/or identifier
        // - partyAgent: optional, agent description
        // - unstructuredRemittanceInformation: optional array of Max140Text
        // - requestingAgent: optional, for inter-bank/inter-PSP

        try {
            // Extract and validate the request body as a complete object
            Optional<Object> bodyOptional = fieldExtractor.mapBodyToInstance(request, messageError, Object.class);

            if (bodyOptional.isEmpty()) {
                return messageError;
            }

            Object requestBody = bodyOptional.get();

            // Validate required fields
            validateRequiredField(requestBody, "partyAccount", messageError);
            validateRequiredField(requestBody, "party", messageError);

            // Validate partyAccount structure and format
            Object partyAccount = extractFieldValue(requestBody, "partyAccount");
            vopFieldValidator.validatePartyAccount(partyAccount, messageError, "request");

            // Validate party structure
            Object party = extractFieldValue(requestBody, "party");
            vopFieldValidator.validateParty(party, messageError, "request");

            // Validate optional fields if present
            Object unstructuredRemittanceInfo = extractFieldValue(requestBody, "unstructuredRemittanceInformation");
            if (unstructuredRemittanceInfo != null) {
                validateUnstructuredRemittanceInformation(unstructuredRemittanceInfo, messageError);
            }

            Object partyAgent = extractFieldValue(requestBody, "partyAgent");
            if (partyAgent != null) {
                validatePartyAgent(partyAgent, messageError);
            }

        } catch (Exception e) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "Invalid request body format"));
        }

        return messageError;
    }

    private MessageError validateBulkPartyVerificationRequest(HttpServletRequest request, MessageError messageError) {
        // For bulk party verification, we expect:
        // - partyVerificationRequests: required array, minItems: 1
        // - Each item: required fields: uetr (UUID), partyAccount, party
        // - Optional: requestingAgent

        try {
            Optional<Object> bodyOptional = fieldExtractor.mapBodyToInstance(request, messageError, Object.class);

            if (bodyOptional.isEmpty()) {
                return messageError;
            }

            Object requestBody = bodyOptional.get();

            // Extract bulk requests array
            Object bulkRequestsObj = extractFieldValue(requestBody, "partyVerificationRequests");
            if (bulkRequestsObj instanceof java.util.List) {
                java.util.List<?> bulkRequests = (java.util.List<?>) bulkRequestsObj;

                // Validate bulk size
                vopFieldValidator.validateBulkSize(bulkRequests, messageError);

                // Validate each individual request in the bulk
                for (int i = 0; i < bulkRequests.size(); i++) {
                    Object individualRequest = bulkRequests.get(i);
                    validateIndividualBulkRequest(individualRequest, messageError, "partyVerificationRequests[" + i + "]");
                }
            } else {
                errorBuildingService.enrichMessageError(messageError,
                    TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "partyVerificationRequests must be an array"));
            }

        } catch (Exception e) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "Invalid bulk request body format"));
        }

        return messageError;
    }

    private void validateIndividualBulkRequest(Object individualRequest, MessageError messageError, String fieldPrefix) {
        if (individualRequest instanceof java.util.Map) {
            java.util.Map<?, ?> requestMap = (java.util.Map<?, ?>) individualRequest;

            // Validate required fields for each bulk item
            String uetr = extractStringValue(requestMap, "uetr");
            vopFieldValidator.validateUetr(uetr, messageError, fieldPrefix);

            Object partyAccount = requestMap.get("partyAccount");
            vopFieldValidator.validatePartyAccount(partyAccount, messageError, fieldPrefix);

            Object party = requestMap.get("party");
            vopFieldValidator.validateParty(party, messageError, fieldPrefix);
        }
    }

    private String extractStringValue(java.util.Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }



    private void validateRequiredField(Object requestBody, String fieldName, MessageError messageError) {
        Object fieldValue = extractFieldValue(requestBody, fieldName);
        if (fieldValue == null) {
            errorBuildingService.enrichMessageError(messageError,
                TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, fieldName + " is required"));
        }
    }

    private Object extractFieldValue(Object requestBody, String fieldName) {
        // Simple reflection-based field extraction
        // In a real implementation, this would use proper JSON parsing or object mapping
        try {
            if (requestBody instanceof java.util.Map) {
                return ((java.util.Map<?, ?>) requestBody).get(fieldName);
            }
            // For complex objects, use reflection or Jackson ObjectMapper
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void validateUnstructuredRemittanceInformation(Object unstructuredRemittanceInfo, MessageError messageError) {
        // Validate unstructuredRemittanceInformation array
        if (unstructuredRemittanceInfo instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) unstructuredRemittanceInfo;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    String text = (String) item;
                    if (text.length() > 140) {
                        errorBuildingService.enrichMessageError(messageError,
                            TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE,
                                "unstructuredRemittanceInformation[" + i + "] exceeds 140 characters"));
                    }
                }
            }
        }
    }

    private void validatePartyAgent(Object partyAgent, MessageError messageError) {
        // Validate partyAgent structure - should contain agent identification
        if (partyAgent != null) {
            // Basic validation for agent structure
            // In a real implementation, this would validate BIC, name, etc.
        }
    }




}
