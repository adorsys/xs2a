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
