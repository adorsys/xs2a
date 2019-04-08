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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TppInfoCheckerService {
    private final TppService tppService;

    /**
     * Checks whether given TPP doesn't match the TPP in the current request
     *
     * @param tppInfo tpp info to be checked
     * @return <code>true</code> if given TPP is different from the one in the current request,
     * <code>false</code> otherwise.
     * If passed TppInfo is <code>null</code> or doesn't contain either authorisation number or authority ID,
     * <code>true</code> will be returned.
     */
    boolean differsFromTppInRequest(@Nullable TppInfo tppInfo) {
        if (tppInfo == null
                || tppInfo.isNotValid()) {
            return true;
        }

        TppInfo tppInRequest = tppService.getTppInfo();
        return !tppInfo.equals(tppInRequest);
    }
}
