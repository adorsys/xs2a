/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * VOP model mapper for Xs2a layer.
 * Maps VOP initiation parameters to appropriate formats for service layer.
 */
@Component
@RequiredArgsConstructor
public class VopModelMapperXs2a{

    private final PaymentModelMapperXs2a paymentModelMapperXs2a;


    /**
     * Maps VOP initiation parameters to XS2A format.
     * Delegates to the payment model mapper to convert the payment data into a byte array representation
     * suitable for processing in the XS2A service layer.
     *
     * @return byte array representation of the VOP payment data in XS2A format
     */
    public byte[] mapToXs2aVop() {
    return paymentModelMapperXs2a.mapToXs2aPayment();
    }
}
