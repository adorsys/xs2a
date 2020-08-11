/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORBIDDEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OauthPiisConsentValidatorTest {
    private static final String TOKEN = "token";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIIS_403, of(FORBIDDEN));

    @InjectMocks
    private OauthPiisConsentValidator oauthConsentValidator;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;

    @Test
    void validate_invalid_tokenEmpty_approachRedirect_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.VALID);
        when(requestProviderService.getOAuth2Token()).thenReturn("");
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(piisConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(MESSAGE_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_invalid_tokenNull_approachRedirect_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.VALID);
        when(requestProviderService.getOAuth2Token()).thenReturn(null);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(piisConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(MESSAGE_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_valid_tokenPresent_approachRedirect_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.VALID);
        when(requestProviderService.getOAuth2Token()).thenReturn(TOKEN);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(piisConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_valid_approachRedirect_flowOauthPreStep_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH_PRE_STEP);
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.VALID);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(piisConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_valid_approachEmbedded_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.VALID);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(piisConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_valid_approachRedirect_flowOauth_statusReceived() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.RECEIVED);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(piisConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    private PiisConsent buildPiisConsent(ConsentStatus consentStatus) {
        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setConsentStatus(consentStatus);
        return piisConsent;
    }
}
