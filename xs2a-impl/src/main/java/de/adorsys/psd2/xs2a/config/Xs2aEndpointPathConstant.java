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

package de.adorsys.psd2.xs2a.config;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Xs2aEndpointPathConstant {
    public static final String PREFIX_V1 = "/v1";
    public static final String PREFIX_V2 = "/v2";
    // v1
    public static final String ACCOUNTS_PATH = PREFIX_V1 + "/accounts/**";
    public static final String BENEFICIARIES_PATH = PREFIX_V1 + "/trusted-beneficiaries";
    public static final String CARD_ACCOUNTS_PATH = PREFIX_V1 + "/card-accounts/**";
    public static final String CONSENTS_PATH = PREFIX_V1 + "/consents/**";
    public static final String FUNDS_CONFIRMATION_PATH = PREFIX_V1 + "/funds-confirmations/**";
    public static final String SINGLE_PAYMENTS_PATH = PREFIX_V1 + "/payments/**";
    public static final String BULK_PAYMENTS_PATH = PREFIX_V1 + "/bulk-payments/**";
    public static final String PERIODIC_PAYMENTS_PATH = PREFIX_V1 + "/periodic-payments/**";
    public static final String SIGNING_BASKETS_PATH = PREFIX_V1 + "/signing-baskets/**";
    public static final String GLOBAL_PATH = PREFIX_V1 + "/**";
    // v2
    public static final String CONSENTS_V2_PATH = PREFIX_V2 + "/consents/**";

    public static String[] getAllXs2aEndpointPaths() {
        return new String[]{ACCOUNTS_PATH, BENEFICIARIES_PATH, CARD_ACCOUNTS_PATH, CONSENTS_PATH, FUNDS_CONFIRMATION_PATH, SINGLE_PAYMENTS_PATH, BULK_PAYMENTS_PATH, PERIODIC_PAYMENTS_PATH, SIGNING_BASKETS_PATH, CONSENTS_V2_PATH};
    }
}
