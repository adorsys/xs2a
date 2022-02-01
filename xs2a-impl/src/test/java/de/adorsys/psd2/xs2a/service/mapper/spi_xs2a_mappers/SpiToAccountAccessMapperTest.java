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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aAccountAccessMapper.class, SpiToXs2aAccountReferenceMapperImpl.class})
class SpiToAccountAccessMapperTest {

    @Autowired
    private SpiToXs2aAccountAccessMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void getAccessForGlobalOrAllAvailableAccountsConsent() {
        CreateConsentReq createConsentReq = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-consent-request.json", CreateConsentReq.class);
        AccountAccess actualAccountAccess = mapper.getAccessForGlobalOrAllAvailableAccountsConsent(createConsentReq);

        AccountAccess expectedAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/expected-xs2a-account-access.json", AccountAccess.class);
        assertEquals(expectedAccountAccess, actualAccountAccess);
    }

    @Test
    void mapToAccountAccess() {
        SpiAccountAccess spiAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-access.json", SpiAccountAccess.class);
        Optional<AccountAccess> actualXs2aAccountAccess = mapper.mapToAccountAccess(spiAccountAccess);

        AccountAccess expectedAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/expected-xs2a-account-access2.json", AccountAccess.class);
        assertTrue(actualXs2aAccountAccess.isPresent());
        assertEquals(expectedAccountAccess, actualXs2aAccountAccess.get());
    }

    @Test
    void mapToAccountAccess_nullValue() {
        assertFalse(mapper.mapToAccountAccess(null).isPresent());
    }
}
