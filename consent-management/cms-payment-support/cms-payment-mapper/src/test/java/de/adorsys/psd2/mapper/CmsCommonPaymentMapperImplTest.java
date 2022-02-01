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

package de.adorsys.psd2.mapper;

import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class CmsCommonPaymentMapperImplTest {

    private CmsCommonPaymentMapperImpl mapper;
    private CmsCommonPayment cmsCommonPayment;

    @BeforeEach
    void setUp() {
        mapper = new CmsCommonPaymentMapperImpl();
        cmsCommonPayment = new CmsCommonPayment("payment product");
    }

    @Test
    void mapToCmsSinglePayment() {
        assertSame(cmsCommonPayment, mapper.mapToCmsSinglePayment(cmsCommonPayment));
    }

    @Test
    void mapToCmsBulkPayment() {
        assertSame(cmsCommonPayment, mapper.mapToCmsBulkPayment(cmsCommonPayment));
    }

    @Test
    void mapToCmsPeriodicPayment() {
        assertSame(cmsCommonPayment, mapper.mapToCmsPeriodicPayment(cmsCommonPayment));
    }
}
