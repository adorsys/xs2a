/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;

import java.util.List;

public interface PaymentSpi {

    /**
     * Queries ASPSP to add single payment to aspsp database
     *
     * @param spiSinglePayments SpiSinglePayments representation of initiated single payment
     * @return Payment initialisation response with related information about initiated payment
     */
    SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments);

    /**
     * Queries ASPSP to to add periodic payment to aspsp database
     *
     * @param periodicPayment SpiPeriodicPayment representation of initiated periodic payment
     * @return Payment initialisation response with related information about initiated payment
     */
    SpiPaymentInitialisationResponse initiatePeriodicPayment(SpiPeriodicPayment periodicPayment);

    /**
     * Queries ASPSP to add bulk payment to aspsp database
     *
     * @param payments List of SpiSinglePayments representation of initiated bulk payment
     * @return List of payment initialisation responses with related information about initiated payments
     */
    List<SpiPaymentInitialisationResponse> createBulkPayments(List<SpiSinglePayments> payments);

    /**
     * Queries ASPSP to get status of the payment identified by payment id
     *
     * @param paymentId      String representation of ASPSP payment primary identifier
     * @param paymentProduct String representing the beginning of the search period
     * @return Transaction status of the payment identified by given id
     */
    SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct);
}
