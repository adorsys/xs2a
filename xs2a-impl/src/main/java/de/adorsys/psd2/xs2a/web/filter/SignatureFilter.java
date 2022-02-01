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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.validator.signature.DigestVerifier;
import de.adorsys.psd2.validator.signature.SignatureVerifier;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.validator.signature.service.RequestHeaders.*;
import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Component
public class SignatureFilter extends AbstractXs2aFilter {
    private static final String PATTERN_MESSAGE = "TPP unauthorized: {}";
    private final AspspProfileServiceWrapper aspspProfileService;
    private final TppErrorMessageWriter tppErrorMessageWriter;
    private final DigestVerifier digestVerifier;
    private final SignatureVerifier signatureVerifier;

    public SignatureFilter(TppErrorMessageWriter tppErrorMessageWriter, Xs2aEndpointChecker xs2aEndpointChecker, AspspProfileServiceWrapper aspspProfileService, TppErrorMessageWriter tppErrorMessageWriter1, DigestVerifier digestVerifier, SignatureVerifier signatureVerifier) {
        super(tppErrorMessageWriter, xs2aEndpointChecker);
        this.aspspProfileService = aspspProfileService;
        this.tppErrorMessageWriter = tppErrorMessageWriter1;
        this.digestVerifier = digestVerifier;
        this.signatureVerifier = signatureVerifier;
    }

    @Override
    protected void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!aspspProfileService.isTppSignatureRequired()) {
            chain.doFilter(request, response);
            return;
        }

        if (!validateHeadersExist(request, response)) {
            return;
        }

        String digest = request.getHeader(DIGEST);
        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        boolean digestValid = digestVerifier.verify(digest, body);
        if (!digestValid) {
            String errorText = "Mandatory header 'digest' is invalid!";
            log.info(PATTERN_MESSAGE, errorText);
            setResponseStatusAndErrorCode(response, FORMAT_ERROR);
            return;
        }

        Map<String, String> allHeaders = obtainRequestHeaders(request);
        String signature = request.getHeader(SIGNATURE);
        String method = request.getMethod();
        String url = request.getRequestURL().toString();
        String encodedCertificate = request.getHeader(TPP_SIGNATURE_CERTIFICATE);

        boolean signatureValid = signatureVerifier.verify(signature, encodedCertificate, allHeaders, method, url);
        if (!signatureValid) {
            String errorText = "Mandatory header 'signature' is invalid!";
            log.info(PATTERN_MESSAGE, errorText);
            setResponseStatusAndErrorCode(response, SIGNATURE_INVALID);
            return;
        }

        chain.doFilter(request, response);
    }

    private Map<String, String> obtainRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                   .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }

    private boolean validateHeadersExist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(request.getHeader(X_REQUEST_ID))) {
            String errorText = "Header 'x-request-id' is missing in request.";
            log.info(PATTERN_MESSAGE, errorText);
            setResponseStatusAndErrorCode(response, FORMAT_ERROR);
            return false;
        }

        if (StringUtils.isBlank(request.getHeader(SIGNATURE))) {
            String errorText = "Header 'signature' is missing in request.";
            log.info(PATTERN_MESSAGE, errorText);
            setResponseStatusAndErrorCode(response, SIGNATURE_MISSING);
            return false;
        }

        StringBuilder otherErrorMessages = new StringBuilder();

        Stream.of(TPP_SIGNATURE_CERTIFICATE, DIGEST, DATE)
            .filter(nm -> StringUtils.isBlank(request.getHeader(nm)))
            .forEach(nm -> appendMessageError(otherErrorMessages, nm));

        if (otherErrorMessages.length() > 0) {
            log.info(PATTERN_MESSAGE, otherErrorMessages.toString());
            setResponseStatusAndErrorCode(response, FORMAT_ERROR);
            return false;
        }

        return true;
    }

    private void appendMessageError(StringBuilder errorMessages, String headerName) {
        errorMessages.append("Header '")
            .append(headerName)
            .append("' is missing in request.")
            .append("\n");
    }

    private void setResponseStatusAndErrorCode(HttpServletResponse response, MessageErrorCode messageErrorCode) throws IOException {
        tppErrorMessageWriter.writeError(response, new TppErrorMessage(ERROR, messageErrorCode));
    }
}
