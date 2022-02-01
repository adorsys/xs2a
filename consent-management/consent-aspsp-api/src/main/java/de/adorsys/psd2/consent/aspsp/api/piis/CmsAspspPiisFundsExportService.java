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

package de.adorsys.psd2.consent.aspsp.api.piis;


import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.TooManyResultsException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collection;


@NotNull
public interface CmsAspspPiisFundsExportService {
    /**
     * Returns list of consents by given criteria.
     *
     * @param tppAuthorisationNumber Mandatory TPP ID
     * @param createDateFrom         Optional starting creation date criteria
     * @param createDateTo           Optional ending creation date criteria
     * @param psuIdData              Optional Psu information criteria
     * @param instanceId             Optional id of particular service instance.
     *                               If it's not provided, default value will be used instead.
     *
     * @param pageIndex              index of current page
     * @param itemsPerPage           quantity of consents on one page
     * @return Collection of consents for TPP by given criteria.
     * By inconsistent criteria an empty list will be returned
     * @throws TooManyResultsException If CMS is not able to provide result due to overflow,
     *                                 developer shall limit his/her request, making pagination by dates.
     */
    PageData<Collection<CmsPiisConsent>> exportConsentsByTpp(String tppAuthorisationNumber,
                                                   @Nullable LocalDate createDateFrom,
                                                   @Nullable LocalDate createDateTo,
                                                   @Nullable PsuIdData psuIdData, @Nullable String instanceId,
                                                   Integer pageIndex, Integer itemsPerPage
    );

    /**
     * Returns list of consents by given criteria.
     *
     * @param psuIdData      Mandatory Psu information criteria
     * @param createDateFrom Optional starting creation date criteria
     * @param createDateTo   Optional ending creation date criteria
     * @param instanceId     Optional id of particular service instance.
     *                       If it's not provided, default value will be used instead.
     *
     * @param pageIndex index of current page
     * @param itemsPerPage quantity of consents on one page
     * @return Collection of consents for PSU by given criteria.
     * By inconsistent criteria an empty list will be returned
     * @throws TooManyResultsException If CMS is not able to provide result due to overflow,
     *                                 developer shall limit his/her request, making pagination by dates.
     */
    PageData<Collection<CmsPiisConsent>> exportConsentsByPsu(PsuIdData psuIdData,
                                                   @Nullable LocalDate createDateFrom,
                                                   @Nullable LocalDate createDateTo,
                                                   @Nullable String instanceId,
                                                   Integer pageIndex, Integer itemsPerPage
    );

    /**
     * Returns list of consents by given criteria.
     *
     * @param aspspAccountId Bank specific account identifier
     * @param createDateFrom Optional starting creation date criteria
     * @param createDateTo   Optional ending creation date criteria
     * @param instanceId     Optional id of particular service instance.
     *                       If it's not provided, default value will be used instead.
     *
     * @param pageIndex index of current page
     * @param itemsPerPage quantity of consents on one page
     * @return Collection of consents by given criteria.
     * By inconsistent criteria an empty list will be returned
     * @throws TooManyResultsException If CMS is not able to provide result due to overflow,
     *                                 developer shall limit his/her request, making pagination by dates.
     */
    PageData<Collection<CmsPiisConsent>> exportConsentsByAccountId(@NotNull String aspspAccountId,
                                                                   @Nullable LocalDate createDateFrom, @Nullable LocalDate createDateTo,
                                                                   @Nullable String instanceId,
                                                                   Integer pageIndex, Integer itemsPerPage
    );

}
