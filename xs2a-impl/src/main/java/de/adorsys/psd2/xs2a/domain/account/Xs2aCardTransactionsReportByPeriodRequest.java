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

package de.adorsys.psd2.xs2a.domain.account;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Xs2aCardTransactionsReportByPeriodRequest {
    private final String consentId;
    private final String accountId;
    private final String acceptHeader;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final BookingStatus bookingStatus;
    private final String requestUri;
    private final String entryReferenceFrom;
    private final Boolean deltaList;
    private final Integer pageIndex;
    private final Integer itemsPerPage;
}
