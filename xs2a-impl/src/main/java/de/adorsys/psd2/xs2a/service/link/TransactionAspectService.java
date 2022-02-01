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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportDownloadLinks;
import de.adorsys.psd2.xs2a.web.link.Xs2aAccountReportLinks;
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
