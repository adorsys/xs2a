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

import de.adorsys.aspsp.aspspmockserver.service.PsuAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/confirm")
public class TanConfirmationController {
    private final PsuAuthenticationService psuAuthenticationService;

    @GetMapping(path = "/{psu-id}/")
    public ModelAndView showConfirmationPage(TanConfirmationObject tanConfirmationObject,
                                             @PathVariable("psu-id") String psuId) {
        tanConfirmationObject.setPsuId(psuId);
        return new ModelAndView("confirmation", "tanConfirmationObject", tanConfirmationObject);
    }

    @PostMapping(path = "/{psu-id}/validate")
    public ModelAndView confirmTan(
        @ModelAttribute("tanConfirmationObject") TanConfirmationObject tanConfirmationObject,
        @PathVariable("psu-id") String psuId) {
        return psuAuthenticationService.isPsuTanNumberValid(psuId, tanConfirmationObject.getTanNumber())
                   ? new ModelAndView("confirmationSuccess")
                   : new ModelAndView("confirmationError");
    }
}
