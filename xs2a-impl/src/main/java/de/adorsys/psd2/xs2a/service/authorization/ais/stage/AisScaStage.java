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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

/**
 * Abstract class to be extended by each stage of updating consent PSU data workflow
 */
@RequiredArgsConstructor
public abstract class AisScaStage<T, R> implements Function<T, R> {
    protected final Xs2aAisConsentService aisConsentService;
    protected final AisConsentDataService aisConsentDataService;
    protected final AisConsentSpi aisConsentSpi;
    protected final Xs2aAisConsentMapper aisConsentMapper;
    protected final SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;
    protected final Xs2aToSpiPsuDataMapper psuDataMapper;
    protected final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;

    UpdateConsentPsuDataResponse createFailedResponse(MessageErrorCode errorCode, List<String> messages) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setErrorCode(errorCode);
        response.setScaStatus(ScaStatus.FAILED);
        response.setPsuMessage(buildPsuMessage(messages));
        return response;
    }

    private String buildPsuMessage(List<String> messages) {
        return String.join(", ", messages);
    }
}
