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

import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TransactionReportAspect {
    private final int maxNumberOfCharInTransactionJson;
    private final JsonConverter jsonConverter;

    public TransactionReportAspect(int maxNumberOfCharInTransactionJson, JsonConverter jsonConverter) {
        this.maxNumberOfCharInTransactionJson = maxNumberOfCharInTransactionJson;
        this.jsonConverter = jsonConverter;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.AccountService.getTransactionsReportByPeriod(..))", returning = "result", argNames = "result")
    public ResponseObject<Xs2aTransactionsReport> invokeGetTransactionsReportByPeriodAspect(ResponseObject<Xs2aTransactionsReport> result) {
        if (!result.hasError()) {
            Xs2aTransactionsReport report = result.getBody();

            report.setTransactionReportHuge(isTransactionReportHuge(report));
        }

        return result;
    }

    private boolean isTransactionReportHuge(Xs2aTransactionsReport transactionsReport) {
        String jsonReport = jsonConverter.toJson(transactionsReport)
                                .orElse("");

        return jsonReport.length() > maxNumberOfCharInTransactionJson;
    }
}
