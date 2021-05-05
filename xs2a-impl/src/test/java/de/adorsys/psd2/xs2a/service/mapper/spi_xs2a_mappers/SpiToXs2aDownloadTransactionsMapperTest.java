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
