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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PsuDataUpdateAuthorisationChecker {

    public boolean canPsuUpdateAuthorisation(@NotNull PsuIdData psuIdDataRequest, @Nullable PsuIdData psuIdDataAuthorisation) {
        return isPsuPresent(psuIdDataAuthorisation)
                   ? psuIdDataRequest.isEmpty() || psuIdDataAuthorisation.contentEquals(psuIdDataRequest)
                   : psuIdDataRequest.isNotEmpty();
    }

    public boolean areBothPsusAbsent(@NotNull PsuIdData psuIdDataRequest, @Nullable PsuIdData psuIdDataAuthorisation) {
        return isPsuAbsent(psuIdDataAuthorisation) && isPsuAbsent(psuIdDataRequest);
    }

    private boolean isPsuPresent(PsuIdData psu) {
        return psu != null && psu.isNotEmpty();
    }

    private boolean isPsuAbsent(PsuIdData psu) {
        return !isPsuPresent(psu);
    }
}
