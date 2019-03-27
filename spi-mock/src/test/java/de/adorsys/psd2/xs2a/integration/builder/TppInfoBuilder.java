/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppUniqueParamsHolder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TppInfoBuilder {
    private static final String TPP_ID = "Test TppId";
    private static final String AUTHORITY_ID = "Authority id";

    public static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        tppInfo.setAuthorityId(AUTHORITY_ID);
        tppInfo.setTppRedirectUri(buildTppRedirectUri());
        return tppInfo;
    }

    private static TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri("redirectUri", "nokRedirectUri");
    }
    
    public static TppUniqueParamsHolder buildTppUniqueParamsHolder() {
        return new TppUniqueParamsHolder(TPP_ID, AUTHORITY_ID);
    }
}
