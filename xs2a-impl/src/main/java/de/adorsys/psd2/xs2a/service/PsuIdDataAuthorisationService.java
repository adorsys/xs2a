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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PsuIdDataAuthorisationService {
    private final Xs2aAuthorisationService authorisationService;

    public PsuIdData getPsuIdData(String authorisationId, List<PsuIdData> psuIdDataList) {
        PsuIdData psuIdData = authorisationService.getAuthorisationById(authorisationId)
                                  .map(Authorisation::getPsuIdData)
                                  .orElse(null);
        if (psuIdData == null && !psuIdDataList.isEmpty()) {
            // This is done for multilevel accounts, since we don't know which PSU did the request, we take first one
            psuIdData = psuIdDataList.get(0);
        }
        return psuIdData != null ? psuIdData : new PsuIdData();
    }
}
