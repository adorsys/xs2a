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
