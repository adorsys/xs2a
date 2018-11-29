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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The intent of this Class is to get the Qwac certificate from header, extract
 * the information inside and set an Authentication Object with extracted data
 * and roles, thus we can use a SecurityConfig extends
 * WebSecurityConfigurerAdapter to filter path by role. And a SecurityUtil class
 * have been implemented to get this TPP data everywhere.
 */
@Profile("default")
@Component
@Slf4j
@RequiredArgsConstructor
public class QwacCertificateFilter extends OncePerRequestFilter {
    private final TppRoleValidationService tppRoleValidationService;
    private final TppInfoHolder tppInfoHolder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String encodedTppQwacCert = getEncodedTppQwacCert(request);

        if (StringUtils.isNotBlank(encodedTppQwacCert)) {
            try {
                TppCertificateData tppCertificateData = CertificateExtractorUtil.extract(encodedTppQwacCert);
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

                List<String> tppRoles = tppCertificateData.getPspRoles();
                List<TppRole> xs2aTppRoles = tppRoles.stream()
                                                     .map(TppRole::valueOf)
                                                     .collect(Collectors.toList());
                tppInfo.setTppRoles(xs2aTppRoles);

                if (!tppRoleValidationService.hasAccess(tppInfo, request)) {
                    log.error("Access forbidden for TPP with authorisation number: {}", tppCertificateData.getPspAuthorisationNumber());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have access to this resource");
                    return;
                }

                tppInfoHolder.setTppInfo(tppInfo);
            } catch (CertificateValidationException e) {
                log.debug(e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                return;
            }
        }

        chain.doFilter(request, response);

    }

    public String getEncodedTppQwacCert(HttpServletRequest httpRequest) {
        return httpRequest.getHeader("tpp-qwac-certificate");
    }
}
