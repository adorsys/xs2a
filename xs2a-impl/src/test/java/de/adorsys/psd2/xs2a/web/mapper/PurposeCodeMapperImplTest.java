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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PurposeCodeMapperImplTest {

    private PurposeCodeMapperImpl purposeCodeMapper;

    @BeforeEach
    void setUp() {
        purposeCodeMapper = new PurposeCodeMapperImpl();
    }

    @Test
    void mapToPurposeCode_returnsNull () {
        de.adorsys.psd2.core.payment.model.PurposeCode purposeCode = purposeCodeMapper.mapToPurposeCode((de.adorsys.psd2.model.PurposeCode)null);
        assertNull(purposeCode);
    }

    @Test
    void mapToPurposeCode_returnsNullCode () {
        de.adorsys.psd2.model.PurposeCode purposeCode = purposeCodeMapper.mapToPurposeCode((de.adorsys.psd2.core.payment.model.PurposeCode) null);
        assertNull(purposeCode);
    }

    @ParameterizedTest
    @EnumSource(de.adorsys.psd2.model.PurposeCode.class)
    void mapToPurposeCode_success(de.adorsys.psd2.model.PurposeCode purposeCode) {
        de.adorsys.psd2.core.payment.model.PurposeCode actual = purposeCodeMapper.mapToPurposeCode(purposeCode);
        assertEquals(purposeCode.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(de.adorsys.psd2.core.payment.model.PurposeCode.class)
    void mapToPurposeCode_success(de.adorsys.psd2.core.payment.model.PurposeCode purposeCode) {
        de.adorsys.psd2.model.PurposeCode actual = purposeCodeMapper.mapToPurposeCode(purposeCode);
        assertEquals(purposeCode.name(), actual.name());
    }
}
