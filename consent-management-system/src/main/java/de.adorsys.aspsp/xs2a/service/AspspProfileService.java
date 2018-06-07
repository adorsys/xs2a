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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.config.ProfileConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspProfileService {
    private final ProfileConfiguration profileConfiguration;

    public int getFrequencyPerDay() {
        return profileConfiguration.getFrequencyPerDay();
    }

    public boolean isCombinedServiceIndicator() {
        return profileConfiguration.isCombinedServiceIndicator();
    }

    public List<String> getAvailablePaymentProducts() {
        return profileConfiguration.getAvailablePaymentProducts();
    }

    public List<String> getAvailablePaymentTypes() {
        return profileConfiguration.getAvailablePaymentTypes();
    }

    public String getScaApproach() {
        return profileConfiguration.getScaApproach();
    }

    public int getMinFrequencyPerDay(int tppFrequency){
        return Math.min(Math.abs(tppFrequency), profileConfiguration.getFrequencyPerDay());
    }
}
