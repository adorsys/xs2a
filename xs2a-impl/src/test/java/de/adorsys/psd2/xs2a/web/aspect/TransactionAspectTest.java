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
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.link.TransactionAspectService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionAspectTest {

    @InjectMocks
    private TransactionAspect aspect;

    @Mock
    private TransactionAspectService transactionAspectService;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void getTransactionsReportByPeriod() {
        Xs2aTransactionsReportByPeriodRequest request = jsonReader.getObjectFromFile("json/Xs2aTransactionsReportByPeriodRequest.json", Xs2aTransactionsReportByPeriodRequest.class);

        ResponseObject<Xs2aTransactionsReport> responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                                            .body(new Xs2aTransactionsReport())
                                            .build();
        aspect.getTransactionsReportByPeriod(responseObject, request);
        verify(transactionAspectService).getTransactionsReportByPeriod(responseObject, request);
    }
}
