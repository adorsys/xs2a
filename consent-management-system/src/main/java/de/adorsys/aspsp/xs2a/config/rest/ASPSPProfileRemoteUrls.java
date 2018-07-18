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

package de.adorsys.aspsp.xs2a.config.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ASPSPProfileRemoteUrls {

    @Value("${aspsp-profile.baseurl:http://localhost:48080/api/v1}")
    private String aspspProfileBaseUrl;

    /**
     * Returns URL-string in order to get frequency per day
     *
     * @return String
     */
    public String getFrequencyPerDay() {
        return aspspProfileBaseUrl + "/aspsp-profile/frequency-per-day";
    }
}
