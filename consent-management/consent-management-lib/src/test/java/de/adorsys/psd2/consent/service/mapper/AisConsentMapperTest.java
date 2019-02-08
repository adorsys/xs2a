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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentMapperTest {
    private static final String EXTERNAL_ID = "ed1d8022-1c38-49ae-898e-78f29234557c";
    private static final String ACCOUNT_IBAN = "DE89876442804656108109";
    private static final String RESOURCE_ID = "resource id";
    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;

    @InjectMocks
    private AisConsentMapper aisConsentMapper;

    @Test
    public void mapToAisAccountConsent_accountAccess() {
        AisConsent aisConsent = buildAisConsent();

        AisAccountAccess expectedAccess = buildAisAccountAccessAccountsWithResourceId();
        AisAccountConsent result = aisConsentMapper.mapToAisAccountConsent(aisConsent);

        assertEquals(expectedAccess, result.getAccess());
    }

    @Test
    public void mapToAisAccountConsent_accountAccess_emptyAspspAccountAccesses() {
        AisConsent aisConsent = buildAisConsentEmptyAspspAccesses();

        AisAccountAccess expectedAccess = buildAisAccountAccessAccounts();
        AisAccountConsent result = aisConsentMapper.mapToAisAccountConsent(aisConsent);

        assertEquals(expectedAccess, result.getAccess());
    }

    private AisConsent buildAisConsentEmptyAspspAccesses() {
        return buildAisConsent(Collections.emptyList());
    }

    private AisConsent buildAisConsent() {
        return buildAisConsent(Collections.singletonList(buildAspspAccountAccessAccounts()));
    }

    private AisConsent buildAisConsent(List<AspspAccountAccess> aspspAccountAccesses) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setExternalId(EXTERNAL_ID);
        aisConsent.setAspspAccountAccesses(aspspAccountAccesses);
        aisConsent.setAccesses(Collections.singletonList(buildTppAccountAccessAccounts()));
        return aisConsent;
    }

    private static TppAccountAccess buildTppAccountAccessAccounts() {
        return new TppAccountAccess(ACCOUNT_IBAN, TypeAccess.ACCOUNT, AccountReferenceType.IBAN, CURRENCY);
    }

    private static AspspAccountAccess buildAspspAccountAccessAccounts() {
        return new AspspAccountAccess(ACCOUNT_IBAN, TypeAccess.ACCOUNT, AccountReferenceType.IBAN, CURRENCY, RESOURCE_ID, ASPSP_ACCOUNT_ID);
    }

    private static List<AisConsentAuthorization> buildAuthorisations() {
        return Collections.singletonList(new AisConsentAuthorization());
    }

    private AisAccountAccess buildAisAccountAccessAccounts() {
        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY);
        List<AccountReference> accountReferences = Collections.singletonList(accountReference);
        return new AisAccountAccess(accountReferences, Collections.emptyList(), Collections.emptyList(), null, null);
    }

    private AisAccountAccess buildAisAccountAccessAccountsWithResourceId() {
        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_IBAN, CURRENCY,
                                                                 RESOURCE_ID, ASPSP_ACCOUNT_ID);
        List<AccountReference> accountReferences = Collections.singletonList(accountReference);
        return new AisAccountAccess(accountReferences, Collections.emptyList(), Collections.emptyList(), null, null);
    }
}
