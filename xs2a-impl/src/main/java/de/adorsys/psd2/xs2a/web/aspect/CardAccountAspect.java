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

