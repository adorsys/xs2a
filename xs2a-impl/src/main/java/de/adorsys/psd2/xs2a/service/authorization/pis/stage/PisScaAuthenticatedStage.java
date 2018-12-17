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
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;

@Service("PIS_PSUAUTHENTICATED")
public class PisScaAuthenticatedStage extends PisScaStage<Xs2aUpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> {
    private final SpiContextDataProvider spiContextDataProvider;
    private final PisConsentDataService pisConsentDataService;
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;

    public PisScaAuthenticatedStage(PaymentAuthorisationSpi paymentAuthorisationSpi, PisConsentDataService pisConsentDataService, CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper, SpiErrorMapper spiErrorMapper, SpiContextDataProvider spiContextDataProvider) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.pisConsentDataService = pisConsentDataService;
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.spiErrorMapper = spiErrorMapper;
        this.spiToXs2aAuthenticationObjectMapper = spiToXs2aAuthenticationObjectMapper;
    }

    @Override
    public Xs2aUpdatePisConsentPsuDataResponse apply(Xs2aUpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        PaymentType paymentType = pisConsentAuthorisationResponse.getPaymentType();
        String paymentProduct = pisConsentAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisConsentAuthorisationResponse, paymentType, paymentProduct);
        String authenticationMethodId = request.getAuthenticationMethodId();
        PsuIdData psuData = request.getPsuData();

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(request.getPaymentId());
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = paymentAuthorisationSpi.requestAuthorisationCode(contextData, authenticationMethodId, payment, aspspConsentData);
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
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(SCAMETHODSELECTED);
        response.setPsuId(psuData.getPsuId());
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(spiAuthenticationObject));
        response.setChallengeData(challengeData);
        return response;
    }

}
