/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisTppInfoValidatorTest {
    private static final String TPP_AUTHORITY_ID = "authority id";
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo DIFFERENT_TPP_INFO = buildTppInfo("different authorisation number");

    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");

    @Mock
    private TppInfoCheckerService tppInfoCheckerService;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private AisTppInfoValidator aisTppInfoValidator;

    @Before
    public void setUp() {
        when(tppInfoCheckerService.differsFromTppInRequest(TPP_INFO))
            .thenReturn(false);
        when(tppInfoCheckerService.differsFromTppInRequest(DIFFERENT_TPP_INFO))
            .thenReturn(true);

        when(requestProviderService.getRequestId())
            .thenReturn(X_REQUEST_ID);
    }

    @Test
    public void validateTpp_withSameTppInConsentAsInRequest_shouldReturnValid() {
        // When
        ValidationResult validationResult = aisTppInfoValidator.validateTpp(TPP_INFO);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateTpp_withDifferentTppInConsent_shouldReturnError() {
        // When
        ValidationResult validationResult = aisTppInfoValidator.validateTpp(DIFFERENT_TPP_INFO);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());

        MessageError expectedError = new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.UNAUTHORIZED, "Invalid TPP"));
        assertEquals(expectedError, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        return tppInfo;
    }
}
