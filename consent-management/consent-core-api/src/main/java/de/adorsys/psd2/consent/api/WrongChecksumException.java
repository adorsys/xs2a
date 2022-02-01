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

/**
 * This exception is added to terminate the flow of AIS consent persisting to CMS for the conditions below:
 * - the consent is already in VALID status;
 * - someone is trying to change definite private (ASPSP-specific) fields in this consent, for example, aspspAccountId.
 *
 * The mechanisms for checking are located in 'ChecksumCalculatingFactory.java' and support version increasing in case of
 * adding or deleting the private fields to be controlled.
 *
 * If the validation mechanism fails - changed consent and/or its account accesses are NOT persisted to CMS because of this exception,
 * which is capable of rolling back the chain of transactions.
 *
 */
public class WrongChecksumException extends Exception {

}
