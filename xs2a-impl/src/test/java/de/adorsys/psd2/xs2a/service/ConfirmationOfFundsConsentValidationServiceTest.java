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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.piis.*;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationOfFundsConsentValidationServiceTest {
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String AUTHORISATION_ID = "authorisation ud";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);

    @InjectMocks
    private ConfirmationOfFundsConsentValidationService service;
    @Mock
    private DeleteConfirmationOfFundsConsentByIdValidator deleteConfirmationOfFundsConsentByIdValidator;
    @Mock
    private CreatePiisConsentAuthorisationValidator createPiisConsentAuthorisationValidator;
    @Mock
    private GetConfirmationOfFundsConsentAuthorisationsValidator getConfirmationOfFundsConsentAuthorisationsValidator;
    @Mock
    private GetConfirmationOfFundsConsentAuthorisationScaStatusValidator getConfirmationOfFundsConsentAuthorisationScaStatusValidator;
    @Mock
    private PiisConsent piisConsent;

    @Test
    void validateConsentOnDelete() {
        //Given
        ArgumentCaptor<CommonConfirmationOfFundsConsentObject> argumentCaptor = ArgumentCaptor.forClass(CommonConfirmationOfFundsConsentObject.class);
        when(deleteConfirmationOfFundsConsentByIdValidator.validate(argumentCaptor.capture())).thenReturn(ValidationResult.valid());
        //When
        service.validateConsentOnDelete(piisConsent);
        //Then
        verify(deleteConfirmationOfFundsConsentByIdValidator).validate(any(CommonConfirmationOfFundsConsentObject.class));
        assertEquals(piisConsent, argumentCaptor.getValue().getPiisConsent());
    }

    @Test
    void validateConsentAuthorisationOnCreate() {
        //Given
        CreatePiisConsentAuthorisationObject createPiisConsentAuthorisationObject = new CreatePiisConsentAuthorisationObject(piisConsent, PSU_ID_DATA);
        ArgumentCaptor<CreatePiisConsentAuthorisationObject> argumentCaptor = ArgumentCaptor.forClass(CreatePiisConsentAuthorisationObject.class);
        when(createPiisConsentAuthorisationValidator.validate(argumentCaptor.capture())).thenReturn(ValidationResult.valid());
        //When
        service.validateConsentAuthorisationOnCreate(createPiisConsentAuthorisationObject);
        //Then
        verify(createPiisConsentAuthorisationValidator).validate(any(CreatePiisConsentAuthorisationObject.class));
        assertEquals(createPiisConsentAuthorisationObject, argumentCaptor.getValue());
    }

    @Test
    void validateConsentAuthorisationOnGettingById() {
        //Given
        ArgumentCaptor<CommonConfirmationOfFundsConsentObject> argumentCaptor = ArgumentCaptor.forClass(CommonConfirmationOfFundsConsentObject.class);
        when(getConfirmationOfFundsConsentAuthorisationsValidator.validate(argumentCaptor.capture())).thenReturn(ValidationResult.valid());
        //When
        service.validateConsentAuthorisationOnGettingById(piisConsent);
        //Then
        verify(getConfirmationOfFundsConsentAuthorisationsValidator).validate(any(CommonConfirmationOfFundsConsentObject.class));
        assertEquals(piisConsent, argumentCaptor.getValue().getPiisConsent());
    }

    @Test
    void validateConsentAuthorisationScaStatus() {
        //Given
        ArgumentCaptor<GetConfirmationOfFundsConsentAuthorisationScaStatusPO> argumentCaptor = ArgumentCaptor.forClass(GetConfirmationOfFundsConsentAuthorisationScaStatusPO.class);
        when(getConfirmationOfFundsConsentAuthorisationScaStatusValidator.validate(argumentCaptor.capture())).thenReturn(ValidationResult.valid());
        //When
        service.validateConsentAuthorisationScaStatus(piisConsent, AUTHORISATION_ID);
        //Then
        verify(getConfirmationOfFundsConsentAuthorisationScaStatusValidator).validate(any(GetConfirmationOfFundsConsentAuthorisationScaStatusPO.class));
        assertEquals(piisConsent, argumentCaptor.getValue().getPiisConsent());
        assertEquals(AUTHORISATION_ID, argumentCaptor.getValue().getAuthorisationId());
    }

}
