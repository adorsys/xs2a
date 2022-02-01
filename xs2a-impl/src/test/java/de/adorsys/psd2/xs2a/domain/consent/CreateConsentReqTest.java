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

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class CreateConsentReqTest {
    private static final String IBAN = "IBAN ";

    @Test
    void getAccountReferences_all() {
        //Given:
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(getAccess(getRefs(1), getRefs(2), getRefs(3)));
        //When:
        Set<AccountReference> result = req.getAccountReferences();
        //Then:
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void getAccountReferences_1_null() {
        //Given:
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(getAccess(null, getRefs(2), getRefs(3)));
        //When:
        Set<AccountReference> result = req.getAccountReferences();
        //Then:
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void getAccountReferences_all_null() {
        //Given:
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(getAccess(null, null, null));
        //When:
        Set<AccountReference> result = req.getAccountReferences();
        //Then:
        assertThat(result).isEmpty();
    }

    private AccountAccess getAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions) {
        return new AccountAccess(accounts, balances, transactions, null);
    }

    private List<AccountReference> getRefs(int qty) {
        List<AccountReference> list = new ArrayList<>();
        for (int i = 0; i < qty; i++) {
            AccountReference reference = new AccountReference();
            reference.setIban(IBAN + i);
            list.add(reference);
        }
        return list;
    }
}
