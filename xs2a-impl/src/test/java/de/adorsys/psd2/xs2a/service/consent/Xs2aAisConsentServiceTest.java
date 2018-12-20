/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aAisConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    @InjectMocks
    private Xs2aAisConsentService xs2aAisConsentService;
    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;

    @Before
    public void setUp() {
        when(aisConsentServiceEncrypted.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(aisConsentServiceEncrypted.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actual = xs2aAisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongIds() {
        // When
        Optional<ScaStatus> actual = xs2aAisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }
}
