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
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;


@Service("PIS_STARTED")
public class PisScaStartAuthorisationStage extends PisScaStage<Xs2aUpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, Xs2aUpdatePisConsentPsuDataResponse> {
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final PisConsentDataService pisConsentDataService;
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;

    public PisScaStartAuthorisationStage(PaymentAuthorisationSpi paymentAuthorisationSpi, PisConsentDataService pisConsentDataService, CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper, SpiErrorMapper spiErrorMapper, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper, SpiContextDataProvider spiContextDataProvider) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.xs2aToSpiPsuDataMapper = xs2aToSpiPsuDataMapper;
        this.pisConsentDataService = pisConsentDataService;
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.spiErrorMapper = spiErrorMapper;
        this.spiToXs2aAuthenticationObjectMapper = spiToXs2aAuthenticationObjectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Xs2aUpdatePisConsentPsuDataResponse apply(Xs2aUpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        PaymentType paymentType = pisConsentAuthorisationResponse.getPaymentType();
        String paymentProduct = pisConsentAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisConsentAuthorisationResponse, paymentType, paymentProduct);

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(request.getPaymentId());

        SpiPsuData psuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(request.getPsuData());
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(request.getPsuData());

        SpiResponse<SpiAuthorisationStatus> authPsuResponse = paymentAuthorisationSpi.authorisePsu(contextData, psuData, request.getPassword(), payment, aspspConsentData);
        aspspConsentData = authPsuResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);

        if (authPsuResponse.hasError()) {
            return new Xs2aUpdatePisConsentPsuDataResponse(spiErrorMapper.mapToErrorHolder(authPsuResponse));
        }

        SpiResponse<List<SpiAuthenticationObject>> availableScaMethodsResponse = paymentAuthorisationSpi.requestAvailableScaMethods(contextData, payment, aspspConsentData);
        pisConsentDataService.updateAspspConsentData(availableScaMethodsResponse.getAspspConsentData());

        if (availableScaMethodsResponse.hasError()) {
            return new Xs2aUpdatePisConsentPsuDataResponse(spiErrorMapper.mapToErrorHolder(availableScaMethodsResponse));
        }

        List<SpiAuthenticationObject> spiScaMethods = availableScaMethodsResponse.getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            PaymentSpi paymentSpi = getPaymentService(pisConsentAuthorisationResponse, paymentType);
            SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentSpi.executePaymentWithoutSca(contextData, payment, availableScaMethodsResponse.getAspspConsentData());
            pisConsentDataService.updateAspspConsentData(executePaymentResponse.getAspspConsentData());

            if (executePaymentResponse.hasError()) {
                return new Xs2aUpdatePisConsentPsuDataResponse(spiErrorMapper.mapToErrorHolder(executePaymentResponse));
            }

            Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(FINALISED);
            response.setPsuId(psuData.getPsuId());
            return response;

        } else if (isSingleScaMethod(spiScaMethods)) {
            SpiAuthenticationObject chosenMethod = spiScaMethods.get(0);
            SpiResponse<SpiAuthorizationCodeResult> authCodeResponse = paymentAuthorisationSpi.requestAuthorisationCode(contextData, chosenMethod.getAuthenticationMethodId(), payment, aspspConsentData);
            pisConsentDataService.updateAspspConsentData(authCodeResponse.getAspspConsentData());

            if (authCodeResponse.hasError()) {
                return new Xs2aUpdatePisConsentPsuDataResponse(spiErrorMapper.mapToErrorHolder(authCodeResponse));
            }

            Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(SCAMETHODSELECTED);
            response.setPsuId(psuData.getPsuId());
            response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenMethod));
            response.setChallengeData(mapToChallengeData(authCodeResponse.getPayload()));
            return response;

        } else if (isMultipleScaMethods(spiScaMethods)) {
            Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(PSUAUTHENTICATED);
            response.setPsuId(psuData.getPsuId());
            response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            return response;
        }
        return new Xs2aUpdatePisConsentPsuDataResponse(FAILED);
    }

    private ChallengeData mapToChallengeData(SpiAuthorizationCodeResult authorizationCodeResult) {
        if (authorizationCodeResult != null && !authorizationCodeResult.isEmpty()) {
            return authorizationCodeResult.getChallengeData();
        }
        return null;
    }

    private boolean isSingleScaMethod(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
