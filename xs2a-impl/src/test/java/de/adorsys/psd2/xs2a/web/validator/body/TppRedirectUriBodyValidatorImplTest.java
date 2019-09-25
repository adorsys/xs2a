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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TppRedirectUriBodyValidatorImplTest {

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private MessageError messageError;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        tppRedirectUriBodyValidator = new TppRedirectUriBodyValidatorImpl(scaApproachResolver, errorBuildingService);
        request = new MockHttpServletRequest();
    }

    @Test
    public void validate_NotRedirect_success() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_RedirectApproach_success() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        request.addHeader(TPP_REDIRECT_URI, "some.url");

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_RedirectApproachAndRedirectPreferredHeaderTrue_TppRedirectUriIsBotContain_error() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    public void validate_RedirectApproachAndRedirectPreferredHeaderTrue_TppRedirectUriIsBlank_error() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        request.addHeader(TPP_REDIRECT_URI, "");

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
    }
}
