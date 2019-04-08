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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Aspect
@Component
public class AccountAspect extends AbstractLinkAspect<AccountController> {
    public AccountAspect(ScaApproachResolver scaApproachResolver, MessageService messageService, AspspProfileService aspspProfileService) {
        super(scaApproachResolver, messageService, aspspProfileService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getAccountDetails(..)) && args( consentId, accountId, withBalance)", returning = "result", argNames = "result,consentId,accountId,withBalance")
    public ResponseObject<Xs2aAccountDetailsHolder> getAccountDetailsAspect(ResponseObject<Xs2aAccountDetailsHolder> result, String consentId, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            Xs2aAccountDetails accountDetails = result.getBody().getAccountDetails();
            accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getResourceId(), result.getBody().getAccountConsent().getAccess()));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getAccountList(..)) && args( consentId, withBalance)", returning = "result", argNames = "result,consentId,withBalance")
    public ResponseObject<Xs2aAccountListHolder> getAccountDetailsListAspect(ResponseObject<Xs2aAccountListHolder> result, String consentId, boolean withBalance) {
        if (!result.hasError()) {
            List<Xs2aAccountDetails> accountDetails = result.getBody().getAccountDetails();
            setLinksToAccounts(accountDetails, result.getBody().getAccountConsent().getAccess());
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getTransactionsReportByPeriod(..)) && args( consentId, accountId, withBalance, ..)", returning = "result", argNames = "result,consentId,accountId,withBalance")
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aTransactionsReport> result, String consentId, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            Xs2aTransactionsReport transactionsReport = result.getBody();

            if (transactionsReport.isTransactionReportHuge()) {
                // TODO we need return only download link without transactions info https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/286
                // TODO further we should implement real flow for downloading file https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/286
                Links links = new Links();
                links.setDownload(buildPath("/v1/accounts/{accountId}/transactions/download", accountId));
                transactionsReport.setLinks(links);
            } else {
                Xs2aAccountReport accountReport = transactionsReport.getAccountReport();
                accountReport.setLinks(buildLinksForAccountReport(accountId));
            }

            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getTransactionDetails(..)) && args( consentID, accountId, resourceId)", returning = "result", argNames = "result,consentID,accountId,resourceId")
    public ResponseObject<Transactions> getTransactionDetailsAspect(ResponseObject<Transactions> result, String consentID, String accountId, String resourceId) {
        if (!result.hasError()) {
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLinksForAccountReport(String accountId) {
        Links links = new Links();
        links.setAccount(buildPath("/v1/accounts/{accountId}", accountId));

        return links;
    }

    private void setLinksToAccounts(List<Xs2aAccountDetails> accountDetailsList, Xs2aAccountAccess xs2aAccountAccess) {
        accountDetailsList.forEach(acc -> acc.setLinks(buildLinksForAccountDetails(acc.getResourceId(), xs2aAccountAccess)));
    }

    private Links buildLinksForAccountDetails(String accountId, Xs2aAccountAccess xs2aAccountAccess) {
        Links links = new Links();

        if (isValidAccountByAccess(accountId, xs2aAccountAccess.getBalances())) {
            links.setBalances(buildPath("/v1/accounts/{accountId}/balances", accountId));
        }

        if (isValidAccountByAccess(accountId, xs2aAccountAccess.getTransactions())) {
            links.setTransactions(buildPath("/v1/accounts/{accountId}/transactions", accountId));
        }

        return links;
    }

    private boolean isValidAccountByAccess(String accountId, List<AccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> accountId.equals(a.getResourceId()));
    }
}
