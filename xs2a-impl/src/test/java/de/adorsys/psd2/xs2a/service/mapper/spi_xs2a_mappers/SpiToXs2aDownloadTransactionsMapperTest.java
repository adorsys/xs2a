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

import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransactionsDownloadResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aDownloadTransactionsMapperImpl.class})
class SpiToXs2aDownloadTransactionsMapperTest {

    @Autowired
    private SpiToXs2aDownloadTransactionsMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aTransactionsDownloadResponseTest() throws IOException {
        // Given
        SpiTransactionsDownloadResponse spiTransactionsDownloadResponse =
            new SpiTransactionsDownloadResponse(new ByteArrayInputStream("test stream".getBytes()), "all_my_transactions.json", 100000);

        // When
        Xs2aTransactionsDownloadResponse actualResponse =
            mapper.mapToXs2aTransactionsDownloadResponse(spiTransactionsDownloadResponse);

        // Then
        Xs2aTransactionsDownloadResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/xs2a-transaction-download-response.json", Xs2aTransactionsDownloadResponse.class);
        expectedResponse.setTransactionStream(new ByteArrayInputStream("test stream".getBytes()));

        assertEquals(expectedResponse.getDataFileName(), actualResponse.getDataFileName());
        assertEquals(expectedResponse.getDataSizeBytes(), actualResponse.getDataSizeBytes());
        assertEquals(IOUtils.toString(expectedResponse.getTransactionStream(), StandardCharsets.UTF_8), IOUtils.toString(actualResponse.getTransactionStream(), StandardCharsets.UTF_8));
    }

    @Test
    void mapToXs2aTransactionsDownloadResponseTest_nullInput() {
        //When
        Xs2aTransactionsDownloadResponse actual = mapper.mapToXs2aTransactionsDownloadResponse(null);

        //Then
        assertNull(actual);
    }
}
