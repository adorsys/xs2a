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

package de.adorsys.psd2.xs2a.core.event;

public enum EventType {
    /* PIIS Funds Confirmation */
    FUNDS_CONFIRMATION_REQUEST_RECEIVED,
    /* AIS Consent */
    CREATE_AIS_CONSENT_REQUEST_RECEIVED,
    DELETE_AIS_CONSENT_REQUEST_RECEIVED,
    GET_AIS_CONSENT_REQUEST_RECEIVED,
    GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED,
    /* AIS Consent Authorisation */
    START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED,
    UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED,
    /* AIS Account information */
    READ_ACCOUNT_DETAILS_REQUEST_RECEIVED,
    READ_ACCOUNT_LIST_REQUEST_RECEIVED,
    READ_BALANCE_REQUEST_RECEIVED,
    READ_TRANSACTION_DETAILS_REQUEST_RECEIVED,
    READ_TRANSACTION_LIST_REQUEST_RECEIVED,
    /* PIS Payment */
    PAYMENT_INITIATION_REQUEST_RECEIVED,
    GET_PAYMENT_REQUEST_RECEIVED,
    GET_TRANSACTION_STATUS_REQUEST_RECEIVED,
    PAYMENT_CANCELLATION_REQUEST_RECEIVED,
    /* PIS Payment Authorisation */
    START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED,
    GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED,
    UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED,
    UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED,
    START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED,
    GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED,
    GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED,
}
