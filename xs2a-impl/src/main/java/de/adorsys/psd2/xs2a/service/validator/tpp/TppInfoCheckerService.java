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
