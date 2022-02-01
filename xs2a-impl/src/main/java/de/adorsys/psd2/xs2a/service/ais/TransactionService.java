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
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAccountService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.service.validator.ais.account.DownloadTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountTransactionsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionService {

    private final AccountSpi accountSpi;

    private final SpiToXs2aBalanceMapper balanceMapper;
    private final SpiToXs2aAccountReferenceMapper referenceMapper;
    private final SpiTransactionListToXs2aAccountReportMapper transactionsToAccountReportMapper;
    private final SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;
    private final SpiToXs2aDownloadTransactionsMapper spiToXs2aDownloadTransactionsMapper;

    private final ValueValidatorService validatorService;
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAccountService xs2aAccountService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetTransactionsReportValidator getTransactionsReportValidator;
    private final DownloadTransactionsReportValidator downloadTransactionsReportValidator;
    private final GetTransactionDetailsValidator getTransactionDetailsValidator;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;

    private final LoggingContextService loggingContextService;

    /**
     * Read Transaction reports of a given account addressed by "account-id", depending on the steering parameter
     * "bookingStatus" together with balances.  For a given account, additional parameters are e.g. the attributes
     * "dateFrom" and "dateTo".  The ASPSP might add balance information, if transaction lists without balances are
     * not supported.
     *
     * @param request Xs2aTransactionsReportByPeriodRequest object which contains information for building Xs2aTransactionsReport
     * @return TransactionsReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances
     * sections is added
     */
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(Xs2aTransactionsReportByPeriodRequest request) {
        xs2aEventService.recordConsentTppRequest(request.getConsentId(), EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(request.getConsentId());

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get transactions report by period failed. Account consent not found by ID",
                     request.getAccountId(), request.getConsentId());
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();
        ValidationResult validationResult = getValidationResultForTransactionsReportByPeriod(request, aisConsent);

        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get transactions report by period - validation failed: {}",
                     request.getAccountId(), request.getConsentId(), request.isWithBalance(),
                     request.getRequestUri(), validationResult.getMessageError());
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        var spiAccountReference = getRequestedAccountReference(aisConsent, request.getAccountId());
        if (spiAccountReference == null) {
            log.info("Account-ID [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get transactions report by period with incorrect account id",
                     request.getAccountId(), request.getConsentId(), request.isWithBalance(),
                     request.getRequestUri());
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(new MessageError(AIS_400, TppMessageInformation.of(FORMAT_ERROR_UNKNOWN_ACCOUNT)))
                       .build();
        }

        SpiResponse<SpiTransactionReport> spiResponse = getSpiResponseSpiTransactionReport(request, aisConsent, spiAccountReference);

        if (spiResponse.hasError()) {
            return checkSpiResponseForTransactionsReport(request, spiResponse);
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        SpiTransactionReport spiTransactionReport = spiResponse.getPayload();
        List<SpiTransaction> spiTransactions = spiTransactionReport.getTransactions();

        if (CollectionUtils.isNotEmpty(spiTransactions)) {
            xs2aAccountService.saveTransactionParameters(request.getConsentId(), request.getAccountId(),
                                                         new Xs2aTransactionParameters(getNumberOfTransactions(spiTransactions, request.getBookingStatus()),
                                                                                       spiTransactionReport.getTotalPages(),
                                                                                       request.getBookingStatus()));
        }

        return getXs2aTransactionsReportResponseObject(request, aisConsent, spiTransactionReport);
    }

    /**
     * Gets transaction details by transaction ID
     *
     * @param consentId     String representing an Consent identification
     * @param accountId     String representing a PSU`s Account at ASPSP
     * @param transactionId String representing the ASPSP identification of transaction
     * @param requestUri    the URI of incoming request
     * @return Transactions based on transaction ID.
     */
    public ResponseObject<Transactions> getTransactionDetails(String consentId, String accountId, String transactionId, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get transaction details failed. Account consent not found by ID",
                     accountId, consentId);
            return ResponseObject.<Transactions>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent accountConsent = aisConsentOptional.get();
        ValidationResult validationResult = getValidationResultForCommonAccountTransactions(accountId, requestUri, accountConsent);

        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get transaction details - validation failed: {}",
                     accountId, consentId, requestUri, validationResult.getMessageError());
            return ResponseObject.<Transactions>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<SpiTransaction> spiResponse = getSpiResponseSpiTransaction(accountConsent, consentId, accountId, transactionId);

        if (spiResponse.hasError()) {
            return checkSpiResponseForTransactions(consentId, accountId, spiResponse);
        }

        loggingContextService.storeConsentStatus(accountConsent.getConsentStatus());

        return getTransactionsResponseObject(consentId, requestUri, accountConsent, spiResponse.getPayload(), accountId);
    }

    /**
     * Gets stream with transaction list by consent ID, account ID and download ID
     *
     * @param consentId  String representing an Consent identification
     * @param accountId  String representing a PSU`s Account at ASPSP
     * @param downloadId String representing the download identifier
     * @return Response with transaction list stream.
     */
    public ResponseObject<Xs2aTransactionsDownloadResponse> downloadTransactions(String consentId, String accountId, String downloadId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.DOWNLOAD_TRANSACTION_LIST_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed. Account consent not found by ID",
                     consentId, accountId, downloadId);
            return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();
        ValidationResult validationResult = getValidationResultForDownloadTransactionRequest(aisConsent, accountId);

        if (validationResult.isNotValid()) {
            log.info("Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions - validation failed: {}",
                     consentId, accountId, downloadId, validationResult.getMessageError());
            return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<SpiTransactionsDownloadResponse> spiResponse = getSpiResponseSpiTransactionsDownloadResponse(aisConsent, consentId, downloadId);

        if (spiResponse.hasError()) {
            return checkSpiResponseForTransactionDownloadResponse(consentId, accountId, downloadId, spiResponse);
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aTransactionsDownloadResponseResponseObject(spiResponse.getPayload());
    }

    private ValidationResult getValidationResultForTransactionsReportByPeriod(Xs2aTransactionsReportByPeriodRequest request,
                                                                              AisConsent aisConsent) {
        TransactionsReportByPeriodObject validatorObject = new TransactionsReportByPeriodObject(aisConsent,
                                                                                                request.getAccountId(),
                                                                                                request.isWithBalance(),
                                                                                                request.getRequestUri(),
                                                                                                request.getEntryReferenceFrom(),
                                                                                                request.getDeltaList(),
                                                                                                request.getAcceptHeader(),
                                                                                                request.getBookingStatus(),
                                                                                                request.getDateFrom());
        return getTransactionsReportValidator.validate(validatorObject);
    }

    private int getNumberOfTransactions(List<SpiTransaction> spiTransactions, BookingStatus bookingStatus) {
        if (bookingStatus == BookingStatus.INFORMATION) {
            //since we can not read transaction details for INFORMATION transaction status, we don't need real number of this kind of transactions
            return 1;
        }
        return spiTransactions.stream()
                   .filter(t -> !t.isInformationTransaction())
                   .collect(Collectors.toList())
                   .size();
    }

    private ValidationResult getValidationResultForCommonAccountTransactions(String accountId, String requestUri,
                                                                             AisConsent aisConsent) {
        CommonAccountTransactionsRequestObject validatorObject = new CommonAccountTransactionsRequestObject(aisConsent,
                                                                                                            accountId,
                                                                                                            requestUri);
        return getTransactionDetailsValidator.validate(validatorObject);
    }

    private ValidationResult getValidationResultForDownloadTransactionRequest(AisConsent aisConsent, String accountId) {
        DownloadTransactionListRequestObject validatorObject = new DownloadTransactionListRequestObject(aisConsent, accountId);
        return downloadTransactionsReportValidator.validate(validatorObject);
    }

    private SpiResponse<SpiTransactionReport> getSpiResponseSpiTransactionReport(Xs2aTransactionsReportByPeriodRequest request,
                                                                                 AisConsent aisConsent,
                                                                                 SpiAccountReference spiAccountReference) {
        return accountSpi.requestTransactionsForAccount(accountHelperService.getSpiContextData(),
                                                        buildSpiTransactionReportParameters(request),
                                                        spiAccountReference,
                                                        consentMapper.mapToSpiAccountConsent(aisConsent),
                                                        aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getConsentId()));
    }

    private SpiTransactionReportParameters buildSpiTransactionReportParameters(Xs2aTransactionsReportByPeriodRequest request) {
        boolean isTransactionsShouldContainBalances =
            !aspspProfileService.isTransactionsWithoutBalancesSupported() || request.isWithBalance();

        return new SpiTransactionReportParameters(request.getAcceptHeader(), isTransactionsShouldContainBalances, request.getDateFrom(), request.getDateTo(),
                                                  request.getBookingStatus(), request.getEntryReferenceFrom(), request.getDeltaList(),
                                                  request.getPageIndex(), request.getItemsPerPage());
    }

    private SpiAccountReference getRequestedAccountReference(AisConsent aisConsent, String accountId) {
        AccountAccess access = aisConsent.getAspspAccountAccesses();
        return accountHelperService.findAccountReference(access.getTransactions(), accountId);
    }

    private ResponseObject<Xs2aTransactionsReport> checkSpiResponseForTransactionsReport(Xs2aTransactionsReportByPeriodRequest request,
                                                                                         SpiResponse<SpiTransactionReport> spiResponse) {
        // in this particular call we use NOT_SUPPORTED to indicate that requested Content-type is not ok for us
        if (spiResponse.getErrors().get(0).getErrorCode() == SERVICE_NOT_SUPPORTED) {
            log.info("Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: requested content-type not json or text.",
                     request.getAccountId(), request.getConsentId());
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(ErrorType.AIS_406, TppMessageInformation.of(REQUESTED_FORMATS_INVALID))
                       .build();
        }

        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("Account-ID [{}], Consent-ID: [{}]. Get transactions report by period failed: Request transactions for account fail at SPI level: {}",
                 request.getAccountId(), request.getConsentId(), errorHolder);
        return ResponseObject.<Xs2aTransactionsReport>builder()
                   .fail(errorHolder)
                   .build();
    }

    @NotNull
    private SpiResponse<SpiTransaction> getSpiResponseSpiTransaction(AisConsent aisConsent, String consentId,
                                                                     String accountId, String transactionId) {
        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        return accountSpi.requestTransactionForAccountByTransactionId(accountHelperService.getSpiContextData(),
                                                                      transactionId,
                                                                      getRequestedAccountReference(aisConsent, accountId),
                                                                      consentMapper.mapToSpiAccountConsent(aisConsent),
                                                                      aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    private ResponseObject<Transactions> checkSpiResponseForTransactions(String consentId, String accountId,
                                                                         SpiResponse<SpiTransaction> spiResponse) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("Account-ID [{}], Consent-ID: [{}]. Get transaction details failed: Request transactions for account fail at SPI level: {}",
                 accountId, consentId, errorHolder);
        return ResponseObject.<Transactions>builder()
                   .fail(new MessageError(errorHolder))
                   .build();
    }

    @NotNull
    private SpiResponse<SpiTransactionsDownloadResponse> getSpiResponseSpiTransactionsDownloadResponse(AisConsent aisConsent,
                                                                                                       String consentId,
                                                                                                       String downloadId) {
        String decodedDownloadId = new String(Base64.getUrlDecoder().decode(downloadId));
        return accountSpi.requestTransactionsByDownloadLink(accountHelperService.getSpiContextData(),
                                                            consentMapper.mapToSpiAccountConsent(aisConsent),
                                                            decodedDownloadId,
                                                            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    private ResponseObject<Xs2aTransactionsDownloadResponse> checkSpiResponseForTransactionDownloadResponse(String consentId,
                                                                                                            String accountId,
                                                                                                            String downloadId,
                                                                                                            SpiResponse<SpiTransactionsDownloadResponse> spiResponse) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("Consent-ID [{}], Account-ID: [{}], Download-ID: [{}]. Download transactions failed: couldn't get download transactions stream by link.",
                 consentId, accountId, downloadId);
        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .fail(new MessageError(errorHolder))
                   .build();
    }

    @NotNull
    private ResponseObject<Xs2aTransactionsReport> getXs2aTransactionsReportResponseObject(Xs2aTransactionsReportByPeriodRequest request,
                                                                                           AisConsent aisConsent,
                                                                                           SpiTransactionReport spiTransactionReport) {
        Xs2aTransactionsReport transactionsReport = mapToTransactionsReport(request, aisConsent, spiTransactionReport);
        ResponseObject<Xs2aTransactionsReport> response = ResponseObject.<Xs2aTransactionsReport>builder()
                                                              .body(transactionsReport)
                                                              .build();

        aisConsentService.consentActionLog(tppService.getTppId(),
                                           request.getConsentId(),
                                           accountHelperService.createActionStatus(request.isWithBalance(), TypeAccess.TRANSACTION, response),
                                           request.getRequestUri(),
                                           accountHelperService.needsToUpdateUsage(aisConsent),
                                           transactionsReport.getAccountReference().getResourceId(), null);
        return response;
    }

    @NotNull
    private Xs2aTransactionsReport mapToTransactionsReport(Xs2aTransactionsReportByPeriodRequest request,
                                                           AisConsent aisConsent,
                                                           SpiTransactionReport spiTransactionReport) {
        Xs2aAccountReport report = transactionsToAccountReportMapper
                                       .mapToXs2aAccountReport(request.getBookingStatus(),
                                                               spiTransactionReport.getTransactions(),
                                                               spiTransactionReport.getTransactionsRaw())
                                       .orElse(null);

        Xs2aTransactionsReport transactionsReport = getXs2aTransactionsReport(report,
                                                                              getRequestedAccountReference(aisConsent, request.getAccountId()),
                                                                              spiTransactionReport);
        if (spiTransactionReport.getDownloadId() != null) {
            String encodedDownloadId = Base64.getUrlEncoder().encodeToString(spiTransactionReport.getDownloadId().getBytes());
            transactionsReport.setDownloadId(encodedDownloadId);
        }
        return transactionsReport;
    }

    private Xs2aTransactionsReport getXs2aTransactionsReport(Xs2aAccountReport report, SpiAccountReference requestedAccountReference,
                                                             SpiTransactionReport spiTransactionReport) {
        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(report);
        transactionsReport.setAccountReference(referenceMapper.mapToXs2aAccountReference(requestedAccountReference));
        transactionsReport.setBalances(balanceMapper.mapToXs2aBalanceList(spiTransactionReport.getBalances()));
        transactionsReport.setResponseContentType(spiTransactionReport.getResponseContentType());
        transactionsReport.setLinks(mapToLinks(spiTransactionReport.getSpiTransactionLinks()));
        return transactionsReport;
    }

    private Links mapToLinks(SpiTransactionLinks spiTransactionLinks) {
        if (spiTransactionLinks == null) {
            return null;
        }
        Links links = new Links();
        links.setFirst(mapToHrefType(spiTransactionLinks.getFirst()));
        links.setNext(mapToHrefType(spiTransactionLinks.getNext()));
        links.setPrevious(mapToHrefType(spiTransactionLinks.getPrevious()));
        links.setLast(mapToHrefType(spiTransactionLinks.getLast()));
        return links;
    }

    private HrefType mapToHrefType(String link) {
        return Optional.ofNullable(link)
                   .map(HrefType::new)
                   .orElse(null);
    }

    @NotNull
    private ResponseObject<Transactions> getTransactionsResponseObject(String consentId, String requestUri, AisConsent aisConsent, SpiTransaction spiTransaction, String accountId) {
        Transactions transactions = spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction);

        ResponseObject<Transactions> response = ResponseObject.<Transactions>builder()
                                                    .body(transactions)
                                                    .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, TypeAccess.TRANSACTION, response),
                                           requestUri,
                                           accountHelperService.needsToUpdateUsage(aisConsent), accountId, transactions.getTransactionId());
        return response;
    }

    private ResponseObject<Xs2aTransactionsDownloadResponse> getXs2aTransactionsDownloadResponseResponseObject(SpiTransactionsDownloadResponse spiTransactionsDownloadResponse) {
        Xs2aTransactionsDownloadResponse transactionsDownloadResponse = spiToXs2aDownloadTransactionsMapper.mapToXs2aTransactionsDownloadResponse(spiTransactionsDownloadResponse);

        return ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                   .body(transactionsDownloadResponse)
                   .build();
    }
}
