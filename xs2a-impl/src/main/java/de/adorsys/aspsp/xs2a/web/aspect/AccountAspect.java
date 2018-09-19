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

package de.adorsys.aspsp.xs2a.web.aspect;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.web12.AccountController12;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class AccountAspect extends AbstractLinkAspect<AccountController12> {
    public AccountAspect(int maxNumberOfCharInTransactionJson, AspspProfileService aspspProfileService, JsonConverter jsonConverter, MessageService messageService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountDetails(..)) && args(consentId, accountId, withBalance)", returning = "result", argNames = "result,consentId,accountId,withBalance")
    public ResponseObject<Xs2aAccountDetails> getAccountDetailsAspect(ResponseObject<Xs2aAccountDetails> result, String consentId, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            Xs2aAccountDetails accountDetails = result.getBody();
            accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getId(), withBalance));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountDetailsList(..)) && args(consentId, withBalance)", returning = "result", argNames = "result,consentId,withBalance")
    public ResponseObject<Map<String, List<Xs2aAccountDetails>>> getAccountDetailsListAspect(ResponseObject<Map<String, List<Xs2aAccountDetails>>> result, String consentId, boolean withBalance) {
        if (!result.hasError()) {
            Map<String, List<Xs2aAccountDetails>> accountDetails = result.getBody();
            setLinksToAccountsMap(accountDetails, withBalance);
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountReportByPeriod(..)) && args(accountId, withBalance, ..)", returning = "result", argNames = "result,accountId,withBalance")
    public ResponseObject<Xs2aAccountReport> getAccountReportByPeriodAspect(ResponseObject<Xs2aAccountReport> result, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            Xs2aAccountReport accountReport = result.getBody();
            accountReport.setLinks(buildLinksForAccountReport(accountReport, accountId));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getTransactionsReportByPeriod(..)) && args(accountId, withBalance, ..)", returning = "result", argNames = "result,accountId,withBalance")
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aTransactionsReport> result, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            Xs2aTransactionsReport transactionsReport = result.getBody();
            Xs2aAccountReport accountReport = transactionsReport.getAccountReport();
            accountReport.setLinks(buildLinksForAccountReport(accountReport, accountId));
            transactionsReport.setLinks(buildLinksForTransactionReport(accountId));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountReportByTransactionId(..)) && args(consentID, accountId, resourceId)", returning = "result", argNames = "result,consentID,accountId,resourceId")
    public ResponseObject<Xs2aAccountReport> getAccountReportByTransactionIdAspect(ResponseObject<Xs2aAccountReport> result, String consentID, String accountId, String resourceId) {
        if (!result.hasError()) {
            Xs2aAccountReport accountReport = result.getBody();
            Links links = new Links();
            links.setViewBalances(buildPath("/v1/accounts/{account-id}/transactions/{resourceId}", accountId, resourceId));
            accountReport.setLinks(links);
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLinksForAccountReport(Xs2aAccountReport accountReport, String accountId) {
        Links links = new Links();
        links.setViewAccount(buildPath("/v1/accounts/{accountId}", accountId));

        Optional<String> optionalAccount = jsonConverter.toJson(accountReport);
        String jsonReport = optionalAccount.orElse("");

        if (jsonReport.length() > maxNumberOfCharInTransactionJson) {
            // todo further we should implement real flow for downloading file
            links.setDownload(buildPath("/v1/accounts/{accountId}/transactions/download", accountId));
        }
        return links;
    }

    private Links buildLinksForTransactionReport(String accountId) {
        Links links = new Links();
        links.setDownload(buildPath("/v1/accounts/{accountId}/transactions/download", accountId));
        return links;
    }

    private Map<String, List<Xs2aAccountDetails>> setLinksToAccountsMap(Map<String, List<Xs2aAccountDetails>> map, boolean withBalance) {
        map.entrySet().forEach(list -> updateAccountLinks(list.getValue(), withBalance));
        return map;
    }

    private List<Xs2aAccountDetails> updateAccountLinks(List<Xs2aAccountDetails> accountDetailsList, boolean withBalance) {
        return accountDetailsList.stream()
                   .map(acc -> setLinksToAccount(acc, withBalance))
                   .collect(Collectors.toList());
    }

    private Xs2aAccountDetails setLinksToAccount(Xs2aAccountDetails accountDetails, boolean withBalance) {
        accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getId(), withBalance));
        return accountDetails;
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
