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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.OauthPaymentValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPaymentCancellationAuthorisationScaStatusValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String AUTHORISATION_ID = "random";
    private static final String WRONG_AUTHORISATION_ID = "random but wrong";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(PRODUCT_INVALID_FOR_PAYMENT));
    private static final MessageError AUTHORISATION_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(RESOURCE_UNKNOWN_403));

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    @Mock
    private PisAuthorisationValidator pisAuthorisationValidator;

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;

    @Mock
    private RequestProviderService requestProviderService;

    @Mock
    private OauthPaymentValidator oauthPaymentValidator;

    @InjectMocks
    private GetPaymentCancellationAuthorisationScaStatusValidator getPaymentCancellationAuthorisationScaStatusValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getPaymentCancellationAuthorisationScaStatusValidator.setPisValidators(pisTppInfoValidator);
    }

    @Test
    void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(oauthPaymentValidator.validate(commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        // When
        ValidationResult validationResult = getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(commonPaymentResponse, AUTHORISATION_ID, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidPaymentObject_oauth_error() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(oauthPaymentValidator.validate(commonPaymentResponse))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
        // When
        ValidationResult validationResult = getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(commonPaymentResponse, AUTHORISATION_ID, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
    }

    @Test
    void validate_withInvalidAuthorisation_shouldReturnAuthorisationValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(pisAuthorisationValidator.validate(WRONG_AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.invalid(AUTHORISATION_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(commonPaymentResponse, WRONG_AUTHORISATION_ID, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AUTHORISATION_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        // When
        ValidationResult validationResult = getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(commonPaymentResponse, AUTHORISATION_ID, SINGLE, WRONG_PAYMENT_PRODUCT));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult =
            getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(commonPaymentResponse, AUTHORISATION_ID, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);
        GetPaymentCancellationAuthorisationScaStatusPO getPaymentCancellationAuthorisationScaStatusPO =
            new GetPaymentCancellationAuthorisationScaStatusPO(commonPaymentResponse, AUTHORISATION_ID, SINGLE, CORRECT_PAYMENT_PRODUCT);

        //When
        Set<TppMessageInformation> actual =
            getPaymentCancellationAuthorisationScaStatusValidator.buildWarningMessages(getPaymentCancellationAuthorisationScaStatusPO);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(pisAuthorisationValidator);
        verifyNoInteractions(pisTppInfoValidator);
        verifyNoInteractions(requestProviderService);
        verifyNoInteractions(oauthPaymentValidator);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(SINGLE);
        return pisCommonPaymentResponse;
    }
}
