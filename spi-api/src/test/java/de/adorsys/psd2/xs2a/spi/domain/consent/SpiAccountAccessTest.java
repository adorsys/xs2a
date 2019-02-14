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

package de.adorsys.psd2.xs2a.spi.domain.consent;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SpiAccountAccessTest {

    @Test
    public void isEmpty() {
        //given:
        List<SpiAccountAccess> listOfAccesses = Arrays.asList(
            new SpiAccountAccess(null, null, null, null, null),              //all fields are null
            new SpiAccountAccess(Collections.emptyList(), null, null, null, null),    //accounts are present as empty list
            new SpiAccountAccess(null, Collections.emptyList(), null, null, null),    //balances are present as empty list
            new SpiAccountAccess(null, null, Collections.emptyList(), null, null)       //transactions are present as empty list
        );
        //then:
        listOfAccesses
            .forEach(a -> assertThat(a.isEmpty()).isTrue());
    }

    @Test
    public void isEmpty_NotEmpty_Access() {
        //given:
        List<SpiAccountAccess> listOfAccesses = Arrays.asList(
            new SpiAccountAccess(Collections.singletonList(getReference()), null, null, null, null),    //accounts are present
            new SpiAccountAccess(null, Collections.singletonList(getReference()), null, null, null),    //accounts are present
            new SpiAccountAccess(null, null, Collections.singletonList(getReference()), null, null),      //balances are present
            new SpiAccountAccess(null, null, null, SpiAccountAccessType.ALL_ACCOUNTS, null),                  //availableAccount flag is present
            new SpiAccountAccess(null, null, null, null, SpiAccountAccessType.ALL_ACCOUNTS)           //allPsd2 flag is present
        );
        //then:
        listOfAccesses
            .forEach(a -> assertThat(a.isEmpty()).isFalse());
    }

    private SpiAccountReference getReference() {
        return new SpiAccountReference("rscId", "IBAN", null, null, null, null, Currency.getInstance("EUR"));
    }
}
