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

import de.adorsys.aspsp.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.SpiCmsPisMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
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
public class PisScaStartAuthorisationStage extends PisScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, UpdatePisConsentPsuDataResponse> {

    public PisScaStartAuthorisationStage(PisAuthorisationService pisAuthorisationService, PaymentAuthorisationSpi paymentAuthorisationSpi, SpiCmsPisMapper spiCmsPisMapper, PisConsentDataService pisConsentDataService, CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper) {
        super(pisAuthorisationService, paymentAuthorisationSpi, spiCmsPisMapper, pisConsentDataService, cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, xs2aToSpiPsuDataMapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public UpdatePisConsentPsuDataResponse apply(UpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        PaymentType paymentType = pisConsentAuthorisationResponse.getPaymentType();

        SpiPayment payment = mapToSpiPayment(pisConsentAuthorisationResponse.getPayments(), paymentType);

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByPaymentId(request.getPaymentId());

        SpiPsuData psuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(request.getPsuData());

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = paymentAuthorisationSpi.authorisePsu(psuData, request.getPassword(), payment, aspspConsentData);

        aspspConsentData = authorisationStatusSpiResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);

        if (SpiAuthorisationStatus.FAILURE == authorisationStatusSpiResponse.getPayload()) {
            return new UpdatePisConsentPsuDataResponse(FAILED);
        }
        SpiResponse<List<SpiScaMethod>> listAvailableScaMethodResponse = paymentAuthorisationSpi.requestAvailableScaMethods(psuData, payment, aspspConsentData);

        aspspConsentData = listAvailableScaMethodResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);
        List<SpiScaMethod> spiScaMethods = listAvailableScaMethodResponse.getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            PaymentSpi paymentSpi = getPaymentService(paymentType);
            SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentSpi.executePaymentWithoutSca(psuData, payment, aspspConsentData);

            aspspConsentData = executePaymentResponse.getAspspConsentData();
            pisConsentDataService.updateAspspConsentData(aspspConsentData);
            request.setScaStatus(FINALISED);
            return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);

        } else if (isSingleScaMethod(spiScaMethods)) {
            aspspConsentData = paymentAuthorisationSpi.requestAuthorisationCode(psuData, spiScaMethods.get(0), payment, aspspConsentData).getAspspConsentData();

            pisConsentDataService.updateAspspConsentData(aspspConsentData);
            request.setScaStatus(SCAMETHODSELECTED);
            request.setAuthenticationMethodId(spiScaMethods.get(0).name());
            return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);

        } else if (isMultipleScaMethods(spiScaMethods)) {
            request.setScaStatus(PSUAUTHENTICATED);
            UpdatePisConsentPsuDataResponse response = pisAuthorisationService.doUpdatePisConsentAuthorisation(request);
            response.setAvailableScaMethods(spiCmsPisMapper.mapToCmsScaMethods(spiScaMethods));
            return response;

        }
        return new UpdatePisConsentPsuDataResponse(FAILED);
    }

    private boolean isSingleScaMethod(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
