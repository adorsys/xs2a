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

package de.adorsys.psd2.xs2a.web.validator.methods.factory;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.methods.service.PsuIpAddressValidationService;
import de.adorsys.psd2.xs2a.web.validator.methods.service.TppRedirectUriValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_PREFERRED;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InitiatePaymentValidatorTest {

    @InjectMocks
    private InitiatePaymentValidator initiatePaymentValidator;

    @Mock
    private ErrorBuildingService errorBuildingService;

    @Mock
    private TppRedirectUriValidationService tppRedirectUriValidationService;

    @Mock
    private PsuIpAddressValidationService psuIpAddressValidationService;

    @Test
    public void validate_validSuccess() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(TPP_REDIRECT_PREFERRED, true);
        request.addHeader(TPP_REDIRECT_URI, "Any URI");

        when(psuIpAddressValidationService.validatePsuIdAddress(anyString()))
            .thenReturn(ValidationResult.valid());
        when(tppRedirectUriValidationService.isNotValid(anyBoolean(), any(String.class)))
            .thenReturn(false);

        // When
        boolean actual = initiatePaymentValidator.validate(request, response);

        // Then
        assertTrue(actual);
    }

    @Test
    public void validate_invalidByIpSuccess() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(TPP_REDIRECT_PREFERRED, true);

        when(psuIpAddressValidationService.validatePsuIdAddress(anyString()))
            .thenReturn(ValidationResult.invalid(new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))));

        when(tppRedirectUriValidationService.isNotValid(anyBoolean(), any(String.class)))
            .thenReturn(true);

        // When
        boolean actual = initiatePaymentValidator.validate(request, response);

        // Then
        assertFalse(actual);
    }

    @Test
    public void validate_invalidByUriSuccess() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(TPP_REDIRECT_PREFERRED, true);

        when(psuIpAddressValidationService.validatePsuIdAddress(anyString()))
            .thenReturn(ValidationResult.valid());

        when(tppRedirectUriValidationService.isNotValid(anyBoolean(), any(String.class)))
            .thenReturn(true);

        // When
        boolean actual = initiatePaymentValidator.validate(request, response);

        // Then
        assertFalse(actual);
    }


}
