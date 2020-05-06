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

import de.adorsys.psd2.consent.aspsp.api.CmsAspspTppInfoApi;
import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CmsAspspTppInfoController implements CmsAspspTppInfoApi {
    private final CmsAspspTppService cmsAspspTppService;

    @Override
    public ResponseEntity<TppInfo> getTppInfo(String tppAuthorisationNumber, String instanceId) {
        return cmsAspspTppService.getTppInfo(tppAuthorisationNumber, instanceId)
                   .map(record -> new ResponseEntity<>(record, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
