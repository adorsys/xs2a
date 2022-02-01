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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
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
