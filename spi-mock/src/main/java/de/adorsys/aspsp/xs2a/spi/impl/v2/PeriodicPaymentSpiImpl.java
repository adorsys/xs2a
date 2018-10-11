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

import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeriodicPaymentSpiImpl implements PeriodicPaymentSpi {
    @NotNull
    @Override
    public SpiResponse<SpiPaymentInitialisationResponse> initiatePayment(@NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiPeriodicPayment> getPaymentById(SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiPeriodicPayment businessObject, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiResponse.VoidResponse> verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiPeriodicPayment businessObject, AspspConsentData aspspConsentData) {
        return null;
    }
}
