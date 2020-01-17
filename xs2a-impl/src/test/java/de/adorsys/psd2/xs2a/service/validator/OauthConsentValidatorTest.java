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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORBIDDEN;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OauthConsentValidatorTest {
    private static final String TOKEN = "token";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.AIS_403, of(FORBIDDEN));

    @InjectMocks private OauthConsentValidator oauthConsentValidator;
    @Mock private RequestProviderService requestProviderService;
    @Mock private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock private ScaApproachResolver scaApproachResolver;

    @Test
    public void validate_invalid_tokenEmpty_approachRedirect_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        AccountConsent accountConsent = buildAccountConsent(ConsentStatus.VALID);
        when(requestProviderService.getOAuth2Token()).thenReturn("");
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(accountConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(MESSAGE_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_invalid_tokenNull_approachRedirect_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        AccountConsent accountConsent = buildAccountConsent(ConsentStatus.VALID);
        when(requestProviderService.getOAuth2Token()).thenReturn(null);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(accountConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(MESSAGE_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_valid_tokenPresent_approachRedirect_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        AccountConsent accountConsent = buildAccountConsent(ConsentStatus.VALID);
        when(requestProviderService.getOAuth2Token()).thenReturn(TOKEN);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(accountConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_valid_approachRedirect_flowOauthPreStep_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH_PRE_STEP);
        AccountConsent accountConsent = buildAccountConsent(ConsentStatus.VALID);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(accountConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_valid_approachEmbedded_flowOauth_statusValid() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        AccountConsent accountConsent = buildAccountConsent(ConsentStatus.VALID);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(accountConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_valid_approachRedirect_flowOauth_statusReceived() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        AccountConsent accountConsent = buildAccountConsent(ConsentStatus.RECEIVED);
        //When
        ValidationResult validationResult = oauthConsentValidator.validate(accountConsent);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    private AccountConsent buildAccountConsent(ConsentStatus consentStatus) {
        return new AccountConsent("id", null, null, false, null, 0,
                                  null, consentStatus, false, false,
                                  Collections.emptyList(), null, null, false,
                                  Collections.emptyList(), null, Collections.emptyMap(), OffsetDateTime.now());
    }
}
