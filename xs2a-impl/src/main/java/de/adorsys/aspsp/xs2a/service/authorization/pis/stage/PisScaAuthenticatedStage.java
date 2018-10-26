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

package de.adorsys.aspsp.xs2a.service.authorization.pis.stage;

import de.adorsys.aspsp.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.SpiCmsPisMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;

@Slf4j
@Service("PIS_PSUAUTHENTICATED")
public class PisScaAuthenticatedStage extends PisScaStage<Xs2aUpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> {

    public PisScaAuthenticatedStage(PisAuthorisationService pisAuthorisationService, PaymentAuthorisationSpi paymentAuthorisationSpi, SpiCmsPisMapper spiCmsPisMapper, PisConsentDataService pisConsentDataService, CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, SpiErrorMapper spiErrorMapper) {
        super(pisAuthorisationService, paymentAuthorisationSpi, spiCmsPisMapper, pisConsentDataService, cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, spiErrorMapper);
    }

    @Override
    public Xs2aUpdatePisConsentPsuDataResponse apply(Xs2aUpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        PaymentType paymentType = pisConsentAuthorisationResponse.getPaymentType();
        SpiPayment payment = mapToSpiPayment(pisConsentAuthorisationResponse.getPayments(), paymentType);

        SpiPsuData psuData = new SpiPsuData(request.getPsuId(), null, null, null);  // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/458
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByPaymentId(request.getPaymentId());

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = paymentAuthorisationSpi.requestAuthorisationCode(psuData, getMethod(request.getAuthenticationMethodId()), payment, aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new Xs2aUpdatePisConsentPsuDataResponse(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        request.setScaStatus(SCAMETHODSELECTED);
        return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);
    }

    private SpiScaMethod getMethod(String method) { //TODO: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/411
        SpiScaMethod scaMethod = SpiScaMethod.SMS_OTP;
        try {
            scaMethod = SpiScaMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            log.error("Sca Method could not be parsed", e.getLocalizedMessage());
        }
        return scaMethod;
    }
}
