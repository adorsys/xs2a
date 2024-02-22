/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.certificate.generator.controller;

import de.adorsys.psd2.certificate.generator.model.CertificateRequest;
import de.adorsys.psd2.certificate.generator.model.CertificateResponse;
import de.adorsys.psd2.certificate.generator.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Tag(name = "Certificate Controller")
@RestController
@RequestMapping("api/cert-generator")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @Operation(description = "Create a new base64 encoded X509 certificate for authentication at the "
                                 + "XS2A API with the corresponding private key and meta data")
    @PostMapping
    public ResponseEntity<CertificateResponse> createCert(@Parameter(description = "JSON request body for a certificate generation request message.", required = true)
                                                          @Valid @RequestBody CertificateRequest certificateRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                   .body(certificateService.newCertificate(certificateRequest));
    }
}
