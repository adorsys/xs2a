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
