/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.link.CardAccountAspectService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CardAccountAspect {

    private CardAccountAspectService cardAccountAspectService;

    public CardAccountAspect(CardAccountAspectService cardAccountAspectService) {
        this.cardAccountAspectService = cardAccountAspectService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ais.CardAccountService.getCardAccountList(..))", returning = "result")
    public ResponseObject<Xs2aCardAccountListHolder> getCardAccountList(ResponseObject<Xs2aCardAccountListHolder> result) {
        return cardAccountAspectService.getCardAccountList(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ais.CardAccountService.getCardAccountDetails(..))", returning = "result")
    public ResponseObject<Xs2aCardAccountDetailsHolder> getCardAccountDetails(ResponseObject<Xs2aCardAccountDetailsHolder> result) {
        return cardAccountAspectService.getCardAccountDetails(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ais.CardTransactionService.getCardTransactionsReportByPeriod(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aCardTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aCardTransactionsReport> result, Xs2aCardTransactionsReportByPeriodRequest request) {
        return cardAccountAspectService.getTransactionsReportByPeriod(result, request);
    }
}
