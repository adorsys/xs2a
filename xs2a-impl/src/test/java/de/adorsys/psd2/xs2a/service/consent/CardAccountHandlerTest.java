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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aUsageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CardAccountHandlerTest {

    private static final String MASKED_PAN = "525412******3241";
    private static final String WRONG_MASKED_PAN = "525412******32410";
    private static final String PAN = "5254120000003241";
    private static final String WRONG_PAN = "52541200000032410";

    @InjectMocks
    private CardAccountHandler cardAccountHandler;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(cardAccountHandler, "maskedPanBeginChars", 6);
        ReflectionTestUtils.setField(cardAccountHandler, "maskedPanEndChars", 4);
    }

    @Test
    void areAccountsEqual_2_maskedPans() {
        // When
        boolean actual =
            cardAccountHandler.areAccountsEqual(getXs2aCardAccountDetails(), getAccountReference(AccountReferenceType.MASKED_PAN, MASKED_PAN));

        // Then
        assertTrue(actual);
    }

    @Test
    void areAccountsEqual_maskedPan_and_pan() {
        // When
        boolean actual = cardAccountHandler.areAccountsEqual(getXs2aCardAccountDetails(), getAccountReference(AccountReferenceType.PAN, PAN));

        // Then
        assertTrue(actual);
    }

    @Test
    void areAccountsEqual_2_maskedPans_wrong() {
        // When
        boolean actual =
            cardAccountHandler.areAccountsEqual(getXs2aCardAccountDetails(), getAccountReference(AccountReferenceType.MASKED_PAN, WRONG_MASKED_PAN));

        // Then
        assertFalse(actual);
    }

    @Test
    void areAccountsEqual_maskedPan_and_pan_wrong() {
        // When
        boolean actual = cardAccountHandler.areAccountsEqual(getXs2aCardAccountDetails(), getAccountReference(AccountReferenceType.PAN, WRONG_PAN));

        // Then
        assertFalse(actual);
    }

    private Xs2aCardAccountDetails getXs2aCardAccountDetails() {
        Xs2aAmount creditLimit = new Xs2aAmount();
        creditLimit.setCurrency(Currency.getInstance("EUR"));
        creditLimit.setAmount("10000");

        return new Xs2aCardAccountDetails(null, null, CardAccountHandlerTest.MASKED_PAN, Currency.getInstance("EUR"), null, null, null,
                                          null, AccountStatus.ENABLED, Xs2aUsageType.PRIV, "details",
                                          null, creditLimit, null, null);
    }

    private AccountReference getAccountReference(AccountReferenceType accountReferenceType, String accountReferenceValue) {
        return new AccountReference(accountReferenceType, accountReferenceValue, Currency.getInstance("EUR"));

    }
}
