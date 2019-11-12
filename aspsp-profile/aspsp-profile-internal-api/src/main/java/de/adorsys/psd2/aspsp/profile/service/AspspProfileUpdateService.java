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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;

import java.util.List;

public interface AspspProfileUpdateService {
    /**
     * Updates SCA approaches in ASPSP profile by replacing all existing approaches with new ones
     *
     * @param scaApproaches SCA approaches to be set in the profile
     */
    void updateScaApproaches(List<ScaApproach> scaApproaches);

    /**
     * Updates ASPSP settings in ASPSP profile by replacing all existing values with new ones
     *
     * @param aspspSettings ASPSP Settings to be set in the profile
     */
    void updateAspspSettings(AspspSettings aspspSettings);
}
