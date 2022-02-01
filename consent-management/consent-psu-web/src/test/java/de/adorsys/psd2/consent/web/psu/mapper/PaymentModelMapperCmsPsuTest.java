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

package de.adorsys.psd2.consent.web.psu.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
class PaymentModelMapperCmsPsuTest {

    private static final String CONTENT = "payment content";

    private PaymentModelMapperCmsPsu paymentModelMapper;

    private MockHttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        paymentModelMapper = new PaymentModelMapperCmsPsu(mockHttpServletRequest);
    }

    @Test
    void mapToXs2aPayment() {
        mockHttpServletRequest.setContent(CONTENT.getBytes());

        byte[] actual = paymentModelMapper.mapToXs2aPayment();

        assertEquals(CONTENT, new String(actual));
    }
}
