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

import de.adorsys.psd2.model.TppMessage201PaymentInitiation;
import de.adorsys.psd2.model.TppMessageCategory;
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
@ContextConfiguration(classes = {TppMessage201MapperImpl.class})
class TppMessage201MapperImplTest {
    @Autowired
    private TppMessage201MapperImpl tppMessage201Mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTppMessage201_infoIsNull_returnsNull() {
        TppMessage201PaymentInitiation tppMessage201 = tppMessage201Mapper.mapToTppMessage201(null);
        assertNull(tppMessage201);
    }

    @Test
    void mapToTppMessageCategory_categoryIsNull_returnsNull() {
        TppMessageCategory tppMessageCategory = tppMessage201Mapper.mapToTppMessageCategory(null);
        assertNull(tppMessageCategory);
    }

    @Test
    void mapToTppMessageCategory_Ok_Error() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageCategory.ERROR,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        TppMessage201PaymentInitiation actual = tppMessage201Mapper.mapToTppMessage201(tppMessageInformation);

        TppMessage201PaymentInitiation expected = jsonReader.getObjectFromFile("json/service/mapper/tpp-message-mapper/tpp-message-2xx-error.json", TppMessage201PaymentInitiation.class);

        assertEquals(expected, actual);
    }

    @Test
    void mapToTppMessageCategory_Ok_Warning() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageCategory.WARNING,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        TppMessage201PaymentInitiation actual = tppMessage201Mapper.mapToTppMessage201(tppMessageInformation);

        TppMessage201PaymentInitiation expected = jsonReader.getObjectFromFile("json/service/mapper/tpp-message-mapper/tpp-message-2xx-warning.json", TppMessage201PaymentInitiation.class);

        assertEquals(expected, actual);
    }


    @Test
    void mapToTppMessage201List_Empty() {
        assertNull(tppMessage201Mapper.mapToTppMessage201List(Collections.emptySet()));
    }

    @Test
    void mapToTppMessage201List_NonEmpty() {
        TppMessageInformation error = TppMessageInformation.of(MessageCategory.WARNING,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");
        TppMessageInformation warning = TppMessageInformation.of(MessageCategory.ERROR,MessageErrorCode.EXECUTION_DATE_INVALID,"test path");

        List<TppMessage201PaymentInitiation> actual = tppMessage201Mapper.mapToTppMessage201List(Set.of(error, warning));

        assertNotNull(actual);
    }
}

