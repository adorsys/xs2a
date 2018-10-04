/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.aspspmockserver.domain;

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
