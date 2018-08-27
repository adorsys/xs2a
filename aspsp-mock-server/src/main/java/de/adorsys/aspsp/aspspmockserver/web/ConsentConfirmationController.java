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

package de.adorsys.aspsp.aspspmockserver.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent/confirmation")
@Api(tags = "Consent confirmation for online banking", description = "Provides access to consent confirmation for online banking")
public class ConsentConfirmationController {

    @Value("${onlinebanking-mock-webapp.baseurl}")
    private String onlineBankingMockWebappUrl;

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Redirects to online banking consent confirmation page")
    public void showConfirmationPage(@PathVariable("consent-id") String consentId,
                                     HttpServletResponse response) throws IOException {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                                          .path("/{consentId}").buildAndExpand(consentId);

        response.sendRedirect(onlineBankingMockWebappUrl + uriComponents.toUriString());
    }
}
