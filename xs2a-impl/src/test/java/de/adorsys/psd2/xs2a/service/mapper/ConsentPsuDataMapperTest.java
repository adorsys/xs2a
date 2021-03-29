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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConsentPsuDataMapperTest {
    @InjectMocks
    private ConsentPsuDataMapper mapper;
    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq updateAuthorisationRequest = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        AuthorisationProcessorResponse authorisationProcessorResponse = jsonReader.getObjectFromFile("json/service/mapper/consent/authorisation-processor-response2.json", AuthorisationProcessorResponse.class);

        UpdateConsentPsuDataReq actual = mapper.mapToUpdateConsentPsuDataReq(updateAuthorisationRequest, authorisationProcessorResponse);

        UpdateConsentPsuDataReq expected = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req-mapped.json", UpdateConsentPsuDataReq.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToSpiUpdateConsentPsuDataReq_nullValue() {
        assertNull(mapper.mapToUpdateConsentPsuDataReq(null, null));
    }
}
