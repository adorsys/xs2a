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

package de.adorsys.aspsp.xs2a.spi.impl.v2;

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.v2.PaymentSpi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SinglePaymentSpiImpl implements PaymentSpi<SpiSinglePayment> {
    @Override
    public SpiResponse<SpiSinglePayment> initiatePayment(SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiSinglePayment> getPaymentById(SpiSinglePayment payment, String paymentId, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }
}
