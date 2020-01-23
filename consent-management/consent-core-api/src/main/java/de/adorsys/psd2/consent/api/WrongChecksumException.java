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
