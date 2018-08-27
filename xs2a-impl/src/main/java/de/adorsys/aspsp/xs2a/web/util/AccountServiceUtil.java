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

package de.adorsys.aspsp.xs2a.web.util;

import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;

public class AccountServiceUtil {


    public static AccountDetails getAccountDetailNoBalances(AccountDetails detail) {
        return new AccountDetails(detail.getId(), detail.getIban(), detail.getBban(), detail.getPan(),
            detail.getMaskedPan(), detail.getMsisdn(), detail.getCurrency(), detail.getName(),
            detail.getAccountType(), detail.getCashAccountType(), detail.getBic(), null);
    }



    public static AccountReport filterByBookingStatus(AccountReport report, BookingStatus bookingStatus) {
        return new AccountReport(
            bookingStatus == BookingStatus.BOOKED || bookingStatus == BookingStatus.BOTH
                ? report.getBooked() : new Transactions[]{},
            bookingStatus == BookingStatus.PENDING || bookingStatus == BookingStatus.BOTH
                ? report.getPending() : new Transactions[]{});
    }


}
