/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TppRedirectUriBodyValidatorImplTest {

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private MessageError messageError;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        tppRedirectUriBodyValidator = new TppRedirectUriBodyValidatorImpl(scaApproachResolver, aspspProfileServiceWrapper, errorBuildingService);
        request = new MockHttpServletRequest();
    }

    @Test
    void validate_NotRedirect_success() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_RedirectApproach_success() {
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.REDIRECT);

        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        request.addHeader(TPP_REDIRECT_URI, "some.url");

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_oAuthApproach_success() {
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver).resolveScaApproach();
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_RedirectApproachAndRedirectPreferredHeaderTrue_TppRedirectUriIsBotContain_error() {
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.REDIRECT);

        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_RedirectApproachAndRedirectPreferredHeaderTrue_TppRedirectUriIsBlank_error() {
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.REDIRECT);

        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        request.addHeader(TPP_REDIRECT_URI, "");

        tppRedirectUriBodyValidator.validate(request, messageError);

        verify(scaApproachResolver, times(1)).resolveScaApproach();
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
    }
}
