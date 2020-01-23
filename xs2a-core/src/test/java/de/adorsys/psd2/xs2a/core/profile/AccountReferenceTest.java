/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class AccountReferenceTest {

    private static final String ACCOUNT_REFERENCE_VALUE = "111";
    private static final String RESOURCE_ID = "resource_id";
    private static final String ASPSP_ACCOUNT_ID = "aspspAccountId";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    private AccountReference accountReference;

    @BeforeEach
    void setUp() {
        accountReference = new AccountReference(null, null, CURRENCY, RESOURCE_ID, ASPSP_ACCOUNT_ID);
        assertEquals(RESOURCE_ID, accountReference.getResourceId());
        assertEquals(ASPSP_ACCOUNT_ID, accountReference.getAspspAccountId());
    }

    @Test
    void createWithSpecificAccountReferenceType_iban() {
        accountReference = new AccountReference(AccountReferenceType.IBAN, ACCOUNT_REFERENCE_VALUE, CURRENCY);
        assertEquals(ACCOUNT_REFERENCE_VALUE, accountReference.getIban());
        assertNull(accountReference.getBban());
        assertNull(accountReference.getPan());
        assertNull(accountReference.getMsisdn());
        assertNull(accountReference.getMaskedPan());
    }

    @Test
    void createWithSpecificAccountReferenceType_bban() {
        accountReference = new AccountReference(AccountReferenceType.BBAN, ACCOUNT_REFERENCE_VALUE, CURRENCY);
        assertEquals(ACCOUNT_REFERENCE_VALUE, accountReference.getBban());
        assertNull(accountReference.getIban());
        assertNull(accountReference.getPan());
        assertNull(accountReference.getMsisdn());
        assertNull(accountReference.getMaskedPan());
    }

    @Test
    void createWithSpecificAccountReferenceType_pan() {
        accountReference = new AccountReference(AccountReferenceType.PAN, ACCOUNT_REFERENCE_VALUE, CURRENCY);
        assertEquals(ACCOUNT_REFERENCE_VALUE, accountReference.getPan());
        assertNull(accountReference.getIban());
        assertNull(accountReference.getBban());
        assertNull(accountReference.getMsisdn());
        assertNull(accountReference.getMaskedPan());
    }

    @Test
    void createWithSpecificAccountReferenceType_maskedPan() {
        accountReference = new AccountReference(AccountReferenceType.MASKED_PAN, ACCOUNT_REFERENCE_VALUE, CURRENCY);
        assertEquals(ACCOUNT_REFERENCE_VALUE, accountReference.getMaskedPan());
        assertNull(accountReference.getIban());
        assertNull(accountReference.getBban());
        assertNull(accountReference.getPan());
        assertNull(accountReference.getMsisdn());
    }

    @Test
    void createWithSpecificAccountReferenceType_msisdn() {
        accountReference = new AccountReference(AccountReferenceType.MSISDN, ACCOUNT_REFERENCE_VALUE, CURRENCY);
        assertEquals(ACCOUNT_REFERENCE_VALUE, accountReference.getMsisdn());
        assertNull(accountReference.getIban());
        assertNull(accountReference.getBban());
        assertNull(accountReference.getPan());
        assertNull(accountReference.getMaskedPan());
    }

    @Test
    void getUsedAccountReferenceSelector_exception() {
        assertThrows(IllegalArgumentException.class, () -> accountReference.getUsedAccountReferenceSelector());
    }

    @Test
    void getUsedAccountReferenceSelector() {
        accountReference.setMaskedPan("masked pan");
        AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();
        assertEquals(AccountReferenceType.MASKED_PAN, selector.getAccountReferenceType());
        assertEquals("masked pan", selector.getAccountValue());

        accountReference.setMsisdn("msisdn");
        selector = accountReference.getUsedAccountReferenceSelector();
        assertEquals(AccountReferenceType.MSISDN, selector.getAccountReferenceType());
        assertEquals("msisdn", selector.getAccountValue());

        accountReference.setPan("pan");
        selector = accountReference.getUsedAccountReferenceSelector();
        assertEquals(AccountReferenceType.PAN, selector.getAccountReferenceType());
        assertEquals("pan", selector.getAccountValue());

        accountReference.setBban("bban");
        selector = accountReference.getUsedAccountReferenceSelector();
        assertEquals(AccountReferenceType.BBAN, selector.getAccountReferenceType());
        assertEquals("bban", selector.getAccountValue());

        accountReference.setIban("iban");
        selector = accountReference.getUsedAccountReferenceSelector();
        assertEquals(AccountReferenceType.IBAN, selector.getAccountReferenceType());
        assertEquals("iban", selector.getAccountValue());
    }

    @Test
    void getUsedAccountReferenceFields() {
        assertTrue(accountReference.getUsedAccountReferenceFields().isEmpty());

        accountReference.setMaskedPan("masked pan");
        assertTrue(accountReference.getUsedAccountReferenceFields().contains(AccountReferenceType.MASKED_PAN));

        accountReference.setMsisdn("msisdn");
        assertTrue(accountReference.getUsedAccountReferenceFields().contains(AccountReferenceType.MSISDN));

        accountReference.setPan("pan");
        assertTrue(accountReference.getUsedAccountReferenceFields().contains(AccountReferenceType.PAN));

        accountReference.setBban("bban");
        assertTrue(accountReference.getUsedAccountReferenceFields().contains(AccountReferenceType.BBAN));

        accountReference.setIban("iban");
        assertTrue(accountReference.getUsedAccountReferenceFields().contains(AccountReferenceType.IBAN));
        assertEquals(5, accountReference.getUsedAccountReferenceFields().size());
    }
}
