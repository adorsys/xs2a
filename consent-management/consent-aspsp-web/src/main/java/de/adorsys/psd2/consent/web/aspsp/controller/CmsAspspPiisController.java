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
import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.CmsAspspPiisApi;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CmsAspspPiisController implements CmsAspspPiisApi {
    private final CmsAspspPiisService cmsAspspPiisService;

    @Override
    public ResponseEntity<CreatePiisConsentResponse> createConsent(CreatePiisConsentRequest request, String psuId,
                                                                   String psuIdType, String psuCorporateId, String psuCorporateIdType,
                                                                   String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsAspspPiisService.createConsent(psuIdData, request, instanceId)
                   .map(consentId -> new ResponseEntity<>(new CreatePiisConsentResponse(consentId), HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Override
    public ResponseData<List<CmsPiisConsent>> getConsentsForPsu(String psuId, String psuIdType, String psuCorporateId,
                                                                String psuCorporateIdType, String instanceId,
                                                                Integer pageIndex, Integer itemsPerPage) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        PageData<List<CmsPiisConsent>> consents = cmsAspspPiisService.getConsentsForPsu(psuIdData, instanceId, pageIndex, itemsPerPage);
        return ResponseData.list(
            consents.getData(),
            new CmsPageInfo(consents.getPage(), consents.getPageSize(), consents.getTotal()),
            HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> terminateConsent(String consentId, String instanceId) {
        return new ResponseEntity<>(cmsAspspPiisService.terminateConsent(consentId, instanceId), HttpStatus.OK);
    }

    private PsuIdData getPsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        return new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
    }
}
