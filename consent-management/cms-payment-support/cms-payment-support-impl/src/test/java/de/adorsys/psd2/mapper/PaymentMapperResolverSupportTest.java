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

import de.adorsys.psd2.consent.api.pis.CmsCommonPaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMapperResolverSupportTest {
    private static final String PAYMENT_PRODUCT = "product";

    @InjectMocks
    private PaymentMapperResolverSupport resolver;
    @Mock
    private CmsStandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private CmsCommonPaymentMapperImpl cmsCommonPaymentMapper;
    @Mock
    private CmsCommonPaymentMapperSupportImpl cmsCommonPaymentMapperSupport;

    @Test
    void getCmsCommonPaymentMapper_Common() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(PAYMENT_PRODUCT)).thenReturn(true);

        // When
        CmsCommonPaymentMapper actual = resolver.getCmsCommonPaymentMapper(PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isEqualTo(cmsCommonPaymentMapper);
    }

    @Test
    void getCmsCommonPaymentMapper_Support() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(PAYMENT_PRODUCT)).thenReturn(false);

        // When
        CmsCommonPaymentMapper actual = resolver.getCmsCommonPaymentMapper(PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isEqualTo(cmsCommonPaymentMapperSupport);
    }
}
