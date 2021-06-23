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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import de.adorsys.psd2.xs2a.web.link.Xs2aAccountReportLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportDownloadLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionAspectService extends BaseAspectService<AccountController> {

    @Autowired
    public TransactionAspectService(AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(aspspProfileServiceWrapper);
    }

    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aTransactionsReport> result,
                                                                                Xs2aTransactionsReportByPeriodRequest request) {
        if (!result.hasError()) {
            Xs2aTransactionsReport transactionsReport = result.getBody();
            if (transactionsReport != null) {
                Xs2aAccountReport accountReport = transactionsReport.getAccountReport();
                if (accountReport != null) {
                    accountReport.setLinks(new Xs2aAccountReportLinks(getHttpUrl(), request.getAccountId(), transactionsReport.getLinks()));
                }
                transactionsReport.setLinks(new TransactionsReportDownloadLinks(getHttpUrl(), request.getAccountId(), request.isWithBalance(), transactionsReport.getDownloadId()));
            }
        }
        return result;
    }
}
