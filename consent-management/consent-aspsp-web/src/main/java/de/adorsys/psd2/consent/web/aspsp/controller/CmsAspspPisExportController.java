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
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.aspsp.api.CmsAspspPisExportApi;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class CmsAspspPisExportController implements CmsAspspPisExportApi {
    private final CmsAspspPisExportService cmsAspspPisExportService;

    @Override
    public ResponseData<Collection<CmsPayment>> getPaymentsByTpp(String tppId, LocalDate start, LocalDate end,
                                                                   String psuId, String psuIdType,
                                                                   String psuCorporateId, String psuCorporateIdType,
                                                                   String instanceId, Integer pageIndex, Integer itemsPerPage) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        PageData<Collection<CmsPayment>> payments = cmsAspspPisExportService.exportPaymentsByTpp(tppId, start, end, psuIdData, instanceId, pageIndex, itemsPerPage);
        return ResponseData.list(
            payments.getData(),
            new CmsPageInfo(payments.getPage(), payments.getPageSize(), payments.getTotal()),
            HttpStatus.OK);
    }

    @Override
    public ResponseData<Collection<CmsPayment>> getPaymentsByPsu(LocalDate start, LocalDate end,
                                                                   String psuId, String psuIdType,
                                                                   String psuCorporateId, String psuCorporateIdType,
                                                                   String instanceId, Integer pageIndex, Integer itemsPerPage) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        PageData<Collection<CmsPayment>> payments = cmsAspspPisExportService.exportPaymentsByPsu(psuIdData, start, end, instanceId, pageIndex, itemsPerPage);
        return ResponseData.list(
            payments.getData(),
            new CmsPageInfo(payments.getPage(), payments.getPageSize(), payments.getTotal()),
            HttpStatus.OK);
    }

    @Override
    public ResponseData<Collection<CmsPayment>> getPaymentsByAccountId(String aspspAccountId, LocalDate start,
                                                                         LocalDate end, String instanceId,
                                                                         Integer pageIndex, Integer itemsPerPage) {
        PageData<Collection<CmsPayment>> payments = cmsAspspPisExportService.exportPaymentsByAccountId(aspspAccountId, start, end, instanceId, pageIndex, itemsPerPage);
        return ResponseData.list(
            payments.getData(),
            new CmsPageInfo(payments.getPage(), payments.getPageSize(), payments.getTotal()),
            HttpStatus.OK);
    }
}
