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
