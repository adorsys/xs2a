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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadTransactionsReportValidatorTest {
    private static final MessageError AIS_CONSENT_EXPIRED_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_EXPIRED));
    private static final MessageError AIS_CONSENT_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @InjectMocks
    private DownloadTransactionsReportValidator downloadTransactionsReportValidator;

    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;

    private JsonReader jsonReader;
    private DownloadTransactionListRequestObject requestObject;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();

        when(aisAccountTppInfoValidator.validateTpp(any()))
            .thenReturn(ValidationResult.valid());
    }

    @Test
    public void testValidate_shouldReturnValid() {
        // Given
        requestObject = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-download-object-valid.json", DownloadTransactionListRequestObject.class);

        // When
        ValidationResult actual = downloadTransactionsReportValidator.validate(requestObject);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidateExpired_shouldReturnExpiredError() {
        // Given
        requestObject = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-download-object-expired.json", DownloadTransactionListRequestObject.class);

        // When
        ValidationResult actual = downloadTransactionsReportValidator.validate(requestObject);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_EXPIRED_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidateInvalid_shouldReturnInvalidError() {
        // Given
        requestObject = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-download-object-invalid.json", DownloadTransactionListRequestObject.class);

        // When
        ValidationResult actual = downloadTransactionsReportValidator.validate(requestObject);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_INVALID_ERROR, actual.getMessageError());
    }
}
