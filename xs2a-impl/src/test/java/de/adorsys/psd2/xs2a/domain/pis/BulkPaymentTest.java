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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkPaymentTest {
    private static final AccountReference DEBTOR_ACCOUNT_1 = new AccountReference(AccountReferenceType.IBAN,
                                                                                  "debtor iban 1",
                                                                                  Currency.getInstance("EUR"));
    private static final AccountReference DEBTOR_ACCOUNT_2 = new AccountReference(AccountReferenceType.IBAN,
                                                                                  "debtor iban 2",
                                                                                  Currency.getInstance("EUR"));
    private static final AccountReference CREDITOR_ACCOUNT = new AccountReference(AccountReferenceType.IBAN,
                                                                                  "creditor iban",
                                                                                  Currency.getInstance("EUR"));

    @Test
    void getAccountReferences_shouldReturnAllReferences() {
        // Given
        BulkPayment bulkPayment = buildBulkPayment(Collections.singletonList(buildSinglePayment()));

        // When
        Set<AccountReference> actualAccountReferences = bulkPayment.getAccountReferences();

        // Then
        Set<AccountReference> expectedAccountReferences = new HashSet<>(Arrays.asList(DEBTOR_ACCOUNT_1, DEBTOR_ACCOUNT_2, CREDITOR_ACCOUNT));
        assertEquals(expectedAccountReferences, actualAccountReferences);
    }

    private BulkPayment buildBulkPayment(List<SinglePayment> payments) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setDebtorAccount(DEBTOR_ACCOUNT_1);
        bulkPayment.setPayments(payments);
        return bulkPayment;
    }

    private SinglePayment buildSinglePayment() {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setDebtorAccount(DEBTOR_ACCOUNT_2);
        singlePayment.setCreditorAccount(CREDITOR_ACCOUNT);
        return singlePayment;
    }
}
