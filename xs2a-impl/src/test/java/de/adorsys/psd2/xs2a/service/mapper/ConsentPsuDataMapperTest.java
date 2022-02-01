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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class ConsentPsuDataMapperTest {
    @InjectMocks
    private ConsentPsuDataMapper mapper;
    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiUpdateConsentPsuDataReq() {
        ConsentAuthorisationsParameters updateAuthorisationRequest = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
        AuthorisationProcessorResponse authorisationProcessorResponse = jsonReader.getObjectFromFile("json/service/mapper/consent/authorisation-processor-response2.json", AuthorisationProcessorResponse.class);

        ConsentAuthorisationsParameters actual = mapper.mapToUpdateConsentPsuDataReq(updateAuthorisationRequest, authorisationProcessorResponse);

        ConsentAuthorisationsParameters expected = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req-mapped.json", ConsentAuthorisationsParameters.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToSpiUpdateConsentPsuDataReq_nullValue() {
        assertNull(mapper.mapToUpdateConsentPsuDataReq(null, null));
    }
}
