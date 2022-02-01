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
