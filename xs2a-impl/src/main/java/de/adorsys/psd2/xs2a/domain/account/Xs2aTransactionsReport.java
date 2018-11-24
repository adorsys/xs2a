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

package de.adorsys.psd2.xs2a.domain.account;

import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import lombok.Data;
import org.springframework.http.MediaType;

import java.util.List;

@Data
public class Xs2aTransactionsReport implements CustomContentTypeProvider {
    private static final String RESPONSE_TYPE_JSON = "application/json";

    private Xs2aAccountReference xs2aAccountReference;

    private Xs2aAccountReport accountReport;

    private List<Xs2aBalance> balances;

    private Links links;

    private boolean transactionReportHuge;

    private String responseContentType;

    public boolean isResponseContentTypeJson() {
        return RESPONSE_TYPE_JSON.equals(responseContentType);
    }

    @Override
    public MediaType getCustomContentType() {
        return new MediaType(responseContentType);
    }
}
