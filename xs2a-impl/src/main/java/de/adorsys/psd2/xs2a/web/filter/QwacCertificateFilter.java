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
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.mapper.TppInfoRolesMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aTppInfoMapper;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

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
public class QwacCertificateFilter extends AbstractXs2aFilter {
    private final TppInfoHolder tppInfoHolder;
    private final RequestProviderService requestProviderService;
    private final TppErrorMessageBuilder tppErrorMessageBuilder;
    private final TppRoleValidationService tppRoleValidationService;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final Xs2aTppInfoMapper xs2aTppInfoMapper;
    private final TppInfoRolesMapper tppInfoRolesMapper;
    private final TppErrorMessageWriter tppErrorMessageWriter;

    public QwacCertificateFilter(TppErrorMessageWriter tppErrorMessageWriter, RequestPathResolver requestPathResolver, TppInfoHolder tppInfoHolder, RequestProviderService requestProviderService, TppErrorMessageBuilder tppErrorMessageBuilder, TppRoleValidationService tppRoleValidationService, TppService tppService, AspspProfileServiceWrapper aspspProfileService, Xs2aTppInfoMapper xs2aTppInfoMapper, TppInfoRolesMapper tppInfoRolesMapper, TppErrorMessageWriter tppErrorMessageWriter1) {
        super(tppErrorMessageWriter, requestPathResolver);
        this.tppInfoHolder = tppInfoHolder;
        this.requestProviderService = requestProviderService;
        this.tppErrorMessageBuilder = tppErrorMessageBuilder;
        this.tppRoleValidationService = tppRoleValidationService;
        this.tppService = tppService;
        this.aspspProfileService = aspspProfileService;
        this.xs2aTppInfoMapper = xs2aTppInfoMapper;
        this.tppInfoRolesMapper = tppInfoRolesMapper;
        this.tppErrorMessageWriter = tppErrorMessageWriter1;
    }

    @Override
    protected void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String encodedTppQwacCert = getEncodedTppQwacCert();

        if (StringUtils.isNotBlank(encodedTppQwacCert)) {
            try {
                TppCertificateData tppCertificateData = CertificateExtractorUtil.extract(encodedTppQwacCert);
                if (isCertificateExpired(tppCertificateData.getNotAfter())) {
                    buildCertificateExpiredErrorResponse(response);
                    return;
                }

                TppInfo tppInfo = xs2aTppInfoMapper.mapToTppInfo(tppCertificateData);
                String tppRolesAllowedHeader = requestProviderService.getTppRolesAllowedHeader();
                boolean checkTppRolesFromHeader = StringUtils.isNotBlank(tppRolesAllowedHeader);
                boolean checkTppRolesFromCertificate = aspspProfileService.isCheckTppRolesFromCertificateSupported();
                if (checkTppRolesFromHeader) {
                    processTppRolesFromHeader(tppInfo, tppRolesAllowedHeader);
                } else if (checkTppRolesFromCertificate) {
                    processTppRolesFromCertificate(tppInfo, tppCertificateData);
                }

                boolean checkTppRoles = checkTppRolesFromHeader || checkTppRolesFromCertificate;
                if (checkTppRoles && !tppRoleValidationService.hasAccess(tppInfo, request)) {
                    buildRoleInvalidErrorResponse(response, tppCertificateData);
                    return;
                }

                tppInfoHolder.setTppInfo(tppInfo);
            } catch (CertificateValidationException e) {
                buildCertificateInvalidNoAccessErrorResponse(response, e);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    protected String getEncodedTppQwacCert() {
        return requestProviderService.getEncodedTppQwacCert();
    }

    private void processTppRolesFromCertificate(TppInfo tppInfo, TppCertificateData tppCertificateData) {
        List<TppRole> xs2aTppRoles = tppCertificateData.getPspRoles().stream()
                                         .map(TppRole::valueOf)
                                         .collect(Collectors.toList());

        setTppRolesAndUpdateTppInfo(tppInfo, xs2aTppRoles);
    }

    private void processTppRolesFromHeader(TppInfo tppInfo, String tppRolesAllowedHeader) {
        Optional.of(tppRolesAllowedHeader)
            .map(roles -> roles.split(","))
            .map(Arrays::asList)
            .map(tppInfoRolesMapper::mapToTppRoles)
            .ifPresent(roles -> setTppRolesAndUpdateTppInfo(tppInfo, roles));
    }

    private void setTppRolesAndUpdateTppInfo(TppInfo tppInfo, List<TppRole> roles) {
        if (!roles.isEmpty()) {
            tppInfo.setTppRoles(roles);
            tppService.updateTppInfo(tppInfo);
        }
    }

    private boolean isCertificateExpired(Date date) {
        return Optional.ofNullable(date)
                   .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                   .map(d -> d.isBefore(LocalDateTime.now()))
                   .orElse(true);
    }

    private void buildCertificateInvalidNoAccessErrorResponse(HttpServletResponse response, CertificateValidationException e) throws IOException {
        log.info("InR-ID: [{}], X-Request-ID: [{}], TPP unauthorised because CertificateValidationException: {}",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), e.getMessage());
        setResponseStatusAndErrorCode(response, CERTIFICATE_INVALID_NO_ACCESS);
    }

    private void buildRoleInvalidErrorResponse(HttpServletResponse response, TppCertificateData tppCertificateData) throws IOException {
        log.info("InR-ID: [{}], X-Request-ID: [{}], Access forbidden for TPP with authorisation number: [{}]",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), tppCertificateData.getPspAuthorisationNumber());
        setResponseStatusAndErrorCode(response, ROLE_INVALID);
    }

    private void buildCertificateExpiredErrorResponse(HttpServletResponse response) throws IOException {
        log.info("InR-ID: [{}], X-Request-ID: [{}], TPP Certificate is expired",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());
        setResponseStatusAndErrorCode(response, CERTIFICATE_EXPIRED);
    }

    private void setResponseStatusAndErrorCode(HttpServletResponse response, MessageErrorCode messageErrorCode) throws IOException {
        tppErrorMessageWriter.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, tppErrorMessageBuilder.buildTppErrorMessage(ERROR, messageErrorCode));
    }
}
