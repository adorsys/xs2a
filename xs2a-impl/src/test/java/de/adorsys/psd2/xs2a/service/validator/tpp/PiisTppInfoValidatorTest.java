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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIIS_400;
import static de.adorsys.psd2.xs2a.service.validator.tpp.PiisTppInfoValidator.TPP_ERROR_MESSAGE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PiisTppInfoValidatorTest {

    private static final String AUTHORISATION_NUMBER = "12345987";

    @InjectMocks
    private PiisTppInfoValidator validator;

    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppService tppService;
    private TppInfo tppInRequest;

    @Before
    public void setUp() {
        tppInRequest = new TppInfo();
        tppInRequest.setAuthorisationNumber(AUTHORISATION_NUMBER);
    }

    @Test
    public void validateTpp_authorisationNumber_success() {
        when(tppService.getTppInfo()).thenReturn(tppInRequest);

        ValidationResult actual = validator.validateTpp(AUTHORISATION_NUMBER);

        verify(tppService, times(1)).getTppInfo();

        assertTrue(actual.isValid());
    }

    @Test
    public void validateTpp_authorisationNumber_null_error() {
        ValidationResult actual = validator.validateTpp(null);

        verify(tppService, never()).getTppInfo();

        assertFalse(actual.isValid());
        assertEquals(PIIS_400, actual.getMessageError().getErrorType());
        assertEquals(CONSENT_UNKNOWN_400, actual.getMessageError().getTppMessage().getMessageErrorCode());
        assertEquals(TPP_ERROR_MESSAGE, actual.getMessageError().getTppMessage().getText());
    }
}
