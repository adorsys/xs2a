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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationValidatorTest {
    private static final String AUTHORISATION_ID = "62561aa4-5d69-4bac-9483-09376188eb78";
    private static final String UNKNOWN_AUTHORISATION_ID = "unknown id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu-id", null, null, null, null);

    private static final MessageError UNKNOWN_AUTHORISATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(RESOURCE_UNKNOWN_403));

    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private PisAuthorisationValidator pisAuthorisationValidator;

    @Test
    void validate_withValidAuthorisation_shouldReturnValid() {
        PisCommonPaymentResponse paymentResponse = buildPisCommonPaymentResponse(new Authorisation(AUTHORISATION_ID, PSU_ID_DATA, "paymentId", AuthorisationType.PIS_CREATION, SCA_STATUS));

        // When
        ValidationResult validationResult = pisAuthorisationValidator.validate(AUTHORISATION_ID, paymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withUnknownAuthorisationId_shouldReturnUnknownError() {
        // Given
        PisCommonPaymentResponse paymentResponse = buildPisCommonPaymentResponse(new Authorisation(AUTHORISATION_ID, PSU_ID_DATA, "paymentId", AuthorisationType.PIS_CREATION, SCA_STATUS));

        // When
        ValidationResult validationResult = pisAuthorisationValidator.validate(UNKNOWN_AUTHORISATION_ID, paymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(UNKNOWN_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(Authorisation authorisation) {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setAuthorisations(Collections.singletonList(authorisation));
        return response;
    }
}
