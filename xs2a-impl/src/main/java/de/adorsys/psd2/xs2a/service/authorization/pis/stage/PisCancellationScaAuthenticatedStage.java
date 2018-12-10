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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage;


import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;

@Service("PIS_CANC_PSUAUTHENTICATED")
public class PisCancellationScaAuthenticatedStage extends PisScaStage<Xs2aUpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> {

    public PisCancellationScaAuthenticatedStage(PaymentAuthorisationSpi paymentAuthorisationSpi, PaymentCancellationSpi paymentCancellationSpi, PisConsentDataService pisConsentDataService, PisConsentService pisConsentService, CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper, Xs2aPisConsentMapper xs2aPisConsentMapper, SpiErrorMapper spiErrorMapper, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper) {
        super(paymentAuthorisationSpi, paymentCancellationSpi, pisConsentDataService, pisConsentService, cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, spiToXs2aAuthenticationObjectMapper, xs2aPisConsentMapper, spiErrorMapper, xs2aToSpiPsuDataMapper);
    }

    @Override
    public Xs2aUpdatePisConsentPsuDataResponse apply(Xs2aUpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        PaymentType paymentType = pisConsentAuthorisationResponse.getPaymentType();
        SpiPayment payment = mapToSpiPayment(pisConsentAuthorisationResponse.getPayments(), paymentType);
        String authenticationMethodId = request.getAuthenticationMethodId();

        PsuIdData psuData = request.getPsuData();
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(request.getPaymentId());

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = paymentCancellationSpi.requestAuthorisationCode(xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData), authenticationMethodId, payment, aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new Xs2aUpdatePisConsentPsuDataResponse(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();

        if (authorizationCodeResult.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.SCA_METHOD_UNKNOWN)
                                          .build();
            return new Xs2aUpdatePisConsentPsuDataResponse(errorHolder);
        }

        SpiAuthenticationObject spiAuthenticationObject = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = mapToChallengeData(authorizationCodeResult);

        Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(SCAMETHODSELECTED);
        response.setPsuId(psuData.getPsuId());
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(spiAuthenticationObject));
        response.setChallengeData(challengeData);
        return response;
    }

    private ChallengeData mapToChallengeData(SpiAuthorizationCodeResult authorizationCodeResult) {
        if(authorizationCodeResult != null && !authorizationCodeResult.isEmpty()) {
            return authorizationCodeResult.getChallengeData();
        }
        return null;
    }
}
