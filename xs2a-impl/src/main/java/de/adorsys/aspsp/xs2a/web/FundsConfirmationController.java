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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
import de.adorsys.aspsp.xs2a.service.mapper.FundsConfirmationModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.api.FundsConfirmationApi;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "PIISP, Funds confirmation", description = "Provides access to the funds confirmation")
public class FundsConfirmationController implements FundsConfirmationApi {

    private final ResponseMapper responseMapper;
    private final FundsConfirmationService fundsConfirmationService;
    private final FundsConfirmationModelMapper fundsConfirmationModelMapper;

    @Override
    public ResponseEntity<?> checkAvailabilityOfFunds(ConfirmationOfFunds body, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate) {
        return responseMapper.ok(fundsConfirmationService.fundsConfirmation(fundsConfirmationModelMapper.mapToFundsConfirmationRequest(body)));

    }
}
