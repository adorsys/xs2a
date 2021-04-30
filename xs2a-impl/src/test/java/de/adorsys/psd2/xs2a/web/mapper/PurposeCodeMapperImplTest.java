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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

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
