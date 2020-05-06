/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(path = "api/v1/ais")
@Api(value = "api/v1/ais", tags = InternalCmsXs2aApiTagName.AIS_PSU_DATA)
public interface AisPsuDataApi {

    @GetMapping(path = "/consent/{consent-id}/psu-data")
    @ApiOperation(value = "Get psu data list by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<List<PsuIdData>> getPsuDataByConsentId(
        @ApiParam(name = "consent-id",
            value = "The consent identification.",
            example = "32454656712432",
            required = true)
        @PathVariable("consent-id") String consentId);
}
