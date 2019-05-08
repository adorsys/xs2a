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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import de.adorsys.psd2.xs2a.web.link.AccountDetailsLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportByPeriodHugeLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportByPeriodLinks;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Aspect
@Component
public class AccountAspect extends AbstractLinkAspect<AccountController> {
    public AccountAspect(MessageService messageService, AspspProfileService aspspProfileService) {
        super(messageService, aspspProfileService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getAccountDetails(..)) && args( consentId, accountId, withBalance, requestUri)", returning = "result", argNames = "result,consentId,accountId,withBalance,requestUri")
    public ResponseObject<Xs2aAccountDetailsHolder> getAccountDetailsAspect(ResponseObject<Xs2aAccountDetailsHolder> result, String consentId, String accountId, boolean withBalance, String requestUri) {
        if (!result.hasError()) {
            Xs2aAccountDetailsHolder body = result.getBody();
            Xs2aAccountDetails accountDetails = body.getAccountDetails();
            accountDetails.setLinks(new AccountDetailsLinks(getHttpUrl(), accountDetails.getResourceId(),
                                                            body.getAccountConsent().getAccess()));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getAccountList(..)) && args( consentId, withBalance, requestUri)", returning = "result", argNames = "result,consentId,withBalance,requestUri")
    public ResponseObject<Xs2aAccountListHolder> getAccountDetailsListAspect(ResponseObject<Xs2aAccountListHolder> result, String consentId, boolean withBalance, String requestUri) {
        if (!result.hasError()) {
            Xs2aAccountListHolder body = result.getBody();
            List<Xs2aAccountDetails> accountDetails = body.getAccountDetails();
            Xs2aAccountAccess xs2aAccountAccess = body.getAccountConsent().getAccess();
            accountDetails.forEach(acc -> acc.setLinks(new AccountDetailsLinks(getHttpUrl(), acc.getResourceId(),
                                                                               xs2aAccountAccess)));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getTransactionsReportByPeriod(..)) && args( consentId, accountId, acceptHeader, withBalance, dateFrom, dateTo, bookingStatus, requestUri)", returning = "result", argNames = "result,consentId,accountId,acceptHeader,withBalance,dateFrom,dateTo,bookingStatus,requestUri")
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aTransactionsReport> result, String consentId, String accountId, String acceptHeader, boolean withBalance, LocalDate dateFrom, LocalDate dateTo, BookingStatus bookingStatus, String requestUri) {
        if (!result.hasError()) {
            Xs2aTransactionsReport transactionsReport = result.getBody();

            if (transactionsReport.isTransactionReportHuge()) {
                transactionsReport.setLinks(new TransactionsReportByPeriodHugeLinks(getHttpUrl(), accountId));
            } else {
                Xs2aAccountReport accountReport = transactionsReport.getAccountReport();
                accountReport.setLinks(new TransactionsReportByPeriodLinks(getHttpUrl(), accountId));
            }

            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getTransactionDetails(..)) && args( consentID, accountId, resourceId, requestUri)", returning = "result", argNames = "result,consentID,accountId,resourceId,requestUri")
    public ResponseObject<Transactions> getTransactionDetailsAspect(ResponseObject<Transactions> result, String consentID, String accountId, String resourceId, String requestUri) {
        if (!result.hasError()) {
            return result;
        }
        return enrichErrorTextMessage(result);
    }

}
