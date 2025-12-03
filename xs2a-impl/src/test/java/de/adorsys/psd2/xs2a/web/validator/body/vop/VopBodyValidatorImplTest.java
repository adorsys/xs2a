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
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for VopBodyValidatorImpl to verify correct URI pattern matching
 * and validation logic selection for bulk vs single party verification requests.
 */
@ExtendWith(MockitoExtension.class)
class VopBodyValidatorImplTest {

    @Mock
    private ErrorBuildingService errorBuildingService;

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @Mock
    private FieldLengthValidator fieldLengthValidator;

    @Mock
    private FieldExtractor fieldExtractor;

    @Mock
    private VopFieldValidator vopFieldValidator;

    @Mock
    private HttpServletRequest request;

    private VopBodyValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new VopBodyValidatorImpl(
            errorBuildingService,
            xs2aObjectMapper,
            fieldLengthValidator,
            fieldExtractor,
            vopFieldValidator
        );
    }

    @Test
    void validate_bulkPayeeVerifications_shouldUseBulkValidation() throws Exception {
        // Given: A request to bulk-payee-verifications endpoint
        when(request.getRequestURI()).thenReturn("/v1/bulk-payee-verifications");

        java.util.Map<String, Object> bulkRequestMap = new java.util.HashMap<>();
        bulkRequestMap.put("partyVerificationRequests", new java.util.ArrayList<>());
        when(fieldExtractor.mapBodyToInstance(eq(request), any(MessageError.class), eq(Object.class)))
            .thenReturn(Optional.of(bulkRequestMap));

        MessageError messageError = new MessageError();

        // When: Validation is performed
        validator.validate(request, messageError);

        // Then: Bulk validation is used (validateBulkSize is called)
        verify(vopFieldValidator).validateBulkSize(any(), any());
    }

    @Test
    void validate_bulkPayerVerifications_shouldUseBulkValidation() throws Exception {
        // Given: A request to bulk-payer-verifications endpoint
        when(request.getRequestURI()).thenReturn("/v1/bulk-payer-verifications");

        java.util.Map<String, Object> bulkRequestMap = new java.util.HashMap<>();
        bulkRequestMap.put("partyVerificationRequests", new java.util.ArrayList<>());
        when(fieldExtractor.mapBodyToInstance(eq(request), any(MessageError.class), eq(Object.class)))
            .thenReturn(Optional.of(bulkRequestMap));

        MessageError messageError = new MessageError();

        // When: Validation is performed
        validator.validate(request, messageError);

        // Then: Bulk validation is used
        verify(vopFieldValidator).validateBulkSize(any(), any());
    }

    @Test
    void validate_bulkPartyVerifications_shouldUseBulkValidation() throws Exception {
        // Given: A request to bulk-party-verifications endpoint
        when(request.getRequestURI()).thenReturn("/v1/bulk-party-verifications");

        java.util.Map<String, Object> bulkRequestMap = new java.util.HashMap<>();
        bulkRequestMap.put("partyVerificationRequests", new java.util.ArrayList<>());
        when(fieldExtractor.mapBodyToInstance(eq(request), any(MessageError.class), eq(Object.class)))
            .thenReturn(Optional.of(bulkRequestMap));

        MessageError messageError = new MessageError();

        // When: Validation is performed
        validator.validate(request, messageError);

        // Then: Bulk validation is used
        verify(vopFieldValidator).validateBulkSize(any(), any());
    }

    @Test
    void validate_singlePayeeVerifications_shouldUseSingleValidation() throws Exception {
        // Given: A request to payee-verifications endpoint (single, not bulk)
        when(request.getRequestURI()).thenReturn("/v1/payee-verifications");

        java.util.Map<String, Object> singleRequestMap = new java.util.HashMap<>();
        singleRequestMap.put("partyAccount", new java.util.HashMap<>());
        singleRequestMap.put("party", new java.util.HashMap<>());
        when(fieldExtractor.mapBodyToInstance(eq(request), any(MessageError.class), eq(Object.class)))
            .thenReturn(Optional.of(singleRequestMap));

        MessageError messageError = new MessageError();

        // When: Validation is performed
        validator.validate(request, messageError);

        // Then: Single validation is used (validatePartyAccount is called for root-level fields)
        verify(vopFieldValidator).validatePartyAccount(any(), any(), eq("request"));
        verify(vopFieldValidator).validateParty(any(), any(), eq("request"));
        // Bulk validation should NOT be called
        verify(vopFieldValidator, never()).validateBulkSize(any(), any());
    }

    @Test
    void validate_singlePartyVerifications_shouldUseSingleValidation() throws Exception {
        // Given: A request to party-verifications endpoint (single, not bulk)
        when(request.getRequestURI()).thenReturn("/v1/party-verifications");

        java.util.Map<String, Object> singleRequestMap = new java.util.HashMap<>();
        singleRequestMap.put("partyAccount", new java.util.HashMap<>());
        singleRequestMap.put("party", new java.util.HashMap<>());
        when(fieldExtractor.mapBodyToInstance(eq(request), any(MessageError.class), eq(Object.class)))
            .thenReturn(Optional.of(singleRequestMap));

        MessageError messageError = new MessageError();

        // When: Validation is performed
        validator.validate(request, messageError);

        // Then: Single validation is used
        verify(vopFieldValidator).validatePartyAccount(any(), any(), eq("request"));
        verify(vopFieldValidator).validateParty(any(), any(), eq("request"));
        verify(vopFieldValidator, never()).validateBulkSize(any(), any());
    }

    @Test
    void validate_bulkRequest_withValidStructure_shouldValidateEachItem() throws Exception {
        // Given: A valid bulk request with multiple items
        when(request.getRequestURI()).thenReturn("/v1/bulk-party-verifications");

        // Create a bulk request with 2 items
        java.util.List<Object> requests = new java.util.ArrayList<>();
        java.util.Map<String, Object> item1 = new java.util.HashMap<>();
        item1.put("uetr", "99435c7e-ad88-49ec-a2ad-99ddcb1f5555");
        item1.put("partyAccount", new java.util.HashMap<>());
        item1.put("party", new java.util.HashMap<>());
        requests.add(item1);

        java.util.Map<String, Object> item2 = new java.util.HashMap<>();
        item2.put("uetr", "88435c7e-ad88-49ec-a2ad-99ddcb1f6666");
        item2.put("partyAccount", new java.util.HashMap<>());
        item2.put("party", new java.util.HashMap<>());
        requests.add(item2);

        java.util.Map<String, Object> bulkRequestMap = new java.util.HashMap<>();
        bulkRequestMap.put("partyVerificationRequests", requests);

        when(fieldExtractor.mapBodyToInstance(eq(request), any(MessageError.class), eq(Object.class)))
            .thenReturn(Optional.of(bulkRequestMap));

        MessageError messageError = new MessageError();

        // When: Validation is performed
        validator.validate(request, messageError);

        // Then: Bulk size validation is called
        verify(vopFieldValidator).validateBulkSize(eq(requests), any());

        // And: Each item is validated (2 times for 2 items)
        verify(vopFieldValidator, times(2)).validateUetr(anyString(), any(), anyString());
        verify(vopFieldValidator, times(2)).validatePartyAccount(any(), any(), anyString());
        verify(vopFieldValidator, times(2)).validateParty(any(), any(), anyString());
    }
}
