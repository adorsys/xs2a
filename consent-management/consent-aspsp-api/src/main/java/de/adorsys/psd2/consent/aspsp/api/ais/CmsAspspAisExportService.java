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

package de.adorsys.psd2.consent.aspsp.api.ais;


import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.TooManyResultsException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collection;


@NotNull
public interface CmsAspspAisExportService {
    /**
     * Returns list of consents by given criteria.
     * @param tppAuthorisationNumber Mandatory TPP ID
     * @param createDateFrom         Optional starting creation date criteria
     * @param createDateTo           Optional ending creation date criteria
     * @param psuIdData              Optional Psu information criteria
     *
     * @return                       Collection of consents for TPP by given criteria.
     *                               By inconsistent criteria an empty list will be returned
     * @throws TooManyResultsException If CMS is not able to provide result due to overflow,
     *                                 developer shall limit his/her request, making pagination by dates.
     */
    Collection<AisAccountConsent> exportConsentsByTpp(String tppAuthorisationNumber,
                                                      @Nullable LocalDate createDateFrom, @Nullable LocalDate createDateTo,
                                                      @Nullable PsuIdData psuIdData);

    /**
     * Returns list of consents by given criteria.
     *
     * @param psuIdData              Mandatory Psu information criteria
     * @param createDateFrom         Optional starting creation date criteria
     * @param createDateTo           Optional ending creation date criteria
     *
     * @return                       Collection of consents for TPP by given criteria.
     *                               By inconsistent criteria an empty list will be returned
     * @throws TooManyResultsException If CMS is not able to provide result due to overflow,
     *                                 developer shall limit his/her request, making pagination by dates.
     */
    Collection<AisAccountConsent> exportConsentsByPsu(PsuIdData psuIdData,
                                                      @Nullable LocalDate createDateFrom, @Nullable LocalDate createDateTo);
}
