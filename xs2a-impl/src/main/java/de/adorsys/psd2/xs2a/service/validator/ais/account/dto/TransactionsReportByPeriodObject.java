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

package de.adorsys.psd2.xs2a.service.validator.ais.account.dto;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class TransactionsReportByPeriodObject implements TppInfoProvider {
    private AisConsent aisConsent;
    private String accountId;
    private boolean withBalance;
    private String requestUri;
    private String entryReferenceFrom;
    private Boolean deltaList;
    private String acceptHeader;
    private BookingStatus bookingStatus;
    private LocalDate dateFrom;

    @Override
    public TppInfo getTppInfo() {
        return aisConsent.getTppInfo();
    }

    public List<AccountReference> getTransactions() {
        return aisConsent.getAspspAccountAccesses().getTransactions();
    }
}
