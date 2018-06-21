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

import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.web.AccountController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class AccountAspect extends AbstractLinkAspect<AccountController> {

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.AccountController.readAccountDetails(..)) && args(consentId, accountId, withBalance, ..)", returning = "result")
    public ResponseEntity<AccountDetails> invokeReadAccountDetailsAspect(ResponseEntity<AccountDetails> result, String consentId, String accountId, boolean withBalance) {
        if (!hasError(result)) {
            AccountDetails body = result.getBody();
            body.setLinks(buildLinksForAccountDetails(body, withBalance));
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.AccountController.getAccounts(..)) && args(consentId, withBalance, ..)", returning = "result")
    public ResponseEntity<Map<String, List<AccountDetails>>> invokeGetAccountsAspect(ResponseEntity<Map<String, List<AccountDetails>>> result, String consentId, boolean withBalance) {
        if (!hasError(result)) {
            Map<String, List<AccountDetails>> body = result.getBody();
            setLinksToAccountsMap(body, withBalance);
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.AccountController.getTransactions(..)) && args(accountId,..)", returning = "result")
    public ResponseEntity<AccountReport> invokeGetTransactionsAspect(ResponseEntity<AccountReport> result, String accountId) {
        if (!hasError(result)) {
            AccountReport body = result.getBody();
            body.setLinks(buildLinksForAccountReport(body, accountId));
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    private Links buildLinksForAccountDetails(AccountDetails accountDetails, boolean withBalance) {
        Class controller = getController();

        Links links = new Links();
        if (withBalance) {
            links.setViewBalances(linkTo(controller).slash(accountDetails.getId()).slash("balances").toString());
        }
        links.setViewTransactions(linkTo(controller).slash(accountDetails.getId()).slash("transactions").toString());

        return links;
    }

    private Links buildLinksForAccountReport(AccountReport accountReport, String accountId) {
        Class controller = getController();

        Links links = new Links();
        links.setViewAccount(linkTo(controller).slash(accountId).toString());

        Optional<String> optionalAccount = jsonConverter.toJson(accountReport);
        String jsonReport = optionalAccount.orElse("");

        if (jsonReport.length() > maxNumberOfCharInTransactionJson) {
            // todo further we should implement real flow for downloading file
            links.setDownload(linkTo(controller).slash(accountId).slash("transactions/download").toString());
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
        accountDetails.setLinks(buildLinksForAccountDetails(accountDetails, withBalance));
        return accountDetails;
    }
}
