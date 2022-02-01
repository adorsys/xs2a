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

package de.adorsys.psd2.xs2a.domain.account;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;

import java.util.List;

@Data
public class Xs2aTransactionsReport implements CustomContentTypeProvider {
    private static final String RESPONSE_TYPE_JSON = "application/json";
    private static final String RESPONSE_TYPE_XML = "application/xml";
    private static final String RESPONSE_TYPE_TEXT = "text/plain";

    private AccountReference accountReference;

    private Xs2aAccountReport accountReport;

    private List<Xs2aBalance> balances;

    private Links links;

    private boolean transactionReportHuge;

    private String responseContentType;

    @Nullable
    private String downloadId;

    public boolean isResponseContentTypeJson() {
        return RESPONSE_TYPE_JSON.equals(responseContentType);
    }

    @Override
    public MediaType getCustomContentType() {
        if (StringUtils.isBlank(responseContentType)) {
            return MediaType.parseMediaType(RESPONSE_TYPE_JSON);
        }
        return MediaType.parseMediaType(responseContentType);
    }
}
