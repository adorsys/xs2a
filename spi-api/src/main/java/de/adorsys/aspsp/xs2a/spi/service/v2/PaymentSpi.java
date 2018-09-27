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

package de.adorsys.aspsp.xs2a.spi.service.v2;

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;

import java.util.List;

public interface PaymentSpi<T> {

    SpiResponse<T> initiatePayment(T payment, AspspConsentData aspspConsentData);

    SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, T payment, AspspConsentData aspspConsentData);

    SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, T payment, AspspConsentData aspspConsentData);

    SpiResponse executePaymentWithoutSca(SpiPaymentType spiPaymentType, T payment, AspspConsentData aspspConsentData);

    SpiResponse requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, T payment, AspspConsentData aspspConsentData);

    SpiResponse verifyAuthorisationCodeAndExecutePayment(SpiPaymentConfirmation spiPaymentConfirmation, T payment, AspspConsentData aspspConsentData);

    SpiResponse<T> getPaymentById(T payment, String paymentId, AspspConsentData aspspConsentData);

    SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, T payment, AspspConsentData aspspConsentData);
}
