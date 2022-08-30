/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiConsent;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class CommonDecoupledConsentService<T extends SpiConsent> {
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aAuthorisationService authorisationService;

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, T spiConsent,
                                                                 String authenticationMethodId, PsuIdData psuData) {
        SpiResponse<SpiAuthorisationDecoupledScaResponse> spiResponse =
            startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData),
                              authorisationId, authenticationMethodId, spiConsent,
                              aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            log.info("Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], Authentication-Method-ID [{}]. Notifies a decoupled app about starting SCA when proceed decoupled approach has failed. Error msg: {}.",
                     consentId, authorisationId, psuData.getPsuId(), authenticationMethodId, errorHolder);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        SpiAuthorisationDecoupledScaResponse spiAuthorisationDecoupledScaResponse = spiResponse.getPayload();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(
            spiAuthorisationDecoupledScaResponse.getScaStatus(),
            consentId, authorisationId, psuData);
        response.setPsuMessage(spiResponse.getPayload().getPsuMessage());
        return response;
    }

    protected abstract ServiceType getServiceType();

    protected abstract SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(SpiContextData spiContextData, String authorisationId, String authenticationMethodId, T spiConsent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);
}
