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

package de.adorsys.psd2.consent.api;

public enum ActionStatus {
    SUCCESS,                //Operation successful
    BAD_PAYLOAD,            //Some of the requested data is malformed or missing
    FAILURE_ACCOUNT,        //The requested account access to the Account Information is not found in Consent AccountAccess section
    FAILURE_BALANCE,        //The requested account access to the Balances Information is not found in Consent BalancesAccess section
    FAILURE_TRANSACTION,    //The requested account access to the Transaction Information is not found in Consent TransactionsAccess section
    FAILURE_PAYMENT,        //The corresponding access is not granted by the Consent
    CONSENT_NOT_FOUND,      //Consent Not Found
    CONSENT_INVALID_STATUS, //Consent Status is invalid
    CONSENT_LIMIT_EXCEEDED  //The daily access limit is exceeded
}
