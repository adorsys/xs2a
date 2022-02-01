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
