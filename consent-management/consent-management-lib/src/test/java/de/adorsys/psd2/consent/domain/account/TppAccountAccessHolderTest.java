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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ais.AccountAdditionalInformationAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TppAccountAccessHolderTest {

    @Test
    public void tppAccountAccessHolder_noAdditionalInformation() {
        //Given
        AisAccountAccessInfo aisAccountAccessInfo = new AisAccountAccessInfo();
        //When
        TppAccountAccessHolder tppAccountAccessHolder = new TppAccountAccessHolder(aisAccountAccessInfo);
        //Then
        Set<TppAccountAccess> accountAccesses = tppAccountAccessHolder.getAccountAccesses();
        assertEquals( 0, accountAccesses.size());
    }

    @Test
    public void tppAccountAccessHolder_ownerName() {
        //Given
        AccountReferenceType accountReferenceType = AccountReferenceType.IBAN;
        String accountIdentifier = "IBAN";
        AccountInfo accountInfo = AccountInfo.builder().accountReferenceType(accountReferenceType).accountIdentifier(accountIdentifier).build();
        List<AccountInfo> ownerName = Collections.singletonList(accountInfo);
        AisAccountAccessInfo aisAccountAccessInfo = new AisAccountAccessInfo();
        aisAccountAccessInfo.setAccountAdditionalInformationAccess(new AccountAdditionalInformationAccess(ownerName));
        //When
        TppAccountAccessHolder tppAccountAccessHolder = new TppAccountAccessHolder(aisAccountAccessInfo);
        //Then
        Set<TppAccountAccess> accountAccesses = tppAccountAccessHolder.getAccountAccesses();
        assertEquals( 1, accountAccesses.size());
        TppAccountAccess tppAccountAccess = accountAccesses.iterator().next();
        assertEquals( accountIdentifier, tppAccountAccess.getAccountIdentifier());
        assertEquals( accountReferenceType, tppAccountAccess.getAccountReferenceType());
    }
}
