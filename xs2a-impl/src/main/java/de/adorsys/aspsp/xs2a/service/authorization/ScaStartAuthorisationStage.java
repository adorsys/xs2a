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
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.SpiCmsPisMapper;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.consent.api.CmsScaStatus.*;

@Service("STARTED")
public class ScaStartAuthorisationStage extends ScaStage<UpdatePisConsentPsuDataRequest, GetPisConsentAuthorisationResponse, UpdatePisConsentPsuDataResponse> {

    public ScaStartAuthorisationStage(PaymentSpi paymentSpi, PisAuthorisationService pisAuthorisationService, SpiCmsPisMapper spiCmsPisMapper, PisConsentDataService pisConsentDataService) {
        super(paymentSpi, pisAuthorisationService, spiCmsPisMapper, pisConsentDataService);
    }

    @Override
    public UpdatePisConsentPsuDataResponse apply(UpdatePisConsentPsuDataRequest request, GetPisConsentAuthorisationResponse pisConsentAuthorisationResponse) {

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByPaymentId(request.getPaymentId());
        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = paymentSpi.authorisePsu(request.getPsuId(),
                                                                                                     request.getPassword(),
                                                                                                     aspspConsentData
                                                                                                    );
        aspspConsentData = authorisationStatusSpiResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);

        if (SpiAuthorisationStatus.FAILURE == authorisationStatusSpiResponse.getPayload()) {
            return new UpdatePisConsentPsuDataResponse(FAILED);
        }
        SpiResponse<List<SpiScaMethod>> listAvailablescaMethodResponse = paymentSpi.readAvailableScaMethod(request.getPsuId(),
                                                                                            aspspConsentData
                                                                                           );
        aspspConsentData = listAvailablescaMethodResponse.getAspspConsentData();
        pisConsentDataService.updateAspspConsentData(aspspConsentData);
        List<SpiScaMethod> spiScaMethods = listAvailablescaMethodResponse.getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            SpiResponse<String> executePaymentResponse = paymentSpi.executePayment(pisConsentAuthorisationResponse.getPaymentType(),
                                                                              pisConsentAuthorisationResponse.getPayments(),
                                                                              aspspConsentData
                                                                             );
            aspspConsentData = executePaymentResponse.getAspspConsentData();
            pisConsentDataService.updateAspspConsentData(aspspConsentData);
            request.setScaStatus(FINALISED);
            return pisAuthorisationService.doUpdatePisConsentAuthorisation(request);

        } else if (isSingleScaMethod(spiScaMethods)) {

            aspspConsentData = paymentSpi.performStrongUserAuthorisation(request.getPsuId(),
                                                                         spiScaMethods.get(0),
                                                                         aspspConsentData
                                                                         )
                                   .getAspspConsentData();
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
