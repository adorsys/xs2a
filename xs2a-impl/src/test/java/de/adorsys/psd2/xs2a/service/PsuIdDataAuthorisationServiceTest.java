/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PsuIdDataAuthorisationServiceTest {
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final PsuIdData PSU_ID_DATA_FROM_AUTHORISATION = new PsuIdData("authorisationPsuData", null, null, null, null);

    private static final PsuIdData PSU_ID_DATA_FROM_PAYMENT = new PsuIdData("paymentPsuData", null, null, null, null);
    private static final List<PsuIdData> PSU_ID_DATA_LIST_FROM_PAYMENT = Collections.singletonList(PSU_ID_DATA_FROM_PAYMENT);

    @InjectMocks
    private PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    @Mock
    private Xs2aAuthorisationService authorisationService;

    private JsonReader jsonReader = new JsonReader();
    private Authorisation authorisation;

    @BeforeEach
    void setUp() {
        authorisation = jsonReader.getObjectFromFile("json/service/authorisation.json", Authorisation.class);
    }

    @Test
    void getPsuIdData_fromAuthorisation() {
        // Given
        when(authorisationService.getAuthorisationById(AUTHORISATION_ID)).thenReturn(Optional.of(authorisation));

        // When
        PsuIdData actualData = psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, PSU_ID_DATA_LIST_FROM_PAYMENT);

        // Then
        assertThat(actualData).isEqualTo(PSU_ID_DATA_FROM_AUTHORISATION);
    }

    @Test
    void getPsuIdData_fromPayment() {
        // Given
        when(authorisationService.getAuthorisationById(AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        PsuIdData actualData = psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, PSU_ID_DATA_LIST_FROM_PAYMENT);

        // Then
        assertThat(actualData).isEqualTo(PSU_ID_DATA_FROM_PAYMENT);
    }

    @Test
    void getPsuIdData_empty() {
        // Given
        when(authorisationService.getAuthorisationById(AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        PsuIdData actualData = psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, Collections.emptyList());

        // Then
        assertThat(actualData).isEqualTo(new PsuIdData());
    }
}
