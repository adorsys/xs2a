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

package de.adorsys.aspsp.xs2a.spi.builder;

import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import org.apache.commons.lang3.StringUtils;

public class SpiAuthorizationCodeResultBuilder {
    private static final String AUTH_METHOD_NAME = "_name";
    private static final String CHALLENGE_DATA_DATA = "some data";
    private static final String CHALLENGE_DATA_LINK = "some link";
    private static final Integer CHALLENGE_DATA_MAX_LEN = 100;
    private static final String CHALLENGE_DATA_ADDITIONAL_INFO = "info";

    public static SpiAuthorizationCodeResult getDefaultSpiAuthorizationCodeResult(String authenticationMethodId) {
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        method.setAuthenticationMethodId(authenticationMethodId);
        method.setAuthenticationType(StringUtils.upperCase(authenticationMethodId));
        method.setName(authenticationMethodId + AUTH_METHOD_NAME);

        ChallengeData challengeData = new ChallengeData(null, CHALLENGE_DATA_DATA, CHALLENGE_DATA_LINK, CHALLENGE_DATA_MAX_LEN, null, CHALLENGE_DATA_ADDITIONAL_INFO);

        SpiAuthorizationCodeResult resultTmp = new SpiAuthorizationCodeResult();
        resultTmp.setChallengeData(challengeData);
        resultTmp.setSelectedScaMethod(method);

        return resultTmp;
    }
}
