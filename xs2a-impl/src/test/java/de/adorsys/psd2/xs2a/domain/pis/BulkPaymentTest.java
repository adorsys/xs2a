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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class BulkPaymentTest {
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
    public void getAccountReferences_shouldReturnAllReferences() {
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
