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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.PisAuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePisAuthorisationValidatorTest {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final TransactionStatus REJECTED_TRANSACTION_STATUS = TransactionStatus.RJCT;
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");

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

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";
    private static final PsuIdData PSU_DATA_1 = new PsuIdData("FIRST PSU ID", null, null, null, null);
    private static final PsuIdData PSU_DATA_2 = new PsuIdData("SECOND PSU ID", null, null, null, null);

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;
    @Mock
    private PisAuthorisationStatusChecker pisAuthorisationStatusChecker;

    @InjectMocks
    private CreatePisAuthorisationValidator createPisAuthorisationValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        createPisAuthorisationValidator.setPisValidators(pisTppInfoValidator);

        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);

        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
    }

    @Test
    public void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, WRONG_PAYMENT_PRODUCT, null));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPaymentObject_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, TPP_INFO);

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(EXPIRED_PAYMENT_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPsuData_shouldReturnCredentialsInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA_1));
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(true);

        // When
        CreatePisAuthorisationObject createPisAuthorisationObject = new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, PSU_DATA_2);
        ValidationResult validationResult = createPisAuthorisationValidator.validate(createPisAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PSU_CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withDifferentPsuData_multilevelSca_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA_1));
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(false);

        // When
        CreatePisAuthorisationObject createPisAuthorisationObject = new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, PSU_DATA_2);
        ValidationResult validationResult = createPisAuthorisationValidator.validate(createPisAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withFinalisedAuthorisation_shouldReturnStatusInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA_1));
        commonPaymentResponse.setAuthorisations(Collections.singletonList(new Authorisation("1", ScaStatus.FINALISED, PSU_DATA_1, PaymentAuthorisationType.CREATED)));
        when(pisAuthorisationStatusChecker.isFinalised(any(PsuIdData.class), anyList())).thenReturn(true);

        // When
        CreatePisAuthorisationObject createPisAuthorisationObject = new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, PSU_DATA_1);
        ValidationResult validationResult = createPisAuthorisationValidator.validate(createPisAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppAndPaymentObject_shouldReturnTppValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(commonPaymentResponse, SINGLE, CORRECT_PAYMENT_PRODUCT, null));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
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
