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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


// TODO add add checking SpiResponse on error https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/450
@Service
@RequiredArgsConstructor
public class RedirectAndEmbeddedPaymentService implements ScaPaymentService {
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;
    private final Xs2aToSpiSinglePaymentMapper xs2AToSpiSinglePaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final PisConsentDataService pisConsentDataService;
    private final SpiErrorMapper spiErrorMapper;

    @Override
    public SinglePaymentInitiationResponse createSinglePayment(SinglePayment payment, TppInfo tppInfo, PaymentProduct paymentProduct, Xs2aPisConsent pisConsent) {
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByConsentId(pisConsent.getConsentId());
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/458

        SpiResponse<SpiSinglePaymentInitiationResponse> spiResponse = singlePaymentSpi.initiatePayment(psuData, xs2AToSpiSinglePaymentMapper.mapToSpiSinglePayment(payment, paymentProduct), aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new SinglePaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse.getPayload(), SinglePaymentInitiationResponse::new);
    }

    @Override
    public PeriodicPaymentInitiationResponse createPeriodicPayment(PeriodicPayment payment, TppInfo tppInfo, PaymentProduct paymentProduct, Xs2aPisConsent pisConsent) {
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByConsentId(pisConsent.getConsentId());
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/458

        SpiResponse<SpiPeriodicPaymentInitiationResponse> spiResponse = periodicPaymentSpi.initiatePayment(psuData, xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(payment, paymentProduct), aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new PeriodicPaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse.getPayload(), PeriodicPaymentInitiationResponse::new);
    }

    @Override
    public BulkPaymentInitiationResponse createBulkPayment(BulkPayment bulkPayment, TppInfo tppInfo, PaymentProduct paymentProduct, Xs2aPisConsent pisConsent) {
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByConsentId(pisConsent.getConsentId());
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/458

        SpiResponse<SpiBulkPaymentInitiationResponse> spiResponse = bulkPaymentSpi.initiatePayment(psuData, xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, paymentProduct), aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new BulkPaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse.getPayload(), BulkPaymentInitiationResponse::new);
    }
}
