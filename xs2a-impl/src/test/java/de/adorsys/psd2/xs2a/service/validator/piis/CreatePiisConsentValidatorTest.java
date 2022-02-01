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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentRequestObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePiisConsentValidatorTest {
    private static final PsuIdData PSU_DATA = new PsuIdData("111", null, null, null, null);
    private final JsonReader jsonReader = new JsonReader();

    @Mock
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    @Mock
    private SupportedAccountReferenceValidator supportedAccountReferenceValidator;

    @InjectMocks
    private CreatePiisConsentValidator createPiisConsentValidator;

    @Test
    void validation_succes() {
        // Given
        CreatePiisConsentRequest consentRequest = jsonReader.getObjectFromFile("json/piis/create-piis-consent.json", CreatePiisConsentRequest.class);
        CreatePiisConsentRequestObject consentRequestObject = new CreatePiisConsentRequestObject(consentRequest, PSU_DATA);

        when(psuDataInInitialRequestValidator.validate(PSU_DATA)).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(Collections.singletonList(consentRequest.getAccount()))).thenReturn(ValidationResult.valid());

        // When
        ValidationResult actual = createPiisConsentValidator.validate(consentRequestObject);

        // Then
        assertNotNull(actual);
        assertTrue(actual.isValid());
    }

    @Test
    void validation_psuValidation_invalid() {
        // Given
        CreatePiisConsentRequest consentRequest = jsonReader.getObjectFromFile("json/piis/create-piis-consent.json", CreatePiisConsentRequest.class);
        CreatePiisConsentRequestObject consentRequestObject = new CreatePiisConsentRequestObject(consentRequest, PSU_DATA);

        when(psuDataInInitialRequestValidator.validate(PSU_DATA)).thenReturn(ValidationResult.invalid(new MessageError(ErrorType.AIS_400)));

        // When
        ValidationResult actual = createPiisConsentValidator.validate(consentRequestObject);

        // Then
        verifyNoInteractions(supportedAccountReferenceValidator);

        assertNotNull(actual);
        assertTrue(actual.isNotValid());
    }

    @Test
    void validation_accountReference_invalid() {
        // Given
        CreatePiisConsentRequest consentRequest = jsonReader.getObjectFromFile("json/piis/create-piis-consent.json", CreatePiisConsentRequest.class);
        CreatePiisConsentRequestObject consentRequestObject = new CreatePiisConsentRequestObject(consentRequest, PSU_DATA);

        when(psuDataInInitialRequestValidator.validate(PSU_DATA)).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(Collections.singletonList(consentRequest.getAccount()))).thenReturn(ValidationResult.invalid(new MessageError(ErrorType.AIS_400)));

        // When
        ValidationResult actual = createPiisConsentValidator.validate(consentRequestObject);

        // Then
        assertNotNull(actual);
        assertTrue(actual.isNotValid());
    }
}
