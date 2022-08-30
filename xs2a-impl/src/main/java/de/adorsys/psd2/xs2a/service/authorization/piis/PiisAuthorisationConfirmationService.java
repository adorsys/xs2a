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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PiisAuthorisationConfirmationService extends ConsentAuthorisationConfirmationService<PiisConsent> {
    private final Xs2aPiisConsentService xs2aPiisConsentService;
    private final PiisConsentSpi piisConsentSpi;
    private final Xs2aToSpiPiisConsentMapper piisConsentMapper;

    public PiisAuthorisationConfirmationService(AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                                SpiContextDataProvider spiContextDataProvider,
                                                SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                Xs2aAuthorisationService authorisationService,
                                                SpiErrorMapper spiErrorMapper,
                                                AuthorisationServiceEncrypted authorisationServiceEncrypted,
                                                Xs2aPiisConsentService xs2aPiisConsentService,
                                                PiisConsentSpi piisConsentSpi,
                                                Xs2aToSpiPiisConsentMapper piisConsentMapper) {
        super(aspspProfileServiceWrapper, spiContextDataProvider, aspspConsentDataProviderFactory, authorisationService, spiErrorMapper, authorisationServiceEncrypted);
        this.xs2aPiisConsentService = xs2aPiisConsentService;
        this.piisConsentSpi = piisConsentSpi;
        this.piisConsentMapper = piisConsentMapper;
    }

    @Override
    protected ErrorType getErrorType403() {
        return ErrorType.PIIS_403;
    }

    @Override
    protected void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        xs2aPiisConsentService.updateConsentStatus(consentId, consentStatus);
    }

    @Override
    protected void findAndTerminateOldConsents(String consentId, PiisConsent consent) {
        // this method is empty because one tpp could have more then one valid piis consent

    }

    @Override
    protected SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(SpiContextData spiContextData, boolean isCodeCorrect, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.notifyConfirmationCodeValidation(spiContextData, isCodeCorrect, piisConsentMapper.mapToSpiPiisConsent(consent), spiAspspConsentDataProvider);
    }

    @Override
    protected Optional<PiisConsent> getConsentById(String consentId) {
        return xs2aPiisConsentService.getPiisConsentById(consentId);
    }

    @Override
    protected SpiResponse<SpiConsentConfirmationCodeValidationResponse> checkConfirmationCode(SpiContextData spiContextData, SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.checkConfirmationCode(spiContextData, spiCheckConfirmationCodeRequest, spiAspspConsentDataProvider);
    }

    @Override
    protected boolean checkConfirmationCodeInternally(String authorisationId, String confirmationCode, String scaAuthenticationData, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return piisConsentSpi.checkConfirmationCodeInternally(authorisationId, confirmationCode, scaAuthenticationData, aspspConsentDataProvider);
    }

    @Override
    protected ErrorType getErrorType400() {
        return ErrorType.PIIS_400;
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.PIIS;
    }
}
