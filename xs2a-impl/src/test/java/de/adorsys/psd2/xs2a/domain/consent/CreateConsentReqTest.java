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

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class CreateConsentReqTest {

    private static final String IBAN = "IBAN ";

    @Test
    public void getAccountReferences_all() {
        //Given:
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(getAccess(getRefs(1), getRefs(2), getRefs(3)));
        //When:
        Set<AccountReference> result = req.getAccountReferences();
        //Then:
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void getAccountReferences_1_null() {
        //Given:
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(getAccess(null, getRefs(2), getRefs(3)));
        //When:
        Set<AccountReference> result = req.getAccountReferences();
        //Then:
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void getAccountReferences_all_null() {
        //Given:
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(getAccess(null, null, null));
        //When:
        Set<AccountReference> result = req.getAccountReferences();
        //Then:
        assertThat(result.size()).isEqualTo(0);
    }

    private Xs2aAccountAccess getAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions) {
        return new Xs2aAccountAccess(accounts, balances, transactions, null, null);
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
