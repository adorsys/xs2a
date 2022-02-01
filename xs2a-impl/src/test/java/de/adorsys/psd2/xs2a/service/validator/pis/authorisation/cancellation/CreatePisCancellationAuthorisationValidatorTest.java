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
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
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

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePisCancellationAuthorisationValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final PsuIdData PSU_ID_DATA = buildPsuIdData();

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(PIS_403, TppMessageInformation.of(PRODUCT_INVALID_FOR_PAYMENT));

    private static final MessageError PSU_CREDENTIALS_INVALID_ERROR =
        new MessageError(PIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));

    private static final MessageError STATUS_INVALID_ERROR =
        new MessageError(PIS_409, TppMessageInformation.of(STATUS_INVALID));

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;
    @Mock
    private AuthorisationStatusChecker authorisationStatusChecker;

    @InjectMocks
    private CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        createPisCancellationAuthorisationValidator.setPisValidators(pisTppInfoValidator);
    }

    @Test
    void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(false);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNull();
    }

    @Test
    void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        commonPaymentResponse.setPaymentProduct(WRONG_PAYMENT_PRODUCT);
        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PAYMENT_PRODUCT_VALIDATION_ERROR);
    }

    @Test
    void validate_withInvalidPsuIds_shouldReturnPsuCredentialsInvalidValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(true);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_CREDENTIALS_INVALID_ERROR);
    }

    @Test
    void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(TPP_VALIDATION_ERROR);
    }

    @Test
    void validate_withFinalisedAuthorisation_shouldReturnStatusInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponseWithPsuIdDataAndAuthorisation(TPP_INFO);
        CreatePisCancellationAuthorisationObject createPisCancellationAuthorisationObject =
            new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT);
        when(authorisationStatusChecker.isFinalised(any(PsuIdData.class), anyList(), eq(AuthorisationType.PIS_CANCELLATION))).thenReturn(true);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(createPisCancellationAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(createPisCancellationAuthorisationObject.getTppInfo());

        assertThat(validationResult).isNotNull();
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(STATUS_INVALID_ERROR);
    }

    @Test
    void buildWarningMessages() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponseWithPsuIdDataAndAuthorisation(TPP_INFO);
        CreatePisCancellationAuthorisationObject createPisCancellationAuthorisationObject =
            new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT);

        //When
        Set<TppMessageInformation> actual =
            createPisCancellationAuthorisationValidator.buildWarningMessages(createPisCancellationAuthorisationObject);

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

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", null, null, null, null);
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(SINGLE);
        return pisCommonPaymentResponse;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponseWithPsuIdDataAndAuthorisation(TppInfo tppInfo) {
        Authorisation authorisation = new Authorisation("1", PSU_ID_DATA, "consentId", AuthorisationType.PIS_CANCELLATION, ScaStatus.FINALISED);

        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse(tppInfo);
        pisCommonPaymentResponse.setAuthorisations(Collections.singletonList(authorisation));

        return pisCommonPaymentResponse;
    }
}
