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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

public class TransactionsReportByPeriodHugeLinks extends AbstractLinks {

    public TransactionsReportByPeriodHugeLinks(String httpUrl, String accountId) {
        super(httpUrl);

        // TODO we need return only download link without transactions info https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/286
        // TODO further we should implement real flow for downloading file https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/286
        setDownload(buildPath(UrlHolder.ACCOUNT_TRANSACTIONS_DOWNLOAD_URL, accountId));

    }
}
