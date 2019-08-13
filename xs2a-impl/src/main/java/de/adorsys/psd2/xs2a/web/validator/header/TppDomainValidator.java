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

package de.adorsys.psd2.xs2a.web.validator.header;

import com.google.common.net.InternetDomainName;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

@Service
@AllArgsConstructor
@Slf4j
public class TppDomainValidator {
    public static final String ERROR_TEXT = "URIs don't comply with domain from certificate";
    private static final String PATTERN_FOR_NORMALIZE_DOMAIN = ".*\\.(?=.*\\.)";
    private final ErrorBuildingService errorBuildingService;
    private final ScaApproachResolver scaApproachResolver;
    private final TppService tppService;
    private final RequestProviderService requestProviderService;

    public ValidationResult validate(String header) {
        if (StringUtils.isNotBlank(header) && isRedirectScaApproach()) {
            List<URL> certificateUrls = getDomainsFromTppInfo().stream()
                                            .map(this::buildURL)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());

            if (certificateUrls.isEmpty()) {
                return ValidationResult.valid();
            }

            URL urlHeader = buildURL(header);
            if (urlHeader == null) {
                return buildInvalidResult();
            }

            return isUrlCompliant(urlHeader, certificateUrls)
                       ? ValidationResult.valid()
                       : buildInvalidResult();
        }

        return ValidationResult.valid();
    }

    private boolean isUrlCompliant(URL urlHeader, List<URL> certificateUrls) {
        String topDomainUrl = getTopDomain(urlHeader.getHost());
        return certificateUrls.stream()
                   .map(URL::getHost)
                   .map(this::getTopDomain)
                   .filter(Objects::nonNull)
                   .anyMatch(topDomainCertificate -> Objects.equals(topDomainCertificate, topDomainUrl));
    }

    private URL buildURL(String domain) {
        try {
            String normalizedDomain = normalizeDomain(domain);
            URL url = new URL(getDomainWithProtocol(normalizedDomain));
            if (InternetDomainName.from(url.getHost()).isUnderPublicSuffix()) {
                return url;
            }
        } catch (MalformedURLException e) {
            log.warn("InR-ID: [{}], X-Request-ID: [{}] Cannot build URL from [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), domain);
        }

        return null;
    }

    private String normalizeDomain(String domain) {
        return domain.replaceAll(PATTERN_FOR_NORMALIZE_DOMAIN, StringUtils.EMPTY);
    }

    private List<String> getDomainsFromTppInfo() {
        TppInfo tppInfo = tppService.getTppInfo();
        List<String> dnsList = new ArrayList<>();
        Optional.ofNullable(tppInfo.getTppName()).ifPresent(dnsList::add);
        dnsList.addAll(tppInfo.getDnsList());
        return dnsList.stream()
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toList());
    }

    private String getDomainWithProtocol(String domain) {
        return domain.startsWith("http")
                   ? domain
                   : "http://" + domain;
    }

    private ValidationResult buildInvalidResult() {
        return ValidationResult.invalid(
            errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, ERROR_TEXT));
    }

    private String getTopDomain(String host) {
        try {
            return InternetDomainName.from(host).topPrivateDomain().toString();
        } catch (IllegalStateException ex) {
            log.warn("InR-ID: [{}], X-Request-ID: [{}] Cannot get top domain from [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), host);
        }
        return null;
    }

    private boolean isRedirectScaApproach() {
        return ScaApproach.REDIRECT == scaApproachResolver.resolveScaApproach();
    }
}
