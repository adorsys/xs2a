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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aPiisConsentMapperImpl.class, ConsentDataMapper.class})
class Xs2aPiisConsentMapperTest {
    private JsonReader jsonReader = new JsonReader();

    @Autowired
    private Xs2aPiisConsentMapper xs2aPiisConsentMapper;

    @Test
    void mapToPiisConsent() {
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/cms-consent.json", CmsConsent.class);
        byte[] piisConsentData = jsonReader.getBytesFromFile("json/service/mapper/consent/piis/piis-consent-data.json");
        cmsConsent.setConsentData(piisConsentData);
        PiisConsent expectedXs2aConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/piis/piis-consent.json", PiisConsent.class);

        PiisConsent result = xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent);

        assertEquals(expectedXs2aConsent, result);
    }
}
