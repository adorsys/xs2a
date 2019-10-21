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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;

/**
 * The intent of this Class is to get the Qwac certificate from header, extract
 * the information inside and set an Authentication Object with extracted data
 * and roles, thus we can use a SecurityConfig extends
 * WebSecurityConfigurerAdapter to filter path by role. And a SecurityUtil class
 * have been implemented to get this TPP data everywhere.
 */
@Profile("!mock-qwac")
@Component
@Slf4j
@RequiredArgsConstructor
public class QwacCertificateFilter extends AbstractXs2aFilter {
    private final TppInfoHolder tppInfoHolder;
    private final RequestProviderService requestProviderService;
    private final TppErrorMessageBuilder tppErrorMessageBuilder;
    private final TppRoleValidationService tppRoleValidationService;
    private final TppService tppService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String encodedTppQwacCert = getEncodedTppQwacCert(request);

        if (StringUtils.isNotBlank(encodedTppQwacCert)) {
            try {
                TppCertificateData tppCertificateData = CertificateExtractorUtil.extract(encodedTppQwacCert);

                if (isCertificateExpired(tppCertificateData.getNotAfter())) {
                    log.info("InR-ID: [{}], X-Request-ID: [{}], TPP Certificate is expired",
                             requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().print(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, CERTIFICATE_EXPIRED));
                    return;
                }

                TppInfo tppInfo = new TppInfo();
                tppInfo.setAuthorisationNumber(tppCertificateData.getPspAuthorisationNumber());
                tppInfo.setTppName(tppCertificateData.getName());
                tppInfo.setAuthorityId(tppCertificateData.getPspAuthorityId());
                tppInfo.setAuthorityName(tppCertificateData.getPspAuthorityName());
                tppInfo.setCountry(tppCertificateData.getCountry());
                tppInfo.setOrganisation(tppCertificateData.getOrganisation());
                tppInfo.setOrganisationUnit(tppCertificateData.getOrganisationUnit());
                tppInfo.setCity(tppCertificateData.getCity());
                tppInfo.setState(tppCertificateData.getState());
                tppInfo.setIssuerCN(tppCertificateData.getIssuerCN());
                tppInfo.setDnsList(tppCertificateData.getDnsList());

                String tppRolesAllowedHeader = requestProviderService.getTppRolesAllowedHeader();
                if (StringUtils.isNotBlank(tppRolesAllowedHeader)) {
                    Optional.of(tppRolesAllowedHeader)
                        .map(roles -> roles.split(","))
                        .map(Arrays::asList)
                        .map(this::mapToTppRoles)
                        .ifPresent(roles -> {
                            tppInfo.setTppRoles(roles);
                            tppService.updateTppInfo(tppInfo);
                        });

                    if (!tppRoleValidationService.hasAccess(tppInfo, request)) {
                        log.info("InR-ID: [{}], X-Request-ID: [{}], Access forbidden for TPP with authorisation number: [{}]",
                                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), tppCertificateData.getPspAuthorisationNumber());
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().print(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, ROLE_INVALID));
                        return;
                    }
                } else {
                    Optional.of(tppCertificateData.getPspRoles())
                        .map(this::mapToTppRoles)
                        .ifPresent(tppInfo::setTppRoles);
                }

                tppInfoHolder.setTppInfo(tppInfo);
            } catch (CertificateValidationException e) {
                log.info("InR-ID: [{}], X-Request-ID: [{}], TPP unauthorised because CertificateValidationException: {}",
                         requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, CERTIFICATE_INVALID_NO_ACCESS));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private List<TppRole> mapToTppRoles(List<String> rolesList) {
        return rolesList.stream()
                   .map(String::trim)
                   .map(this::getTppRole)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    private TppRole getTppRole(String role) {
        return Arrays.stream(TppRole.values())
                   .map(Enum::toString)
                   .filter(roleString -> roleString.equals(role))
                   .findFirst()
                   .map(TppRole::valueOf)
                   .orElse(null);
    }

    public String getEncodedTppQwacCert(HttpServletRequest httpRequest) {
        return httpRequest.getHeader("tpp-qwac-certificate");
    }

    private boolean isCertificateExpired(Date date) {
        return Optional.ofNullable(date)
                   .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                   .map(d -> d.isBefore(LocalDateTime.now()))
                   .orElse(true);
    }
}
