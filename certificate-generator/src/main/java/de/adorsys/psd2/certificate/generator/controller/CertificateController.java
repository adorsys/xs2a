package de.adorsys.psd2.certificate.generator.controller;

import de.adorsys.psd2.certificate.generator.model.CertificateRequest;
import de.adorsys.psd2.certificate.generator.model.CertificateResponse;
import de.adorsys.psd2.certificate.generator.service.CertificateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(value = "Certificate Controller")
@RestController
@RequestMapping("api/cert-generator")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @ApiOperation(value = "Create a new base64 encoded X509 certificate for authentication at the "
                              + "XS2A API with the corresponding private key and meta data", response = CertificateResponse.class)
    @PostMapping
    public ResponseEntity<CertificateResponse> createCert(@ApiParam(value = "JSON request body for a certificate generation request message.", required = true)
                                                          @Valid @RequestBody CertificateRequest certificateRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                   .body(certificateService.newCertificate(certificateRequest));
    }
}
