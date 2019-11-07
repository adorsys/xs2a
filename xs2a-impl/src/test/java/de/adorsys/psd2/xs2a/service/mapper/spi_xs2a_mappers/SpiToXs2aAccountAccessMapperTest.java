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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aAccountAccessMapper.class, SpiToXs2aAccountReferenceMapperImpl.class})
public class SpiToXs2aAccountAccessMapperTest {

    @Autowired
    private SpiToXs2aAccountAccessMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void getAccessForGlobalOrAllAvailableAccountsConsent() {
        CreateConsentReq createConsentReq = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-consent-request.json", CreateConsentReq.class);
        Xs2aAccountAccess actualXs2aAccountAccess = mapper.getAccessForGlobalOrAllAvailableAccountsConsent(createConsentReq);

        Xs2aAccountAccess expectedXs2aAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/expected-xs2a-account-access.json", Xs2aAccountAccess.class);
        assertEquals(expectedXs2aAccountAccess, actualXs2aAccountAccess);
    }

    @Test
    public void mapToAccountAccess() {
        SpiAccountAccess spiAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-access.json", SpiAccountAccess.class);
        Optional<Xs2aAccountAccess> actualXs2aAccountAccess = mapper.mapToAccountAccess(spiAccountAccess);

        Xs2aAccountAccess expectedXs2aAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/expected-xs2a-account-access2.json", Xs2aAccountAccess.class);
        assertTrue(actualXs2aAccountAccess.isPresent());
        assertEquals(expectedXs2aAccountAccess, actualXs2aAccountAccess.get());
    }

    @Test
    public void mapToAccountAccess_nullValue() {
       assertFalse(mapper.mapToAccountAccess(null).isPresent());
    }
}
