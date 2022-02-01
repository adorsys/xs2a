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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AisAuthorisationConfirmationService extends ConsentAuthorisationConfirmationService<AisConsent> {
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aAisConsentMapper aisConsentMapper;

    public AisAuthorisationConfirmationService(AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                               SpiContextDataProvider spiContextDataProvider,
                                               SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                               Xs2aAuthorisationService authorisationService,
                                               SpiErrorMapper spiErrorMapper,
                                               AuthorisationServiceEncrypted authorisationServiceEncrypted,
                                               Xs2aAisConsentService aisConsentService,
                                               AisConsentSpi aisConsentSpi,
                                               Xs2aAisConsentMapper aisConsentMapper) {
        super(aspspProfileServiceWrapper, spiContextDataProvider, aspspConsentDataProviderFactory, authorisationService, spiErrorMapper, authorisationServiceEncrypted);
        this.aisConsentService = aisConsentService;
        this.aisConsentSpi = aisConsentSpi;
        this.aisConsentMapper = aisConsentMapper;
    }

    @Override
    protected ErrorType getErrorType403() {
        return ErrorType.AIS_403;
    }

    @Override
    protected void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        aisConsentService.updateConsentStatus(consentId, consentStatus);
    }

    @Override
    protected void findAndTerminateOldConsents(String consentId, AisConsent consent) {
        var request = new TerminateOldConsentsRequest(consent.isOneAccessType(),
                                                      consent.isWrongConsentData(),
                                                      consent.getPsuIdDataList(),
                                                      consent.getTppInfo().getAuthorisationNumber(),
                                                      consent.getInstanceId());
        aisConsentService.findAndTerminateOldConsents(consentId, request);
    }

    @Override
    protected SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(SpiContextData spiContextData, boolean isCodeCorrect, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.notifyConfirmationCodeValidation(spiContextData, isCodeCorrect, aisConsentMapper.mapToSpiAccountConsent(consent), spiAspspConsentDataProvider);
    }

    @Override
    protected Optional<AisConsent> getConsentById(String consentId) {
        return aisConsentService.getAccountConsentById(consentId);
    }

    @Override
    protected SpiResponse<SpiConsentConfirmationCodeValidationResponse> checkConfirmationCode(SpiContextData spiContextData, SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.checkConfirmationCode(spiContextData, spiCheckConfirmationCodeRequest, spiAspspConsentDataProvider);
    }

    @Override
    protected boolean checkConfirmationCodeInternally(String authorisationId, String confirmationCode, String scaAuthenticationData, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return aisConsentSpi.checkConfirmationCodeInternally(authorisationId, confirmationCode, scaAuthenticationData, aspspConsentDataProvider);
    }

    @Override
    protected ErrorType getErrorType400() {
        return ErrorType.AIS_400;
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.AIS;
    }
}
