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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import com.google.common.net.InternetDomainName;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.TppUriCompliance;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_DOMAIN;

@Service
@AllArgsConstructor
@Slf4j
public class TppDomainValidator implements BusinessValidator<String> {
    private static final String INVALID_DOMAIN_MESSAGE = "TPP URIs are not compliant with the domain secured by the eIDAS QWAC certificate of the TPP in the field CN or SubjectAltName of the certificate";
    private static final String PATTERN_FOR_NORMALIZE_DOMAIN = "\\*.";
    private static final TppMessageInformation INVALID_DOMAIN_WARNING_MESSAGE = TppMessageInformation.buildWarning(INVALID_DOMAIN_MESSAGE);
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final ErrorBuildingService errorBuildingService;
    private final ScaApproachResolver scaApproachResolver;

    @Override
    public ValidationResult validate(String header) {
        if (StringUtils.isNotBlank(header) &&
                isRedirectScaApproach() &&
                isCheckUriComplianceToDomainSupported() &&
                isRejectMode()) {
            List<URL> certificateUrls = getCertificateUrls();

            if (certificateUrls.isEmpty()) {
                return ValidationResult.valid();
            }

            URL urlHeader = buildURL(header);
            if (urlHeader == null || !isUrlCompliant(urlHeader, certificateUrls)) {
                return buildInvalidResult();
            }
        }

        return ValidationResult.valid();
    }

    @Override
    public Set<TppMessageInformation> buildWarningMessages(String urlString) {
        Set<TppMessageInformation> warningMessages = new HashSet<>();

        if (!isCheckUriComplianceToDomainSupported()) {
            return warningMessages;
        }

        if (StringUtils.isNotBlank(urlString)) {
            List<URL> certificateUrls = getCertificateUrls();

            if (certificateUrls.isEmpty()) {
                return warningMessages;
            }

            URL url = buildURL(urlString);
            if (url == null) {
                warningMessages.add(INVALID_DOMAIN_WARNING_MESSAGE);
                return warningMessages;
            }

            if (!isUrlCompliant(url, certificateUrls)) {
                warningMessages.add(INVALID_DOMAIN_WARNING_MESSAGE);
            }

        }

        return warningMessages;
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
            if (InternetDomainName.from(url.getHost()).hasPublicSuffix()) {
                return url;
            }
        } catch (MalformedURLException e) {
            log.warn("Cannot build URL from [{}]", domain);
        }

        return null;
    }

    private String normalizeDomain(String domain) {
        return domain.replaceAll(PATTERN_FOR_NORMALIZE_DOMAIN, StringUtils.EMPTY);
    }

    private List<String> getDomainsFromTppInfo() {
        TppInfo tppInfo = tppService.getTppInfo();
        List<String> dnsList = new ArrayList<>();
        Optional.ofNullable(tppInfo.getTppName()).filter(InternetDomainName::isValid).ifPresent(dnsList::add);
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

    private String getTopDomain(String host) {
        try {
            return InternetDomainName.from(host).topPrivateDomain().toString();
        } catch (IllegalStateException ex) {
            log.warn("Cannot get top domain from [{}]", host);
        }
        return null;
    }

    private List<URL> getCertificateUrls() {
        return getDomainsFromTppInfo().stream()
                   .map(this::buildURL)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    private ValidationResult buildInvalidResult() {
        return ValidationResult.invalid(
            errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_INVALID_DOMAIN));
    }

    private boolean isRejectMode() {
        return aspspProfileServiceWrapper.getTppUriComplianceResponse() == TppUriCompliance.REJECT;
    }

    private boolean isCheckUriComplianceToDomainSupported() {
        return aspspProfileServiceWrapper.isCheckUriComplianceToDomainSupported();
    }

    private boolean isRedirectScaApproach() {
        return ScaApproach.REDIRECT == scaApproachResolver.resolveScaApproach();
    }
}
