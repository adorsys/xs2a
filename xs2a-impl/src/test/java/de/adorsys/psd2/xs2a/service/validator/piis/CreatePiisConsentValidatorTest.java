/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
