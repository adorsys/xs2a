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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.PisAuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.PisCancellationAuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePisCancellationAuthorisationValidatorTest {
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
    RequestProviderService requestProviderService;
    @Mock
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;
    @Mock
    private PisCancellationAuthorisationStatusChecker pisCancellationAuthorisationStatusChecker;

    @InjectMocks
    private CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        createPisCancellationAuthorisationValidator.setPisValidators(pisTppInfoValidator);

        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
    }

    @Test
    public void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(false);
        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        commonPaymentResponse.setPaymentProduct(WRONG_PAYMENT_PRODUCT);
        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPsuIds_shouldReturnPsuCredentialsInvalidValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(true);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PSU_CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withFinalisedAuthorisation_shouldReturnStatusInvalidError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponseWithPsuIdDataAndAuthorisation(TPP_INFO);
        CreatePisCancellationAuthorisationObject createPisCancellationAuthorisationObject = new CreatePisCancellationAuthorisationObject(commonPaymentResponse, PSU_ID_DATA, SINGLE, CORRECT_PAYMENT_PRODUCT);
        when(pisCancellationAuthorisationStatusChecker.isFinalised(any(PsuIdData.class), anyList())).thenReturn(true);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(createPisCancellationAuthorisationObject);

        // Then
        verify(pisTppInfoValidator).validateTpp(createPisCancellationAuthorisationObject.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_INVALID_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", null, null, null);
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(SINGLE);
        return pisCommonPaymentResponse;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponseWithPsuIdDataAndAuthorisation(TppInfo tppInfo) {
        Authorisation authorisation = new Authorisation("1", ScaStatus.FINALISED, PSU_ID_DATA, PaymentAuthorisationType.CREATED);

        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse(tppInfo);
        pisCommonPaymentResponse.setAuthorisations(Collections.singletonList(authorisation));

        return pisCommonPaymentResponse;
    }

}
