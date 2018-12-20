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

package de.adorsys.psd2.xs2a.service.authorization.pis;

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
public class RedirectPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";

    @InjectMocks
    private RedirectPisScaAuthorisationService redirectPisScaAuthorisationService;
    @Mock
    private PisAuthorisationService pisAuthorisationService;

    @Before
    public void setUp() {
        when(pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(pisAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(ScaStatus.RECEIVED, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongIds() {
        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }
}
