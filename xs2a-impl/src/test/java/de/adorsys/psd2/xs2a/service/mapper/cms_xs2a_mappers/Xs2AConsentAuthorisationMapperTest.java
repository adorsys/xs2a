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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
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

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAuthorisationRequest_CreateAuthorisationRequest() {
        CreateAuthorisationRequest actual = mapper.mapToAuthorisationRequest(null, ScaStatus.RECEIVED, PSU_ID_DATA, ScaApproach.EMBEDDED, "ok.uri", "nok.uri");

        CreateAuthorisationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/consent/create-authorisation-request.json", CreateAuthorisationRequest.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAuthorisationRequest_CreateAuthorisationRequest_scaStatusIsNull() {
        CreateAuthorisationRequest actual = mapper.mapToAuthorisationRequest(null, null, PSU_ID_DATA, ScaApproach.EMBEDDED, "ok.uri", "nok.uri");
        assertNull(actual);
    }

    @Test
    void mapToAuthorisationRequest_UpdateAuthorisationRequest() {
        ConsentAuthorisationsParameters updateAuthorisationRequest = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);

        UpdateAuthorisationRequest actual = mapper.mapToAuthorisationRequest(updateAuthorisationRequest);

        UpdateAuthorisationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/consent/ais-update-authorisation-request.json", UpdateAuthorisationRequest.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAuthorisationRequest_UpdateAuthorisationRequest_nullValue() {
        assertNull(mapper.mapToAuthorisationRequest(null));
    }
}
