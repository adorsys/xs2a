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
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionParameters;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.CardAccountHandler;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAccountService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiCardTransactionListToXs2aAccountReportMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetCardTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CardTransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardTransactionReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransactionReportParameters;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CardAccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@AllArgsConstructor
public class CardTransactionService {

    private final CardAccountSpi cardAccountSpi;
    private final SpiToXs2aBalanceMapper balanceMapper;
    private final SpiCardTransactionListToXs2aAccountReportMapper cardTransactionListToXs2aAccountReportMapper;
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAccountService xs2aAccountService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;
    private final GetCardTransactionsReportValidator getCardTransactionsReportValidator;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;
    private final LoggingContextService loggingContextService;
    private final CardAccountHandler cardAccountHandler;

    /**
     * Read Card transaction reports of a given account addressed by "account-id", depending on the steering parameter
     * "bookingStatus".  For a given account, additional parameters are e.g. the attributes
     * "dateFrom" and "dateTo".  The ASPSP might add balance information, if transaction lists without balances are
     * not supported.
     *
     * @param request Xs2aTransactionsReportByPeriodRequest object which contains information for building Xs2aCardTransactionsReport
     * @return CardTransactionsReport filled with appropriate card transaction arrays Booked and Pending.
     */
    public ResponseObject<Xs2aCardTransactionsReport> getCardTransactionsReportByPeriod(Xs2aCardTransactionsReportByPeriodRequest request) {
        xs2aEventService.recordConsentTppRequest(request.getConsentId(), EventType.READ_CARD_TRANSACTION_LIST_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(request.getConsentId());

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get card transactions report by period failed. Account consent not found by ID",
                     request.getAccountId(), request.getConsentId());
            return ResponseObject.<Xs2aCardTransactionsReport>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();
        ValidationResult validationResult = getValidationResultForTransactionsReportByPeriod(request, aisConsent);

        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get transactions report by period - validation failed: {}",
                     request.getAccountId(), request.getConsentId(),
                     request.getRequestUri(), validationResult.getMessageError());
            return ResponseObject.<Xs2aCardTransactionsReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<SpiCardTransactionReport> spiResponse = getSpiResponseSpiCardTransactionReport(request, aisConsent);

        if (spiResponse.hasError()) {
            return checkSpiResponseForCardTransactionsReport(request, spiResponse);
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        xs2aAccountService.saveTransactionParameters(request.getConsentId(), request.getAccountId(), new Xs2aTransactionParameters(spiResponse.getPayload().getCardTransactions().size(), 1, request.getBookingStatus()));

        return getXs2aCardTransactionsReportResponseObject(request, aisConsent, spiResponse.getPayload());
    }

    private ValidationResult getValidationResultForTransactionsReportByPeriod(Xs2aCardTransactionsReportByPeriodRequest request,
                                                                              AisConsent aisConsent) {

        CardTransactionsReportByPeriodObject validatorObject = new CardTransactionsReportByPeriodObject(aisConsent,
                                                                                                        request.getAccountId(),
                                                                                                        request.getRequestUri(),
                                                                                                        request.getDeltaList(),
                                                                                                        request.getAcceptHeader(),
                                                                                                        request.getBookingStatus(),
                                                                                                        request.getDateFrom(),
                                                                                                        request.getDateTo());
        return getCardTransactionsReportValidator.validate(validatorObject);
    }

