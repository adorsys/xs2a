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

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.CmsAspspPiisExportApi;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class CmsAspspPiisExportController implements CmsAspspPiisExportApi {
    private final CmsAspspPiisFundsExportService cmsAspspPiisExportService;

    @Override
    public ResponseEntity<Collection<CmsPiisConsent>> getConsentsByTpp(String tppId, LocalDate start, LocalDate end, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        Collection<CmsPiisConsent> consents = cmsAspspPiisExportService.exportConsentsByTpp(tppId, start, end, psuIdData, instanceId);
        return new ResponseEntity<>(consents, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Collection<CmsPiisConsent>> getConsentsByPsu(LocalDate start, LocalDate end, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        Collection<CmsPiisConsent> consents = cmsAspspPiisExportService.exportConsentsByPsu(psuIdData, start, end, instanceId);
        return new ResponseEntity<>(consents, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Collection<CmsPiisConsent>> getConsentsByAccountId(String aspspAccountId, LocalDate start, LocalDate end, String instanceId) {
        Collection<CmsPiisConsent> consents = cmsAspspPiisExportService.exportConsentsByAccountId(aspspAccountId, start, end, instanceId);
        return new ResponseEntity<>(consents, HttpStatus.OK);
    }
}
