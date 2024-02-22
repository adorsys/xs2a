/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aFundsConfirmationMapperImpl.class})
class SpiToXs2aFundsConfirmationMapperTest {

    @Autowired
    private SpiToXs2aFundsConfirmationMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToFundsConfirmationResponse() {
        SpiFundsConfirmationResponse spiFundsConfirmationResponse = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-funds-confirmation-response.json",
                                                                                                 SpiFundsConfirmationResponse.class);

        FundsConfirmationResponse fundsConfirmationResponse = mapper.mapToFundsConfirmationResponse(spiFundsConfirmationResponse);

        FundsConfirmationResponse expectedFundsConfirmationResponse = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-funds-confirmation-response.json",
                                                                                                   FundsConfirmationResponse.class);
        assertEquals(expectedFundsConfirmationResponse, fundsConfirmationResponse);
    }

    @Test
    void mapToFundsConfirmationResponse_nullValue() {
        FundsConfirmationResponse fundsConfirmationResponse = mapper.mapToFundsConfirmationResponse(null);
        assertNull(fundsConfirmationResponse);
    }
}
