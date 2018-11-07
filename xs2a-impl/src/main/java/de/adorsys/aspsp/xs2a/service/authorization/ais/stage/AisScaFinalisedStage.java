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

package de.adorsys.aspsp.xs2a.service.authorization.ais.stage;

import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aOtpFormatMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

@Service("AIS_FINALISED")
public class AisScaFinalisedStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {

    public AisScaFinalisedStage(Xs2aAisConsentService aisConsentService,
                                AisConsentDataService aisConsentDataService,
                                AisConsentSpi aisConsentSpi,
                                Xs2aAisConsentMapper aisConsentMapper,
                                SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                Xs2aToSpiPsuDataMapper psuDataMapper,
                                SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                SpiToXs2aOtpFormatMapper spiToXs2aOtpFormatMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiToXs2aOtpFormatMapper);
    }

    // Needed to prevent error in case of trying to update consent PSU data, that already has FINALISED Sca status.
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED);
    }
}
