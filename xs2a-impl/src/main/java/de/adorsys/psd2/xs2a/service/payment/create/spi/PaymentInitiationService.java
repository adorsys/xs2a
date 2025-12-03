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

package de.adorsys.psd2.xs2a.service.payment.create.spi;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;

/**
 * Service for initiating payments in SPI layer
 *
 * @param <T> type of the payment initiated by this service
 */
public interface PaymentInitiationService<T extends CommonPayment> {
    /**
     * Initiates a payment at the SPI layer.
     *
     * @param payment the payment to initiate
     * @param paymentProduct the payment product
     * @param psuIdData the PSU identification data
     * @return payment initiation response
     */
    PaymentInitiationResponse initiatePayment(T payment, String paymentProduct, PsuIdData psuIdData);
}
