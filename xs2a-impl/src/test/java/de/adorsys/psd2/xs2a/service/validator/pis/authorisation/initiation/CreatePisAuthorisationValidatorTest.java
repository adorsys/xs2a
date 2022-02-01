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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePisAuthorisationValidatorTest {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final TransactionStatus REJECTED_TRANSACTION_STATUS = TransactionStatus.RJCT;
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError EXPIRED_PAYMENT_ERROR =
        new MessageError(PIS_403, of(RESOURCE_EXPIRED_403));
    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(PIS_403, TppMessageInformation.of(PRODUCT_INVALID_FOR_PAYMENT));
    private static final MessageError PSU_CREDENTIALS_INVALID_ERROR =
        new MessageError(PIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));
    private static final MessageError STATUS_INVALID_ERROR =
        new MessageError(PIS_409, TppMessageInformation.of(STATUS_INVALID));
    private static final MessageError STATUS_INVALID_ERROR_MESSAGE =
        new MessageError(PIS_400, TppMessageInformation.of(STATUS_INVALID));
    private static final MessageError RESOURCE_BLOCKED_SB_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(RESOURCE_BLOCKED_SB));

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";
    private static final PsuIdData PSU_DATA_1 = new PsuIdData("FIRST PSU ID", null, null, null, null);
    private static final PsuIdData PSU_DATA_2 = new PsuIdData("SECOND PSU ID", null, null, null, null);

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;
    @Mock
    private AuthorisationStatusChecker authorisationStatusChecker;

    @InjectMocks
    private CreatePisAuthorisationValidator createPisAuthorisationValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        createPisAuthorisationValidator.setPisValidators(pisTppInfoValidator);
    }

    @Test
    void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNull();
    }

    @Test
    void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, WRONG_PAYMENT_PRODUCT, null));

        // Then
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PAYMENT_PRODUCT_VALIDATION_ERROR);
    }

    @Test
    void validate_paymentIsBlocked_shouldReturnResourceBlockedInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setSigningBasketBlocked(true);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(RESOURCE_BLOCKED_SB_ERROR);
    }

    @Test
    void validate_paymentIsBlocked_shouldReturnStatusInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setSigningBasketAuthorised(true);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verifyNoInteractions(authorisationPsuDataChecker, authorisationStatusChecker);

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(STATUS_INVALID_ERROR_MESSAGE);
    }

    @Test
    void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, INVALID_TPP_INFO);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(TPP_VALIDATION_ERROR);
    }

    @Test
    void validate_withInvalidPaymentObject_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, TPP_INFO);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(EXPIRED_PAYMENT_ERROR);
    }

    @Test
    void validate_withInvalidPsuData_shouldReturnCredentialsInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA_1));
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(true);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        CreatePisAuthorisationObject createPisAuthorisationObject = new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, PSU_DATA_2);
        ValidationResult validationResult = createPisAuthorisationValidator.validate(createPisAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_CREDENTIALS_INVALID_ERROR);
    }

    @Test
    void validate_withDifferentPsuData_multilevelSca_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA_1));
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(false);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        CreatePisAuthorisationObject createPisAuthorisationObject = new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, PSU_DATA_2);
        ValidationResult validationResult = createPisAuthorisationValidator.validate(createPisAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNull();
    }

    @Test
    void validate_withFinalisedAuthorisation_shouldReturnStatusInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA_1));
        commonPaymentResponse.setAuthorisations(Collections.singletonList(new Authorisation("1", PSU_DATA_1, "paymentID", AuthorisationType.PIS_CREATION, ScaStatus.FINALISED)));
        when(authorisationStatusChecker.isFinalised(any(PsuIdData.class), anyList(), eq(AuthorisationType.PIS_CREATION))).thenReturn(true);
        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        CreatePisAuthorisationObject createPisAuthorisationObject = new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, PSU_DATA_1);
        ValidationResult validationResult = createPisAuthorisationValidator.validate(createPisAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(STATUS_INVALID_ERROR);
    }

    @Test
    void validate_withInvalidTppAndPaymentObject_shouldReturnTppValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, INVALID_TPP_INFO);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(TPP_VALIDATION_ERROR);
    }

    @Test
    void buildWarningMessages() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, INVALID_TPP_INFO);
        CreatePisAuthorisationObject createPisAuthorisationObject =
            new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null);

        //When
        Set<TppMessageInformation> actual = createPisAuthorisationValidator.buildWarningMessages(createPisAuthorisationObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(pisTppInfoValidator);
        verifyNoInteractions(authorisationPsuDataChecker);
        verifyNoInteractions(authorisationStatusChecker);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TransactionStatus transactionStatus, TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTransactionStatus(transactionStatus);
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(SINGLE);
        return pisCommonPaymentResponse;
    }
}
