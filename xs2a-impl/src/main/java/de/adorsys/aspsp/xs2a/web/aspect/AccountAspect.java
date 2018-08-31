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

import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
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
    public ResponseEntity<Xs2aAccountDetails> invokeReadAccountDetailsAspect(ResponseEntity<Xs2aAccountDetails> result, String consentId, String accountId, boolean withBalance) {
        if (!hasError(result)) {
            Xs2aAccountDetails body = result.getBody();
            body.setLinks(buildLinksForAccountDetails(body, withBalance));
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.AccountController.getAccounts(..)) && args(consentId, withBalance, ..)", returning = "result")
    public ResponseEntity<Map<String, List<Xs2aAccountDetails>>> invokeGetAccountsAspect(ResponseEntity<Map<String, List<Xs2aAccountDetails>>> result, String consentId, boolean withBalance) {
        if (!hasError(result)) {
            Map<String, List<Xs2aAccountDetails>> body = result.getBody();
            setLinksToAccountsMap(body, withBalance);
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.AccountController.getTransactions(..)) && args(accountId,..)", returning = "result")
    public ResponseEntity<Xs2aAccountReport> invokeGetTransactionsAspect(ResponseEntity<Xs2aAccountReport> result, String accountId) {
        if (!hasError(result)) {
            Xs2aAccountReport body = result.getBody();
            body.setLinks(buildLinksForAccountReport(body, accountId));
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    private Links buildLinksForAccountDetails(Xs2aAccountDetails accountDetails, boolean withBalance) {
        Class controller = getController();

        Links links = new Links();
        if (withBalance) {
            links.setViewBalances(linkTo(controller).slash(accountDetails.getId()).slash("balances").toString());
        }
        links.setViewTransactions(linkTo(controller).slash(accountDetails.getId()).slash("transactions").toString());

        return links;
    }

    private Links buildLinksForAccountReport(Xs2aAccountReport accountReport, String accountId) {
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
        accountDetails.setLinks(buildLinksForAccountDetails(accountDetails, withBalance));
        return accountDetails;
    }
}
