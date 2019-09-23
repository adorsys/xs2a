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

package de.adorsys.psd2.xs2a.service.validator.ais;

import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AisAuthorisationValidatorTest {
    public static final String AUTHORISATION_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    public static final String WRONG_AUTHORISATION_ID = "234ef79c-d785-41ec-9b14-2ea3a7ae2ce0";

    @InjectMocks
    private AisAuthorisationValidator validator;

    private AccountConsent accountConsent;

    @Mock
    private RequestProviderService requestProviderService;
    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-with-authorisations.json",
                                                      AccountConsent.class);
    }

    @Test
    public void validate_success() {
        ValidationResult validationResult = validator.validate(AUTHORISATION_ID, accountConsent);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_wrong_authorisationId() {
        ValidationResult validationResult = validator.validate(WRONG_AUTHORISATION_ID, accountConsent);

        assertTrue(validationResult.isNotValid());
        assertEquals(ErrorType.PIS_403, validationResult.getMessageError().getErrorType());
        assertEquals(RESOURCE_UNKNOWN_403, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }
}
