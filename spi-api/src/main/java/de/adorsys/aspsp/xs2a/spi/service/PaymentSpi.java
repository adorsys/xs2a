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
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;

import java.util.List;

public interface PaymentSpi {
    /**
     * Initiates a single payment at ASPSP
     *
     * @param spiSinglePayments single payment to be sent for saving at ASPSP
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayment spiSinglePayments);

    /**
     * Initiates a periodic payment at ASPSP
     *
     * @param periodicPayment periodic payment to be sent for saving at ASPSP
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiPaymentInitialisationResponse initiatePeriodicPayment(SpiPeriodicPayment periodicPayment);

    /**
     * Initiates a bulk payment at ASPSP
     *
     * @param payments bulk payment to be sent for saving at ASPSP
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    List<SpiPaymentInitialisationResponse> createBulkPayments(List<SpiSinglePayment> payments);

    /**
     * Returns a payment status by its ASPSP identifier
     *
     * @param paymentId      ASPSP identifier of a payment
     * @param paymentProduct The addressed payment product
     * @return payment status
     */
    SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct);

    /**
     * Returns a single payment by its ASPSP identifier
     *
     * @param paymentType    Type of payment
     * @param paymentProduct The addressed payment product
     * @param paymentId      ASPSP identifier of a payment
     * @return single payment
     */
    SpiSinglePayment getSinglePaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId);

    /**
     * Returns a periodic payment by its ASPSP identifier
     *
     * @param paymentType    Type of payment
     * @param paymentProduct The addressed payment product
     * @param paymentId      ASPSP identifier of a payment
     * @return periodic payment
     */
    SpiPeriodicPayment getPeriodicPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId);

    /**
     * Returns a bulk payment by its ASPSP identifier
     *
     * @param paymentType    Type of payment
     * @param paymentProduct The addressed payment product
     * @param paymentId      ASPSP identifier of a payment
     * @return bulk payment
     */
    List<SpiSinglePayment> getBulkPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId);
}
