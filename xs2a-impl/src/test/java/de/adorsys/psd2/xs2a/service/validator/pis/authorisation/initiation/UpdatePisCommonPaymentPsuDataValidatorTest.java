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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationStatusValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.PIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePisCommonPaymentPsuDataValidatorTest {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final TransactionStatus REJECTED_TRANSACTION_STATUS = TransactionStatus.RJCT;
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String INVALID_AUTHORISATION_ID_FOR_ENDPOINT = "invalid authorisation id for endpoint";
    private static final String INVALID_AUTHORISATION_ID = "invalid authorisation id";

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_409, TppMessageInformation.of(STATUS_INVALID));
    private static final MessageError SCA_INVALID_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(SCA_INVALID));
    private static final MessageError EXPIRED_PAYMENT_ERROR = new MessageError(PIS_403, of(RESOURCE_EXPIRED_403));
    private static final MessageError BLOCKED_ENDPOINT_ERROR = new MessageError(PIS_403, of(SERVICE_BLOCKED));
    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(PRODUCT_INVALID_FOR_PAYMENT));
    private static final MessageError INVALID_AUTHORISATION_ERROR = new MessageError(PIS_403, of(RESOURCE_UNKNOWN_403));
    private static final MessageError FORMAT_BOTH_PSUS_ABSENT_ERROR = new MessageError(ErrorType.PIS_400, of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
    private static final MessageError CREDENTIALS_INVALID_ERROR = new MessageError(PIS_401, of(PSU_CREDENTIALS_INVALID));
    private static final MessageError PIS_SERVICE_INVALID = new MessageError(PIS_400, of(SERVICE_INVALID_400));

    private static final String CONFIRMATION_CODE = "confirmation code";
    private static final boolean CONFIRMATION_CODE_RECEIVED_FALSE = false;
    private static final boolean CONFIRMATION_CODE_RECEIVED_TRUE = true;

    private static final PsuIdData PSU_ID_DATA_1 = new PsuIdData("psu-id", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("psu-id-2", null, null, null, null);

    @Mock
    private PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator;
    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    @Mock
    private PisAuthorisationValidator pisAuthorisationValidator;
    @Mock
    private PisAuthorisationStatusValidator pisAuthorisationStatusValidator;
    @Mock
    private AuthorisationStageCheckValidator authorisationStageCheckValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @InjectMocks
    private UpdatePisCommonPaymentPsuDataValidator updatePisCommonPaymentPsuDataValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        updatePisCommonPaymentPsuDataValidator.setPisValidators(pisTppInfoValidator);
    }

    @Test
    void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(any(), any(), eq(PIS)))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED);
        commonPaymentResponse.setPaymentProduct(WRONG_PAYMENT_PRODUCT);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, INVALID_TPP_INFO, ScaStatus.RECEIVED);

        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAuthorisationForEndpoint_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID_FOR_ENDPOINT, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(false);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, INVALID_AUTHORISATION_ID_FOR_ENDPOINT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BLOCKED_ENDPOINT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withExpiredPaymentObject_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(EXPIRED_PAYMENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withExpiredPaymentObjectInAuthorisationConfirmation_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, TPP_INFO, ScaStatus.FAILED);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_TRUE))
            .thenReturn(true);
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(true);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate( new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1, CONFIRMATION_CODE)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(SCA_INVALID_ERROR,validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppAndExpiredPaymentAndInvalidAuthorisationForEndpoint_shouldReturnTppValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, INVALID_TPP_INFO, ScaStatus.RECEIVED);

        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, INVALID_AUTHORISATION_ID_FOR_ENDPOINT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAuthorisation_shouldReturnAuthorisationValidationError() {
        // Given
        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED);
        when(pisAuthorisationValidator.validate(INVALID_AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.invalid(INVALID_AUTHORISATION_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, INVALID_AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidScaStatus_shouldReturnStatusValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.FAILED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.FAILED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.invalid(STATUS_VALIDATION_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidScaStatusInConfirmationAuthorisationFlow_shouldReturnScaInvalid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.FAILED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.FAILED, CONFIRMATION_CODE_RECEIVED_TRUE))
            .thenReturn(ValidationResult.invalid(SCA_INVALID_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_TRUE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1, CONFIRMATION_CODE)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(SCA_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNoAuthorisation_shouldReturnAuthorisationValidationError() {
        //Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED, INVALID_AUTHORISATION_ID, PSU_ID_DATA_1);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        //When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withBothPsusAbsent_shouldReturnFormatError() {
        //Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED, AUTHORISATION_ID, null);
        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);

        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, null))
            .thenReturn(ValidationResult.invalid(FORMAT_BOTH_PSUS_ABSENT_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, psuIdData)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(FORMAT_BOTH_PSUS_ABSENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_cantPsuUpdateAuthorisation_shouldReturnCredentialsInvalidError() {
        //Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED, AUTHORISATION_ID, PSU_ID_DATA_2);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_2))
            .thenReturn(ValidationResult.invalid(CREDENTIALS_INVALID_ERROR));

        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_received() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.RECEIVED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1), ScaStatus.RECEIVED, PIS))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_psuidentified() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.PSUIDENTIFIED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.PSUIDENTIFIED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1), ScaStatus.PSUIDENTIFIED, PIS))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_psuauthenticated() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.PSUAUTHENTICATED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.PSUAUTHENTICATED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1), ScaStatus.PSUAUTHENTICATED, PIS))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_scamethodselected() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.SCAMETHODSELECTED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(ScaStatus.SCAMETHODSELECTED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1), ScaStatus.SCAMETHODSELECTED, PIS))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse =
            buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO, ScaStatus.SCAMETHODSELECTED);
        UpdatePaymentPsuDataPO updatePaymentPsuDataPO =
            buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID);

        //When
        Set<TppMessageInformation> actual =
            updatePisCommonPaymentPsuDataValidator.buildWarningMessages(updatePaymentPsuDataPO);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(pisPsuDataUpdateAuthorisationCheckerValidator);
        verifyNoInteractions(pisTppInfoValidator);
        verifyNoInteractions(pisEndpointAccessCheckerService);
        verifyNoInteractions(pisAuthorisationValidator);
        verifyNoInteractions(pisAuthorisationStatusValidator);
        verifyNoInteractions(authorisationStageCheckValidator);
        verifyNoInteractions(aspspProfileService);
    }


    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TransactionStatus transactionStatus, TppInfo tppInfo, ScaStatus scaStatus) {
        return buildPisCommonPaymentResponse(transactionStatus, tppInfo, scaStatus, AUTHORISATION_ID, PSU_ID_DATA_1);
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TransactionStatus transactionStatus, TppInfo tppInfo, ScaStatus scaStatus, String authorisationId, PsuIdData psuIdData) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTransactionStatus(transactionStatus);
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentResponse.setAuthorisations(buildAuthorisation(scaStatus, authorisationId, psuIdData));
        return pisCommonPaymentResponse;
    }

    private UpdatePaymentPsuDataPO buildUpdatePisCommonPaymentPsuDataPO(PisCommonPaymentResponse commonPaymentResponse, String authorisationId) {
        return new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(authorisationId, PSU_ID_DATA_1));
    }

    private PaymentAuthorisationParameters buildUpdateRequest(String authoridsationId, PsuIdData psuIdData) {
        PaymentAuthorisationParameters result = new PaymentAuthorisationParameters();
        result.setAuthorisationId(authoridsationId);
        result.setPsuData(psuIdData);
        result.setPaymentService(PaymentType.SINGLE);
        result.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        return result;
    }

    private PaymentAuthorisationParameters buildUpdateRequest(String authorisationId, PsuIdData psuIdData, String confirmationCode) {
        PaymentAuthorisationParameters result = buildUpdateRequest(authorisationId, psuIdData);
        result.setConfirmationCode(confirmationCode);
        return result;
    }

    private List<Authorisation> buildAuthorisation(ScaStatus scaStatus, String authorisationId, PsuIdData psuIdData) {
        return Collections.singletonList(new Authorisation(authorisationId, psuIdData, "paymentID", AuthorisationType.PIS_CREATION, scaStatus));
    }
}
