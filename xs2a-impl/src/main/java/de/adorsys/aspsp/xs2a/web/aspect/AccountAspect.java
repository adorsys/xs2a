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

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
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

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountDetails(..)) && args(consentId, accountId, withBalance)", returning = "result", argNames = "result,consentId,accountId,withBalance")
    public ResponseObject<AccountDetails> getAccountDetailsAspect(ResponseObject<AccountDetails> result, String consentId, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            AccountDetails accountDetails = result.getBody();
            accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getId(), withBalance));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountDetailsList(..)) && args(consentId, withBalance)", returning = "result", argNames = "result,consentId,withBalance")
    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsListAspect(ResponseObject<Map<String, List<AccountDetails>>> result, String consentId, boolean withBalance) {
        if (!result.hasError()) {
            Map<String, List<AccountDetails>> accountDetails = result.getBody();
            setLinksToAccountsMap(accountDetails, withBalance);
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountReportByPeriod(..)) && args(accountId, withBalance, ..)", returning = "result", argNames = "result,accountId,withBalance")
    public ResponseObject<AccountReport> getAccountReportByPeriodAspect(ResponseObject<AccountReport> result, String accountId, boolean withBalance) {
        if (!result.hasError()) {
            AccountReport accountReport = result.getBody();
            accountReport.setLinks(buildLinksForAccountReport(accountReport, accountId));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.AccountService.getAccountReportByTransactionId(..)) && args(consentID, accountId, resourceId)", returning = "result", argNames = "result,consentID,accountId,resourceId")
    public ResponseObject<AccountReport> getAccountReportByTransactionIdAspect(ResponseObject<AccountReport> result, String consentID, String accountId, String resourceId) {
        if (!result.hasError()) {
            AccountReport accountReport = result.getBody();
            Links links = new Links();
            links.setViewBalances(buildLink("/v1/accounts/{account-id}/transactions/{resourceId}", accountId, resourceId));
            accountReport.setLinks(links);
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLinksForAccountReport(AccountReport accountReport, String accountId) {
        Links links = new Links();
        links.setViewAccount(buildLink("/v1/accounts/{accountId}", accountId));

        Optional<String> optionalAccount = jsonConverter.toJson(accountReport);
        String jsonReport = optionalAccount.orElse("");

        if (jsonReport.length() > maxNumberOfCharInTransactionJson) {
            // todo further we should implement real flow for downloading file
            links.setDownload(buildLink("/v1/accounts/{accountId}/transactions/download", accountId));
        }
        return links;
    }

    private Map<String, List<AccountDetails>> setLinksToAccountsMap(Map<String, List<AccountDetails>> map, boolean withBalance) {
        map.entrySet().forEach(list -> updateAccountLinks(list.getValue(), withBalance));
        return map;
    }

    private List<AccountDetails> updateAccountLinks(List<AccountDetails> accountDetailsList, boolean withBalance) {
        return accountDetailsList.stream()
                   .map(acc -> setLinksToAccount(acc, withBalance))
                   .collect(Collectors.toList());
    }

    private AccountDetails setLinksToAccount(AccountDetails accountDetails, boolean withBalance) {
        accountDetails.setLinks(buildLinksForAccountDetails(accountDetails.getId(), withBalance));
        return accountDetails;
    }

    private Links buildLinksForAccountDetails(String accountId, boolean withBalance) {
        Links links = new Links();
        if (withBalance) {
            links.setViewBalances(buildLink("/v1/accounts/{accountId}/balances", accountId, accountId));
        }
        links.setViewTransactions(buildLink("/v1/accounts/{accountId}/transactions", accountId));
        return links;
    }
}
