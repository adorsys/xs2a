/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
