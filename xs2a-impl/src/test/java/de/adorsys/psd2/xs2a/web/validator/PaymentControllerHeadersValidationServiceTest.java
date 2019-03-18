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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.common.TppRedirectUriValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_PREFERRED;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerHeadersValidationServiceTest {
    private static final String TPP_REDIRECT_PREFERRED_TRUE = "true";
    private static final String TPP_REDIRECT_URI_EXISTING = "TPP redirect URI";
    private static final String TPP_REDIRECT_URI_MISSING = null;
    private static final ErrorType ERROR_TYPE_PIS_400 = ErrorType.PIS_400;
    private static final MessageCategory MESSAGE_CATEGORY_ERROR = MessageCategory.ERROR;
    private static final MessageErrorCode MESSAGE_ERROR_CODE_FORMAT_ERROR = FORMAT_ERROR;

    @InjectMocks
    private PaymentControllerHeadersValidationService headersValidationService;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private TppRedirectUriValidationService tppRedirectUriValidationService;

    @Test
    public void validateCreateConsent_Failure_TppRedirectUriIsNotValid() {
        when(httpServletRequest.getHeader(TPP_REDIRECT_PREFERRED))
            .thenReturn(TPP_REDIRECT_PREFERRED_TRUE);

        when(httpServletRequest.getHeader(TPP_REDIRECT_URI))
            .thenReturn(TPP_REDIRECT_URI_MISSING);

        when(tppRedirectUriValidationService.isNotValid(Boolean.parseBoolean(TPP_REDIRECT_PREFERRED_TRUE), TPP_REDIRECT_URI_MISSING))
            .thenReturn(true);

        ValidationResult actualResult = headersValidationService.validateInitiatePayment();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.isNotValid()).isTrue();

        MessageError messageError = actualResult.getMessageError();

        assertThat(messageError).isNotNull();
        assertThat(messageError.getErrorType()).isEqualTo(ERROR_TYPE_PIS_400);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getCategory()).isEqualTo(MESSAGE_CATEGORY_ERROR);
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MESSAGE_ERROR_CODE_FORMAT_ERROR);
    }

    @Test
    public void validateCreateConsent_Success() {
        when(httpServletRequest.getHeader(TPP_REDIRECT_PREFERRED))
            .thenReturn(TPP_REDIRECT_PREFERRED_TRUE);

        when(httpServletRequest.getHeader(TPP_REDIRECT_URI))
            .thenReturn(TPP_REDIRECT_URI_EXISTING);

        when(tppRedirectUriValidationService.isNotValid(Boolean.parseBoolean(TPP_REDIRECT_PREFERRED_TRUE), TPP_REDIRECT_URI_EXISTING))
            .thenReturn(false);

        ValidationResult actualResult = headersValidationService.validateInitiatePayment();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.isValid()).isTrue();
    }
}
