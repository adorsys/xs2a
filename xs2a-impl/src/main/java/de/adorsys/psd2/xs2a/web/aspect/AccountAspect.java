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

import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class AccountAspect extends AbstractLinkAspect<AccountController> {
    public AccountAspect(ScaApproachResolver scaApproachResolver, MessageService messageService) {
        super(scaApproachResolver, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getAccountDetails(..)) && args( consentId, accountId, withBalance)", returning = "result", argNames = "result,consentId,accountId,withBalance")
    public ResponseObject<Xs2aAccountDetails> getAccountDetailsAspect(ResponseObject<Xs2aAccountDetails> result, String consentId, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            Xs2aAccountDetails accountDetails = result.getBody();
            accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getResourceId(), withBalance));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getAccountList(..)) && args( consentId, withBalance)", returning = "result", argNames = "result,consentId,withBalance")
    public ResponseObject<Map<String, List<Xs2aAccountDetails>>> getAccountDetailsListAspect(ResponseObject<Map<String, List<Xs2aAccountDetails>>> result, String consentId, boolean withBalance) {
        if (!result.hasError()) {
            Map<String, List<Xs2aAccountDetails>> accountDetails = result.getBody();
            setLinksToAccounts(accountDetails, withBalance);
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

    private void setLinksToAccounts(Map<String, List<Xs2aAccountDetails>> accountDetailsMap, boolean withBalance) {
        for (Map.Entry<String, List<Xs2aAccountDetails>> entry : accountDetailsMap.entrySet()) {
            updateAccountLinks(entry.getValue(), withBalance);
        }
    }

    private void updateAccountLinks(List<Xs2aAccountDetails> accountDetailsList, boolean withBalance) {
        for (Xs2aAccountDetails acc : accountDetailsList) {
            setLinksToAccount(acc, withBalance);
        }
    }

    private void setLinksToAccount(Xs2aAccountDetails accountDetails, boolean withBalance) {
        accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getResourceId(), withBalance));
    }

    private Links buildLinksForAccountDetails(String accountId, boolean withBalance) {
        Links links = new Links();
        if (withBalance) {
            links.setViewBalances(buildPath("/v1/accounts/{accountId}/balances", accountId));
        }
        links.setViewTransactions(buildPath("/v1/accounts/{accountId}/transactions", accountId));
        return links;
    }
}
