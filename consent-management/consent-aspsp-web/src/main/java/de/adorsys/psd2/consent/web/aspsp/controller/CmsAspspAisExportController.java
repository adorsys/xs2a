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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.api.CmsPageInfo;
import de.adorsys.psd2.consent.api.ResponseData;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.CmsAspspAisExportApi;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService;
import de.adorsys.psd2.xs2a.core.pagination.data.PageRequestParameters;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class CmsAspspAisExportController implements CmsAspspAisExportApi {
    private final CmsAspspAisExportService cmsAspspAisExportService;

    @Override
    public ResponseData<Collection<CmsAisAccountConsent>> getConsentsByTpp(String tppId, LocalDate start,
                                                                           LocalDate end, String psuId,
                                                                           String psuIdType, String psuCorporateId,
                                                                           String psuCorporateIdType, String instanceId,
                                                                           Integer pageIndex, Integer itemsPerPage,
                                                                           String additionalTppInfo) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        PageRequestParameters pageRequestParameters = new PageRequestParameters(pageIndex, itemsPerPage);
        PageData<Collection<CmsAisAccountConsent>> consents =
            cmsAspspAisExportService.exportConsentsByTpp(tppId, start, end, psuIdData, instanceId, pageRequestParameters, additionalTppInfo);
        return ResponseData.list(
            consents.getData(),
            new CmsPageInfo(consents.getPage(), consents.getPageSize(), consents.getTotal()),
            HttpStatus.OK);
    }

    @Override
    public ResponseData<Collection<CmsAisAccountConsent>> getConsentsByPsu(LocalDate start, LocalDate end,
                                                                           String psuId, String psuIdType,
                                                                           String psuCorporateId,
                                                                           String psuCorporateIdType, String instanceId,
                                                                           Integer pageIndex, Integer itemsPerPage,
                                                                           String additionalTppInfo) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        PageData<Collection<CmsAisAccountConsent>> consents =
            cmsAspspAisExportService.exportConsentsByPsuAndAdditionalTppInfo(psuIdData, start, end, instanceId,
                                                                             pageIndex, itemsPerPage, additionalTppInfo);
        return ResponseData.list(
            consents.getData(),
            new CmsPageInfo(consents.getPage(), consents.getPageSize(), consents.getTotal()),
            HttpStatus.OK);
    }

    @Override
    public ResponseData<Collection<CmsAisAccountConsent>> getConsentsByAccount(String aspspAccountId, LocalDate start,
                                                                               LocalDate end, String instanceId,
                                                                               Integer pageIndex, Integer itemsPerPage,
                                                                               String additionalTppInfo) {
        PageData<Collection<CmsAisAccountConsent>> consents =
            cmsAspspAisExportService.exportConsentsByAccountIdAndAdditionalTppInfo(aspspAccountId, start, end,
                                                                                   instanceId, pageIndex, itemsPerPage,
                                                                                   additionalTppInfo);
        return ResponseData.list(
            consents.getData(),
            new CmsPageInfo(consents.getPage(), consents.getPageSize(), consents.getTotal()),
            HttpStatus.OK);
    }
}
