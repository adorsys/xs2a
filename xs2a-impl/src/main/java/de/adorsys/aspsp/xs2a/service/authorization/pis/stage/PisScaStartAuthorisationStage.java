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

import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
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

    public PisScaStartAuthorisationStage(PaymentAuthorisationSpi paymentAuthorisationSpi, PisConsentDataService pisConsentDataService, CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper, Xs2aPisConsentMapper xs2aPisConsentMapper) {
        super(paymentAuthorisationSpi, pisConsentDataService, cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, spiToXs2aAuthenticationObjectMapper, xs2aPisConsentMapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Xs2aUpdatePisConsentPsuDataResponse apply(Xs2aUpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        PaymentType paymentType = pisConsentAuthorisationResponse.getPaymentType();

        SpiPayment payment = mapToSpiPayment(pisConsentAuthorisationResponse.getPayments(), paymentType);

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByPaymentId(request.getPaymentId());

        SpiPsuData psuData = new SpiPsuData(request.getPsuId(), null, null, null);  // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/458

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = paymentAuthorisationSpi.authorisePsu(psuData, request.getPassword(), payment, aspspConsentData);

        aspspConsentData = authorisationStatusSpiResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);

        if (SpiAuthorisationStatus.FAILURE == authorisationStatusSpiResponse.getPayload()) {
            return new Xs2aUpdatePisConsentPsuDataResponse(FAILED);
        }
        SpiResponse<List<SpiAuthenticationObject>> listAvailableScaMethodResponse = paymentAuthorisationSpi.requestAvailableScaMethods(psuData, payment, aspspConsentData);

        aspspConsentData = listAvailableScaMethodResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);
        List<SpiAuthenticationObject> spiScaMethods = listAvailableScaMethodResponse.getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            PaymentSpi paymentSpi = getPaymentService(paymentType);
            SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentSpi.executePaymentWithoutSca(psuData, payment, aspspConsentData);

            aspspConsentData = executePaymentResponse.getAspspConsentData();
            pisConsentDataService.updateAspspConsentData(aspspConsentData);

            Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(FINALISED);
            response.setPsuId(psuData.getPsuId());
            return response;

        } else if (isSingleScaMethod(spiScaMethods)) {
            SpiAuthenticationObject chosenMethod = spiScaMethods.get(0);
            aspspConsentData = paymentAuthorisationSpi.requestAuthorisationCode(psuData, chosenMethod.getAuthenticationMethodId(), payment, aspspConsentData).getAspspConsentData();

            pisConsentDataService.updateAspspConsentData(aspspConsentData);

            Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(SCAMETHODSELECTED);
            response.setPsuId(psuData.getPsuId());
            response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenMethod));
            return response;

        } else if (isMultipleScaMethods(spiScaMethods)) {
            Xs2aUpdatePisConsentPsuDataResponse response = new Xs2aUpdatePisConsentPsuDataResponse(PSUAUTHENTICATED);
            response.setPsuId(psuData.getPsuId());
            response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            return response;
        }
        return new Xs2aUpdatePisConsentPsuDataResponse(FAILED);
    }

    private boolean isSingleScaMethod(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
