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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TppMessageGenericMapperImpl.class})
class TppMessageGenericMapperImplTest {
    @Autowired
    private TppMessageGenericMapper tppMessageGenericMapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTppMessageGeneric_infoIsNull_returnsNull() {
        TppMessageGeneric tppMessageGeneric = tppMessageGenericMapper.mapToTppMessageGeneric(null);
        assertNull(tppMessageGeneric);
    }

    @Test
    void mapToTppMessageCategory_categoryIsNull_returnsNull() {
        TppMessageCategory tppMessageCategory = tppMessageGenericMapper.mapToTppMessageCategory(null);
        assertNull(tppMessageCategory);
    }

    @Test
    void mapToTppMessageCategory_Ok_Error() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageCategory.ERROR,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        TppMessageGeneric actual = tppMessageGenericMapper.mapToTppMessageGeneric(tppMessageInformation);

        TppMessageGeneric expected = jsonReader.getObjectFromFile("json/service/mapper/tpp-message-mapper/tpp-message-2xx-error.json", TppMessageGeneric.class);

        assertEquals(expected, actual);
    }

    @Test
    void mapToTppMessageCategory_Ok_Warning() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageCategory.WARNING,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        TppMessageGeneric actual = tppMessageGenericMapper.mapToTppMessageGeneric(tppMessageInformation);

        TppMessageGeneric expected = jsonReader.getObjectFromFile("json/service/mapper/tpp-message-mapper/tpp-message-2xx-warning.json", TppMessageGeneric.class);

        assertEquals(expected, actual);
    }


    @Test
    void mapToTppMessageGenericList_Empty() {
        assertNull(tppMessageGenericMapper.mapToTppMessageGenericList(Collections.emptySet()));
    }

    @Test
    void mapToTppMessageGenericList_NonEmpty() {
        TppMessageInformation error = TppMessageInformation.of(MessageCategory.WARNING,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");
        TppMessageInformation warning = TppMessageInformation.of(MessageCategory.ERROR,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        List<TppMessageGeneric> actual = tppMessageGenericMapper.mapToTppMessageGenericList(Set.of(error, warning));

        assertNotNull(actual);
    }
}
