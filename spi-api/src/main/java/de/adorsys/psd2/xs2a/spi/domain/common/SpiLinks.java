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

package de.adorsys.psd2.xs2a.spi.domain.common;

import lombok.Data;

@Data
public class SpiLinks {
    private SpiHrefType scaRedirect;
    private SpiHrefType scaOAuth;
    private SpiHrefType updatePsuIdentification;
    private SpiHrefType updateProprietaryData;
    private SpiHrefType updatePsuAuthentication;
    private SpiHrefType selectAuthenticationMethod;
    private SpiHrefType self;
    private SpiHrefType status;
    private SpiHrefType account;
    private SpiHrefType balances;
    private SpiHrefType transactions;
    private SpiHrefType first;
    private SpiHrefType next;
    private SpiHrefType previous;
    private SpiHrefType last;
    private SpiHrefType download;
    private SpiHrefType startAuthorisation;
    private SpiHrefType startAuthorisationWithPsuIdentification;
    private SpiHrefType startAuthorisationWithPsuAuthentication;
    private SpiHrefType startAuthorisationWithAuthenticationMethodSelection;//NOPMD naming according to spec!
    private SpiHrefType startAuthorisationWithTransactionAuthorisation;
    private SpiHrefType scaStatus;
    private SpiHrefType authoriseTransaction;
    private SpiHrefType confirmation;
    private SpiHrefType card;
}
