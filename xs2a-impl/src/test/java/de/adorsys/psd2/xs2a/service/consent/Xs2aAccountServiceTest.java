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

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Xs2aAccountServiceTest {

    private static final String CONSENT_ID = "consent ID";
    private static final String ACCOUNT_ID = "account ID";

    @InjectMocks
    private Xs2aAccountService xs2aAccountService;
    @Mock
    private AccountServiceEncrypted accountServiceEncrypted;

    @Test
    void saveNumberOfTransaction() {
        Xs2aTransactionParameters transactionParameters = new Xs2aTransactionParameters(5, 1, BookingStatus.BOOKED);
        UpdateTransactionParametersRequest updateTransactionParametersRequest = new UpdateTransactionParametersRequest(5, 1, BookingStatus.BOOKED);
        xs2aAccountService.saveTransactionParameters(CONSENT_ID, ACCOUNT_ID, transactionParameters);
        verify(accountServiceEncrypted).saveTransactionParameters(CONSENT_ID, ACCOUNT_ID, updateTransactionParametersRequest);
    }
}
