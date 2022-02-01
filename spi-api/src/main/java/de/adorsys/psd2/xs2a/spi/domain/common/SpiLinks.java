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
