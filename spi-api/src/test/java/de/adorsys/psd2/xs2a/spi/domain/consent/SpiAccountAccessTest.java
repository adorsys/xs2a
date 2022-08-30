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

package de.adorsys.psd2.xs2a.spi.domain.consent;

import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpiAccountAccessTest {

    @Test
    void isEmpty() {
        //given:
        List<SpiAccountAccess> listOfAccesses = Arrays.asList(
            new SpiAccountAccess(null, null, null, null, null, null, null),              //all fields are null
            new SpiAccountAccess(Collections.emptyList(), null, null, null, null, null, null),    //accounts are present as empty list
            new SpiAccountAccess(null, Collections.emptyList(), null, null, null, null, null),    //balances are present as empty list
            new SpiAccountAccess(null, null, Collections.emptyList(), null, null, null, null)       //transactions are present as empty list
        );
        //then:
        listOfAccesses
            .forEach(a -> assertTrue(a.isEmpty()));
    }

    @Test
    void isEmpty_NotEmpty_Access() {
        //given:
        List<SpiAccountAccess> listOfAccesses = Arrays.asList(
            new SpiAccountAccess(Collections.singletonList(getReference()), null, null, null, null, null, null),    //accounts are present
            new SpiAccountAccess(null, Collections.singletonList(getReference()), null, null, null, null, null),    //accounts are present
            new SpiAccountAccess(null, null, Collections.singletonList(getReference()), null, null, null, null),      //balances are present
            new SpiAccountAccess(null, null, null, AccountAccessType.ALL_ACCOUNTS, null, null, null),                  //availableAccount flag is present
            new SpiAccountAccess(null, null, null, null, AccountAccessType.ALL_ACCOUNTS, null, null),       //allPsd2 flag is present
            new SpiAccountAccess(null, null, null, null, null, AccountAccessType.ALL_ACCOUNTS, null)           //availableAccountWithBalances flag is present
        );
        //then:
        listOfAccesses
            .forEach(a -> assertFalse(a.isEmpty()));
    }

    private SpiAccountReference getReference() {
        return SpiAccountReference.builder()
                   .resourceId("rscId")
                   .iban("IBAN")
                   .currency(Currency.getInstance("EUR"))
                   .build();
    }
}
