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

    SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments);

    SpiPaymentInitialisationResponse initiatePeriodicPayment(SpiPeriodicPayment periodicPayment);

    List<SpiPaymentInitialisationResponse> createBulkPayments(List<SpiSinglePayments> payments);

    SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct);
}
