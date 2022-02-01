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
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aAccountService {

    private final AccountServiceEncrypted accountServiceEncrypted;

    public void saveTransactionParameters(String consentId, String resourceId, Xs2aTransactionParameters transactionParameters) {
        accountServiceEncrypted.saveTransactionParameters(consentId, resourceId,
                                                          new UpdateTransactionParametersRequest(transactionParameters.getNumberOfTransactions(),
                                                                                                 transactionParameters.getTotalPages(),
                                                                                                 transactionParameters.getBookingStatus()));
    }
}
