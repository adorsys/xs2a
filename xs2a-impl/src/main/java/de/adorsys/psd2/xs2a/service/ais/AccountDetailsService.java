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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetAccountDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;

@Slf4j
@Service
@AllArgsConstructor
public class AccountDetailsService {
    private final AccountSpi accountSpi;

    private final SpiToXs2aAccountDetailsMapper accountDetailsMapper;

    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetAccountDetailsValidator getAccountDetailsValidator;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;
    private final LoggingContextService loggingContextService;

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent, depending on
     * withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an Consent identification
     * @param accountId   String representing a PSU`s Account at ASPSP
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @param requestUri  the URI of incoming request
     * @return response with {@link Xs2aAccountDetailsHolder} based on accountId with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountDetailsHolder> getAccountDetails(String consentId, String accountId,
                                                                      boolean withBalance, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_ACCOUNT_DETAILS_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get account details failed. Account consent not found by id",
                     accountId, consentId);
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();
        ValidationResult validationResult = getValidationResultForCommonAccountRequest(accountId, withBalance, requestUri, aisConsent);

        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get account details - validation failed: {}",
                     accountId, consentId, withBalance, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<SpiAccountDetails> spiResponse = getSpiResponse(aisConsent, consentId, accountId, withBalance);

        if (spiResponse.hasError()) {
            return checkSpiResponse(consentId, accountId, spiResponse);
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aAccountDetailsHolderResponseObject(consentId, withBalance, requestUri, aisConsent, spiResponse.getPayload());
    }

    private ValidationResult getValidationResultForCommonAccountRequest(String accountId, boolean withBalance, String requestUri, AisConsent aisConsent) {
        CommonAccountRequestObject validatorObject = new CommonAccountRequestObject(aisConsent, accountId, withBalance, requestUri);
        return getAccountDetailsValidator.validate(validatorObject);
    }

    private SpiResponse<SpiAccountDetails> getSpiResponse(AisConsent aisConsent, String consentId,
                                                          String accountId, boolean withBalance) {
        AccountAccess access = aisConsent.getAspspAccountAccesses();
        SpiAccountReference requestedAccountReference = aisConsent.isGlobalConsent() ?
                                                            SpiAccountReference.builder()
                                                                .resourceId(accountId)
                                                                .build() :
                                                            accountHelperService.findAccountReference(access.getAccounts(), accountId);

        return accountSpi.requestAccountDetailForAccount(accountHelperService.getSpiContextData(),
                                                         withBalance, requestedAccountReference,
                                                         consentMapper.mapToSpiAccountConsent(aisConsent),
                                                         aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    private ResponseObject<Xs2aAccountDetailsHolder> checkSpiResponse(String consentId, String accountId, SpiResponse<SpiAccountDetails> spiResponse) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("Account-ID [{}], Consent-ID: [{}]. Get account details failed: couldn't get account details. Error msg: [{}]",
                 accountId, consentId, errorHolder);
        return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                   .fail(errorHolder)
                   .build();
    }

    @NotNull
    private ResponseObject<Xs2aAccountDetailsHolder> getXs2aAccountDetailsHolderResponseObject(String consentId,
                                                                                               boolean withBalance,
                                                                                               String requestUri,
                                                                                               AisConsent aisConsent,
                                                                                               SpiAccountDetails spiAccountDetails) {
        Xs2aAccountDetails accountDetails = accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails);
        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = new Xs2aAccountDetailsHolder(accountDetails, aisConsent);

        ResponseObject<Xs2aAccountDetailsHolder> response = ResponseObject.<Xs2aAccountDetailsHolder>builder()
                                                                .body(xs2aAccountDetailsHolder)
                                                                .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(withBalance, TypeAccess.ACCOUNT, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(aisConsent),
                                           spiAccountDetails.getResourceId(), null);

        return response;
    }
}
