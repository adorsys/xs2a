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

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;

@Service("AIS_PSUIDENTIFIED")
public class AisScaIdentifiedAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private final AisScaStageAuthorisationFactory scaStageAuthorisationFactory;

    public AisScaIdentifiedAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                              AisConsentDataService aisConsentDataService,
                                              AisConsentSpi aisConsentSpi,
                                              Xs2aAisConsentMapper aisConsentMapper,
                                              SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                              Xs2aToSpiPsuDataMapper psuDataMapper,
                                              SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                              SpiErrorMapper spiErrorMapper,
                                              AisScaStageAuthorisationFactory scaStageAuthorisationFactory) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.scaStageAuthorisationFactory = scaStageAuthorisationFactory;
    }

    /**
     * Psu identified authorisation stage workflow: PSU authorising process using data from request
     * (returns response with FAILED status in case of non-successful authorising), available SCA methods getting
     * and performing the flow according to none, one or multiple available methods.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> service = scaStageAuthorisationFactory.getService(SERVICE_PREFIX + SEPARATOR + ScaStatus.STARTED.name());
        return service.apply(request);
    }
}
