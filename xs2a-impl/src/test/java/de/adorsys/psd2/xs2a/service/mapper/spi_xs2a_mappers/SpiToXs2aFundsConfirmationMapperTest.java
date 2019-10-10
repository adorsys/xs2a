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

import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aFundsConfirmationMapperImpl.class})
public class SpiToXs2aFundsConfirmationMapperTest {

    @Autowired
    private SpiToXs2aFundsConfirmationMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToFundsConfirmationResponse() {
        SpiFundsConfirmationResponse spiFundsConfirmationResponse = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-funds-confirmation-response.json",
                                                                                                 SpiFundsConfirmationResponse.class);

        FundsConfirmationResponse fundsConfirmationResponse = mapper.mapToFundsConfirmationResponse(spiFundsConfirmationResponse);

        FundsConfirmationResponse expectedFundsConfirmationResponse = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-funds-confirmation-response.json",
                                                                                                   FundsConfirmationResponse.class);
        assertEquals(expectedFundsConfirmationResponse, fundsConfirmationResponse);
    }

    @Test
    public void mapToFundsConfirmationResponse_nullValue() {
        FundsConfirmationResponse fundsConfirmationResponse = mapper.mapToFundsConfirmationResponse(null);
        assertNull(fundsConfirmationResponse);
    }
}
