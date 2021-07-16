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
import de.adorsys.psd2.model.TppMessageInitiationStatusResponse200;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TppMessage200MapperImpl.class})
class TppMessage200MapperImplTest {
    @Autowired
    private TppMessage200Mapper tppMessage200Mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTppMessage200_infoIsNull_returnsNull() {
        //When
        TppMessageInitiationStatusResponse200 tppMessage200 = tppMessage200Mapper.mapToTppMessage200(null);

        //Then
        assertThat(tppMessage200).isNull();
    }

    @Test
    void mapToTppMessageCategory_categoryIsNull_returnsNull() {
        //When
        TppMessageCategory tppMessageCategory = tppMessage200Mapper.mapToTppMessageCategory(null);

        //Then
        assertThat(tppMessageCategory).isNull();
    }

    @Test
    void mapToTppMessageCategory_Ok_Error() {
        //Given
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageCategory.ERROR, MessageErrorCode.EXECUTION_DATE_INVALID, "test path");
        TppMessageInitiationStatusResponse200 expected = jsonReader.getObjectFromFile("json/service/mapper/tpp-message-mapper/tpp-message-200-error.json", TppMessageInitiationStatusResponse200.class);

        //When
        TppMessageInitiationStatusResponse200 actual = tppMessage200Mapper.mapToTppMessage200(tppMessageInformation);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToTppMessageCategory_Ok_Warning() {
        //Given
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageCategory.WARNING,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");
        TppMessageInitiationStatusResponse200 expected = jsonReader.getObjectFromFile("json/service/mapper/tpp-message-mapper/tpp-message-200-warning.json", TppMessageInitiationStatusResponse200.class);

        //When
        TppMessageInitiationStatusResponse200 actual = tppMessage200Mapper.mapToTppMessage200(tppMessageInformation);

        //Then
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void mapToTppMessage200List_Empty() {
        //When
        List<TppMessageInitiationStatusResponse200> actual = tppMessage200Mapper.mapToTppMessage200List(Collections.emptySet());

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToTppMessage200List_NonEmpty() {
        //Given
        TppMessageInformation error = TppMessageInformation.of(MessageCategory.WARNING,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");
        TppMessageInformation warning = TppMessageInformation.of(MessageCategory.ERROR,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        //When
        List<TppMessageInitiationStatusResponse200> actual = tppMessage200Mapper.mapToTppMessage200List(Set.of(error, warning));

        //Then
        assertThat(actual).isNotNull();
    }
}
