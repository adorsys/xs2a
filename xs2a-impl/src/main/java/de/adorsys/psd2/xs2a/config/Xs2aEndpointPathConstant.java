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

package de.adorsys.psd2.xs2a.config;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Xs2aEndpointPathConstant {
    // v1
    public static final String ACCOUNTS_PATH = "/v1/accounts/**";
    public static final String BENEFICIARIES_PATH = "/v1/trusted-beneficiaries";
    public static final String CARD_ACCOUNTS_PATH = "/v1/card-accounts/**";
    public static final String CONSENTS_PATH = "/v1/consents/**";
    public static final String FUNDS_CONFIRMATION_PATH = "/v1/funds-confirmations/**";
    public static final String SINGLE_PAYMENTS_PATH = "/v1/payments/**";
    public static final String BULK_PAYMENTS_PATH = "/v1/bulk-payments/**";
    public static final String PERIODIC_PAYMENTS_PATH = "/v1/periodic-payments/**";
    public static final String SIGNING_BASKETS_PATH = "/v1/signing-baskets/**";
    public static final String GLOBAL_PATH = "/v1/**";
    // v2
    public static final String CONSENTS_V2_PATH = "/v2/consents/**";

    public static String[] getAllXs2aEndpointPaths() {
        return new String[]{ACCOUNTS_PATH, BENEFICIARIES_PATH, CARD_ACCOUNTS_PATH, CONSENTS_PATH, FUNDS_CONFIRMATION_PATH, SINGLE_PAYMENTS_PATH, BULK_PAYMENTS_PATH, PERIODIC_PAYMENTS_PATH, SIGNING_BASKETS_PATH, CONSENTS_V2_PATH};
    }
}
