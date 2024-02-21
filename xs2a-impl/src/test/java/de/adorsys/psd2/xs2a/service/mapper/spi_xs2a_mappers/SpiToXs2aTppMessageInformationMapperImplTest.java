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

import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiMessageCategory;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiTppMessageInformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpiToXs2aTppMessageInformationMapperImplTest {
    private static final MessageCategory TEST_MESSAGE_CATEGORY = MessageCategory.ERROR;
    private static final MessageErrorCode TEST_MESSAGE_ERROR_CODE = MessageErrorCode.CERTIFICATE_INVALID;
    private static final SpiMessageCategory TEST_SPI_MESSAGE_CATEGORY = SpiMessageCategory.ERROR;
    private static final SpiMessageErrorCode TEST_SPI_MESSAGE_ERROR_CODE = SpiMessageErrorCode.CERTIFICATE_INVALID;
    private static final String TEST_TEXT = "text";
    private static final String TEST_PATH = "path";

    private final SpiToXs2aTppMessageInformationMapperImpl mapper = new SpiToXs2aTppMessageInformationMapperImpl();

    @Test
    void toTppMessageInformationSet_null() {
        //When
        Set<TppMessageInformation> actual = mapper.toTppMessageInformationSet(null);
        //Then
        assertNull(actual);
    }

    @Test
    void toTppMessageInformationList_null() {
        //When
        List<TppMessageInformation> actual = mapper.toTppMessageInformationList(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToMessageCategory_null() {
        //When
        MessageCategory actual = mapper.mapToMessageCategory(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToMessageErrorCode_null() {
        //When
        MessageErrorCode actual = mapper.mapToMessageErrorCode(null);
        //Then
        assertNull(actual);
    }

    @Test
    void mapToTppMessage_null() {
        //When
        TppMessageInformation actual = mapper.mapToTppMessage(null);
        //Then
        assertNull(actual);
    }

    @Test
    void toTppMessageInformationSet() {
        //Given
        Set<SpiTppMessageInformation> tppMessageInformationSet = new HashSet<>();
        tppMessageInformationSet.add(buildTestSpiTppMessageInformation());
        Set<TppMessageInformation> expected = new HashSet<>();
        expected.add(buildTestTppMessageInformation());
        //When
        Set<TppMessageInformation> actual = mapper.toTppMessageInformationSet(tppMessageInformationSet);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void toTppMessageInformationList() {
        //Given
        Set<SpiTppMessageInformation> tppMessageInformationSet = new HashSet<>();
        tppMessageInformationSet.add(buildTestSpiTppMessageInformation());
        List<TppMessageInformation> expected = new ArrayList<>();
        expected.add(buildTestTppMessageInformation());
        //When
        List<TppMessageInformation> actual = mapper.toTppMessageInformationList(tppMessageInformationSet);
        //Then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(SpiMessageCategory.class)
    void mapToMessageCategory(SpiMessageCategory messageCategory) {
        //When
        MessageCategory actual = mapper.mapToMessageCategory(messageCategory);
        //Then
        assertEquals(messageCategory.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(SpiMessageErrorCode.class)
    void mapToMessageErrorCode(SpiMessageErrorCode messageErrorCode) {
        //When
        MessageErrorCode actual = mapper.mapToMessageErrorCode(messageErrorCode);
        //Then
        assertEquals(messageErrorCode.name(), actual.name());
    }

    @Test
    void mapToTppMessage() {
        //Given
        TppMessageInformation expected = buildTestTppMessageInformation();
        SpiTppMessageInformation tppMessageInformation = buildTestSpiTppMessageInformation();
        //When
        TppMessageInformation actual = mapper.mapToTppMessage(tppMessageInformation);
        //Then
        assertEquals(expected, actual);
    }

    private TppMessageInformation buildTestTppMessageInformation() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(TEST_MESSAGE_CATEGORY, TEST_MESSAGE_ERROR_CODE, TEST_PATH, null);
        tppMessageInformation.setText(TEST_TEXT);
        return tppMessageInformation;
    }

    private SpiTppMessageInformation buildTestSpiTppMessageInformation() {
        SpiTppMessageInformation tppMessageInformation = SpiTppMessageInformation.of(TEST_SPI_MESSAGE_CATEGORY, TEST_SPI_MESSAGE_ERROR_CODE, TEST_PATH, null);
        tppMessageInformation.setText(TEST_TEXT);
        return tppMessageInformation;
    }
}
