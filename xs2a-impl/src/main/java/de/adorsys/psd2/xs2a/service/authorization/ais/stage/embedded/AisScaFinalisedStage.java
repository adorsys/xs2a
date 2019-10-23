/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

@Service("AIS_FINALISED")
public class AisScaFinalisedStage extends AisScaStage<UpdateConsentPsuDataReq, AccountConsentAuthorization, UpdateConsentPsuDataResponse> {

    public AisScaFinalisedStage(Xs2aAisConsentService aisConsentService,
                                SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                AisConsentSpi aisConsentSpi,
                                Xs2aAisConsentMapper aisConsentMapper,
                                Xs2aToSpiPsuDataMapper psuDataMapper,
                                SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                SpiErrorMapper spiErrorMapper) {
        super(aisConsentService, aspspConsentDataProviderFactory, aisConsentSpi, aisConsentMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
    }

    // Needed to prevent error in case of trying to update consent PSU data, that already has FINALISED Sca status.
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request, AccountConsentAuthorization authorisationResponse) {
        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, request.getConsentId(), request.getAuthorizationId());
    }
}
