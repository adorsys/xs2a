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

package de.adorsys.psd2.xs2a.service.event.mapper;

import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EventMapperImpl.class})
class EventMapperTest {

    @Autowired
    private EventMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void toEventPsuIdData() {
        PsuIdDataBO actualPsuIdDataBO = mapper.toEventPsuIdData(jsonReader.getObjectFromFile("json/service/event/psu-id-data.json", PsuIdData.class));

        PsuIdDataBO expectedPsuIdDataBO = jsonReader.getObjectFromFile("json/service/event/psu-id-data.json", PsuIdDataBO.class);
        assertEquals(expectedPsuIdDataBO, actualPsuIdDataBO);
    }

    @Test
    void toEventPsuIdData_nullValue() {
        PsuIdDataBO actualPsuIdDataBO = mapper.toEventPsuIdData(null);
        assertNull(actualPsuIdDataBO);
    }
}
