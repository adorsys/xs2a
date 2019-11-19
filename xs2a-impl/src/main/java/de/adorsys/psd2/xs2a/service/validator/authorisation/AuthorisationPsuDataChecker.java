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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorisationPsuDataChecker {

    public boolean isPsuDataWrong(boolean isMultilevelSca, List<PsuIdData> psuDataFromDb, PsuIdData psuDataFromRequest) {

        return !isMultilevelSca
                   && CollectionUtils.isNotEmpty(psuDataFromDb)
                   && psuDataFromRequest.isNotEmpty()
                   && isPsuDataDiffers(psuDataFromDb.iterator().next(), psuDataFromRequest);
    }

    private boolean isPsuDataDiffers(PsuIdData psuIdDataFromDb, PsuIdData psuDataFromRequest) {
        return !psuDataFromRequest.contentEquals(psuIdDataFromDb);
    }
}
