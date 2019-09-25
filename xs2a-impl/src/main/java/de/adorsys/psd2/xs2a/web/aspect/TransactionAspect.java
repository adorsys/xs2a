/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportAccountLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportDownloadLinks;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TransactionAspect extends AbstractLinkAspect<AccountController> {

    public TransactionAspect(AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(aspspProfileServiceWrapper);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ais.TransactionService.getTransactionsReportByPeriod(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aTransactionsReport> result, Xs2aTransactionsReportByPeriodRequest request) {
        if (!result.hasError()) {
            Xs2aTransactionsReport transactionsReport = result.getBody();
            transactionsReport.setLinks(new TransactionsReportDownloadLinks(getHttpUrl(), request.getAccountId(), request.isWithBalance(), transactionsReport.getDownloadId()));
            Xs2aAccountReport accountReport = transactionsReport.getAccountReport();

            if (transactionsReport.getAccountReport() != null) {
                accountReport.setLinks(new TransactionsReportAccountLinks(getHttpUrl(), request.getAccountId(), request.isWithBalance()));
            }

        }
        return result;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ais.TransactionService.getTransactionDetails(..)) && args( consentID, accountId, resourceId, requestUri)", returning = "result", argNames = "result,consentID,accountId,resourceId,requestUri")
    public ResponseObject<Transactions> getTransactionDetailsAspect(ResponseObject<Transactions> result, String consentID, String accountId, String resourceId, String requestUri) {
        return result;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ais.TransactionService.downloadTransactions(..)) && args( consentId, accountId, downloadId)", returning = "result", argNames = "result,consentId,accountId,downloadId")
    public ResponseObject<Xs2aTransactionsDownloadResponse> downloadTransactions(ResponseObject<Xs2aTransactionsDownloadResponse> result, String consentId, String accountId, String downloadId) {
        return result;
    }
}
