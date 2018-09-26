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

package de.adorsys.aspsp.xs2a.service.authorization;

import de.adorsys.aspsp.xs2a.config.factory.ScaStage;
import de.adorsys.aspsp.xs2a.consent.api.CmsAspspConsentData;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.SpiCmsPisMapper;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus.*;

@Service("STARTED")
public class ScaStartAuthorisationStage extends ScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, UpdatePisConsentPsuDataResponse> {

    public ScaStartAuthorisationStage(PaymentSpi paymentSpi, PisAuthorisationService pisAuthorisationService, SpiCmsPisMapper spiCmsPisMapper) {
        super(paymentSpi, pisAuthorisationService, spiCmsPisMapper);
    }

    @Override
    public UpdatePisConsentPsuDataResponse apply(UpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {
        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = paymentSpi.authorisePsu(request.getPsuId(), request.getPassword(), new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        if (SpiAuthorisationStatus.FAILURE == authorisationStatusSpiResponse.getPayload()) {
            return new UpdatePisConsentPsuDataResponse(FAILED);
        }
        request.setCmsAspspConsentData(new CmsAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData().getBody()));
        List<SpiScaMethod> spiScaMethods = paymentSpi.readAvailableScaMethod(request.getPsuId(), authorisationStatusSpiResponse.getAspspConsentData()).getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            paymentSpi.executePayment(pisConsentAuthorisationResponse.getPaymentType(), pisConsentAuthorisationResponse.getPayments(), authorisationStatusSpiResponse.getAspspConsentData());
            request.setScaStatus(FINALISED);
            return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);

        } else if (isSingleScaMethod(spiScaMethods)) {
            paymentSpi.performStrongUserAuthorisation(request.getPsuId(), new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
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
