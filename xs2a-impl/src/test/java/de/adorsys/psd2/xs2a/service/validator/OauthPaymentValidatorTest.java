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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORBIDDEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OauthPaymentValidatorTest {
    private static final String TOKEN = "token";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIS_403, of(FORBIDDEN));
    private static final List<TransactionStatus> ALLOWED_WITHOUT_TOKEN = Stream.of(TransactionStatus.RCVD, TransactionStatus.PDNG, TransactionStatus.PATC).collect(Collectors.toList());
    private static final List<TransactionStatus> NOT_ALLOWED_WITHOUT_TOKEN = Stream.of(TransactionStatus.values()).filter(status -> !ALLOWED_WITHOUT_TOKEN.contains(status)).collect(Collectors.toList());
    private static final List<TransactionStatus> ALL_TRANSACTION_STATUSES = Stream.of(TransactionStatus.values()).collect(Collectors.toList());

    @InjectMocks
    private OauthPaymentValidator oauthPaymentValidator;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;

    @Test
    void validate_invalid_tokenEmpty_approachRedirect_flowOauth() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(requestProviderService.getOAuth2Token()).thenReturn("");
        NOT_ALLOWED_WITHOUT_TOKEN.forEach(this::testForInvalid);
        ALLOWED_WITHOUT_TOKEN.forEach(this::testForValid);
    }

    @Test
    void validate_invalid_tokenNull_approachRedirect_flowOauth() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(requestProviderService.getOAuth2Token()).thenReturn(null);
        NOT_ALLOWED_WITHOUT_TOKEN.forEach(this::testForInvalid);
        ALLOWED_WITHOUT_TOKEN.forEach(this::testForValid);
    }

    @Test
    void validate_valid_tokenPresent_approachRedirect_flowOauth() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(requestProviderService.getOAuth2Token()).thenReturn(TOKEN);
        ALL_TRANSACTION_STATUSES.forEach(this::testForValid);
    }

    @Test
    void validate_valid_approachRedirect_flowOauthPreStep() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH_PRE_STEP);
        ALL_TRANSACTION_STATUSES.forEach(this::testForValid);
    }

    @Test
    void validate_valid_approachEmbedded_flowOauth() {
        //Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        ALL_TRANSACTION_STATUSES.forEach(this::testForValid);
    }

    private void testForInvalid(TransactionStatus status) {
        //When
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse(status);
        ValidationResult validationResult = oauthPaymentValidator.validate(pisCommonPaymentResponse);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(MESSAGE_ERROR, validationResult.getMessageError());
    }

    private void testForValid(TransactionStatus status) {
        //When
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse(status);
        ValidationResult validationResult = oauthPaymentValidator.validate(pisCommonPaymentResponse);
        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TransactionStatus transactionStatus) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTransactionStatus(transactionStatus);
        return pisCommonPaymentResponse;

    }
}
