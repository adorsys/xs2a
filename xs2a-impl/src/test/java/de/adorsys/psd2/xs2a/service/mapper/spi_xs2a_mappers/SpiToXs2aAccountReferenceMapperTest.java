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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aAccountReferenceMapperImpl.class})
class SpiToXs2aAccountReferenceMapperTest {

    @Autowired
    private SpiToXs2aAccountReferenceMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private SpiAccountReference spiAccountReference;
    private AccountReference expectedAccountReference;

    @BeforeEach
    void setUp() {
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-reference.json", AccountReference.class);
    }

    @Test
    void mapToXs2aAccountReference() {
        AccountReference accountReference = mapper.mapToXs2aAccountReference(spiAccountReference);
        assertEquals(expectedAccountReference, accountReference);
    }

    @Test
    void mapToXs2aAccountReference_nullValue() {
        assertNull(mapper.mapToXs2aAccountReference(null));
    }

    @Test
    void mapToXs2aAccountReferenceList() {
        List<AccountReference> accountReference = mapper.mapToXs2aAccountReferences(Collections.singletonList(spiAccountReference));
        assertEquals(expectedAccountReference, accountReference.get(0));
    }
}
