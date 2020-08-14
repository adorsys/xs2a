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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aConsentAuthorisationMapper.class, TppRedirectUriMapper.class})
class Xs2AConsentAuthorisationMapperTest {

    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu Id", "psuId Type", "psu Corporate Id", "psuCorporate Id Type", "psuIp Address");


    @Autowired
    private Xs2aConsentAuthorisationMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAuthorisationRequest_CreateAuthorisationRequest() {
        CreateAuthorisationRequest actual = mapper.mapToAuthorisationRequest(ScaStatus.RECEIVED, PSU_ID_DATA, ScaApproach.EMBEDDED, "ok.uri", "nok.uri");

        CreateAuthorisationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/consent/create-authorisation-request.json", CreateAuthorisationRequest.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAuthorisationRequest_CreateAuthorisationRequest_scaStatusIsNull() {
        CreateAuthorisationRequest actual = mapper.mapToAuthorisationRequest(null, PSU_ID_DATA, ScaApproach.EMBEDDED, "ok.uri", "nok.uri");
        assertNull(actual);
    }

    @Test
    void mapToAuthorisationRequest_UpdateAuthorisationRequest() {
        UpdateConsentPsuDataReq updateAuthorisationRequest = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);

        UpdateAuthorisationRequest actual = mapper.mapToAuthorisationRequest(updateAuthorisationRequest);

        UpdateAuthorisationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/consent/ais-update-authorisation-request.json", UpdateAuthorisationRequest.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAuthorisationRequest_UpdateAuthorisationRequest_nullValue() {
        assertNull(mapper.mapToAuthorisationRequest(null));
    }
}
