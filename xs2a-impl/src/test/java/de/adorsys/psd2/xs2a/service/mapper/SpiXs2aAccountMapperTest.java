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

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SpiXs2aAccountMapperTest {
    private static final String SPI_ACCOUNT_DETAILS_JSON_PATH = "/json/MapSpiAccountDetailsToXs2aAccountDetailsTest.json";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private Xs2aObjectMapper xs2aObjectMapper = (Xs2aObjectMapper) new Xs2aObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void mapSpiAccountDetailsToXs2aAccountDetails() throws IOException {
        //Given:
        String spiAccountDetailsJson = IOUtils.resourceToString(SPI_ACCOUNT_DETAILS_JSON_PATH, UTF_8);
        SpiAccountDetails donorAccountDetails = xs2aObjectMapper.readValue(spiAccountDetailsJson, SpiAccountDetails.class);

        //When:
        assertNotNull(donorAccountDetails);
    }
}