    @NotNull
    private SpiResponse<SpiCardTransactionReport> getSpiResponseSpiCardTransactionReport(Xs2aCardTransactionsReportByPeriodRequest request,
                                                                                         AisConsent aisConsent) {
        return cardAccountSpi.requestCardTransactionsForAccount(accountHelperService.getSpiContextData(),
                                                                buildSpiTransactionReportParameters(request),
                                                                getRequestedAccountReference(aisConsent, request.getAccountId()),
                                                                consentMapper.mapToSpiAccountConsent(aisConsent),
                                                                aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getConsentId()));
    }

    private SpiTransactionReportParameters buildSpiTransactionReportParameters(Xs2aCardTransactionsReportByPeriodRequest request) {
        return new SpiTransactionReportParameters(request.getAcceptHeader(), Boolean.FALSE, request.getDateFrom(), request.getDateTo(),
                                                  request.getBookingStatus(), request.getEntryReferenceFrom(), request.getDeltaList(), null, null);
    }

    private SpiAccountReference getRequestedAccountReference(AisConsent aisConsent, String accountId) {
        AccountAccess access = aisConsent.getAspspAccountAccesses();
        return accountHelperService.findAccountReference(access.getTransactions(), accountId);
    }

    private ResponseObject<Xs2aCardTransactionsReport> checkSpiResponseForCardTransactionsReport(Xs2aCardTransactionsReportByPeriodRequest request,
                                                                                                 SpiResponse<SpiCardTransactionReport> spiResponse) {
        // in this particular call we use NOT_SUPPORTED to indicate that requested Content-type is not ok for us
        if (spiResponse.getErrors().get(0).getErrorCode() == SERVICE_NOT_SUPPORTED) {
            log.info("Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: requested content-type not json or text.",
                     request.getAccountId(), request.getConsentId());
            return ResponseObject.<Xs2aCardTransactionsReport>builder()
                       .fail(ErrorType.AIS_406, TppMessageInformation.of(REQUESTED_FORMATS_INVALID))
                       .build();
        }

        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: Request transactions for account fail at SPI level: {}",
                 request.getAccountId(), request.getConsentId(), errorHolder);
        return ResponseObject.<Xs2aCardTransactionsReport>builder()
                   .fail(errorHolder)
                   .build();
    }

    @NotNull
    private ResponseObject<Xs2aCardTransactionsReport> getXs2aCardTransactionsReportResponseObject(Xs2aCardTransactionsReportByPeriodRequest request,
                                                                                                   AisConsent aisConsent,
                                                                                                   SpiCardTransactionReport spiTransactionReport) {
        Xs2aCardTransactionsReport transactionsReport = mapToCardTransactionsReport(request, aisConsent, spiTransactionReport);
        ResponseObject<Xs2aCardTransactionsReport> response = ResponseObject.<Xs2aCardTransactionsReport>builder()
                                                                  .body(transactionsReport)
                                                                  .build();

        AccountReference accountReference = transactionsReport.getAccountReference();
        aisConsentService.consentActionLog(tppService.getTppId(),
                                           request.getConsentId(),
                                           accountHelperService.createActionStatus(false, TypeAccess.TRANSACTION, response),
                                           request.getRequestUri(),
                                           accountHelperService.needsToUpdateUsage(aisConsent),
                                           accountReference == null ? null : accountReference.getResourceId(), null);
        return response;
    }

    @NotNull
    private Xs2aCardTransactionsReport mapToCardTransactionsReport(Xs2aCardTransactionsReportByPeriodRequest request,
                                                                   AisConsent accountConsent,
                                                                   SpiCardTransactionReport spiTransactionReport) {
        Xs2aCardAccountReport report = cardTransactionListToXs2aAccountReportMapper
                                           .mapToXs2aCardAccountReport(request.getBookingStatus(),
                                                                       spiTransactionReport.getCardTransactions(),
                                                                       spiTransactionReport.getTransactionsRaw())
                                           .orElse(null);

        Xs2aCardTransactionsReport cardTransactionsReport = getXs2aCardTransactionsReport(report,
                                                                                          filterAccountReference(accountConsent.getAccess().getTransactions(), request.getAccountId()),
                                                                                          spiTransactionReport);
        if (spiTransactionReport.getDownloadId() != null) {
            String encodedDownloadId = Base64.getUrlEncoder().encodeToString(spiTransactionReport.getDownloadId().getBytes());
            cardTransactionsReport.setDownloadId(encodedDownloadId);
        }
        return cardTransactionsReport;
    }

    private Xs2aCardTransactionsReport getXs2aCardTransactionsReport(Xs2aCardAccountReport report, AccountReference requestedAccountReference,
                                                                     SpiCardTransactionReport spiCardTransactionReport) {
        Xs2aCardTransactionsReport transactionsReport = new Xs2aCardTransactionsReport();
        transactionsReport.setCardAccountReport(report);
        transactionsReport.setAccountReference(getMaskedAccountReference(requestedAccountReference));
        transactionsReport.setBalances(balanceMapper.mapToXs2aBalanceList(spiCardTransactionReport.getBalances()));
        transactionsReport.setResponseContentType(spiCardTransactionReport.getResponseContentType());
        return transactionsReport;
    }

    private AccountReference getMaskedAccountReference(AccountReference filteredAccountReference) {
        if (filteredAccountReference != null && StringUtils.isNotBlank(filteredAccountReference.getPan())) {
            String maskedPan = cardAccountHandler.hidePanInAccountReference(filteredAccountReference.getPan());

            filteredAccountReference.setPan(null);
            filteredAccountReference.setMaskedPan(maskedPan);
        }

        return filteredAccountReference;
    }

    private AccountReference filterAccountReference(List<AccountReference> references, String resourceId) {

        if (references == null) {
            return null;
        }

        return references.stream()
                   .filter(accountReference -> StringUtils.equals(accountReference.getResourceId(), resourceId))
                   .findFirst()
                   .orElse(null);
    }

}
