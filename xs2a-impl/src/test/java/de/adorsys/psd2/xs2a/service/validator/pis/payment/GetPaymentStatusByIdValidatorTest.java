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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.OauthPaymentValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPaymentStatusByIdValidatorTest {
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PaymentType INVALID_PAYMENT_TYPE = PaymentType.BULK;
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String INVALID_PAYMENT_PRODUCT = "invalid payment product";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String JSON_ACCEPT_HEADER = "application/json";
    private static final String XML_ACCEPT_HEADER = "application/xml";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError PAYMENT_TYPE_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_405, TppMessageInformation.of(SERVICE_INVALID_405_FOR_PAYMENT));
    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(PRODUCT_INVALID_FOR_PAYMENT));
    private static final MessageError ACCEPT_HEADER_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_406, TppMessageInformation.of(REQUESTED_FORMATS_INVALID));

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private OauthPaymentValidator oauthPaymentValidator;
    @Mock
    private TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator;

    @InjectMocks
    private GetPaymentStatusByIdValidator getPaymentStatusByIdValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getPaymentStatusByIdValidator.setPisValidators(pisTppInfoValidator);
    }

    @Test
    void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(oauthPaymentValidator.validate(commonPaymentResponse))
            .thenReturn(ValidationResult.valid());

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(requestProviderService.getAcceptHeader()).thenReturn(JSON_ACCEPT_HEADER);

        when(transactionStatusAcceptHeaderValidator.validate(JSON_ACCEPT_HEADER))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT));

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
        when(oauthPaymentValidator.validate(commonPaymentResponse))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(requestProviderService.getAcceptHeader()).thenReturn(JSON_ACCEPT_HEADER);

        when(transactionStatusAcceptHeaderValidator.validate(JSON_ACCEPT_HEADER))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
    }

    @Test
    void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidPaymentObject_shouldReturnGetCommonPaymentValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, INVALID_PAYMENT_TYPE, PAYMENT_PRODUCT));

        // Then

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_TYPE_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppAndPaymentObject_shouldReturnTypeAndProductValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT));

        // Then

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withWrongPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, WRONG_PAYMENT_PRODUCT));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNotSupportedAcceptHeader_shouldReturnAcceptHeaderValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(requestProviderService.getAcceptHeader()).thenReturn(XML_ACCEPT_HEADER);

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(transactionStatusAcceptHeaderValidator.validate(XML_ACCEPT_HEADER))
            .thenReturn(ValidationResult.invalid(ACCEPT_HEADER_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCEPT_HEADER_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        GetPaymentStatusByIdPO getPaymentStatusByIdPO =
            new GetPaymentStatusByIdPO(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        //When
        Set<TppMessageInformation> actual = getPaymentStatusByIdValidator.buildWarningMessages(getPaymentStatusByIdPO);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(pisTppInfoValidator);
        verifyNoInteractions(requestProviderService);
        verifyNoInteractions(oauthPaymentValidator);
        verifyNoInteractions(transactionStatusAcceptHeaderValidator);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PAYMENT_TYPE);
        return pisCommonPaymentResponse;
    }
}
