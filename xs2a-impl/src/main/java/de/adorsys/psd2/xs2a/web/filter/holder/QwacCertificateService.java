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

package de.adorsys.psd2.xs2a.web.filter.holder;

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
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aTppInfoMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

@Slf4j
@AllArgsConstructor
@Service
public class QwacCertificateService {
    private final TppInfoHolder tppInfoHolder;
    private final RequestProviderService requestProviderService;
    private final TppRoleValidationService tppRoleValidationService;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final Xs2aTppInfoMapper xs2aTppInfoMapper;
    private final TppErrorMessageWriter tppErrorMessageWriter;

    /**
     * Checks if certificate is applicable
     * @param request is incoming HttpServletRequest
     * @param response is outcoming HttpServletResponse
     * @param encodedTppQwacCert is a certificate obtained from request header 'tpp-qwac-certificate'
     * @return  true, if certificate is not expired, can be parsed, contains appropriate roles to access resource;
     *          false otherwise
     * @throws IOException if certificate is expired
     */
    public boolean isApplicable(HttpServletRequest request, HttpServletResponse response,
                                String encodedTppQwacCert) throws IOException {
        try {
            TppCertificateData tppCertificateData = CertificateExtractorUtil.extract(encodedTppQwacCert);
            if (isCertificateExpired(tppCertificateData.getNotAfter())) {
                buildCertificateExpiredErrorResponse(response);
                return false;
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
                return false;
            }
            tppInfoHolder.setTppInfo(tppInfo);
            return true;
        } catch (CertificateValidationException e) {
            buildCertificateInvalidNoAccessErrorResponse(response, e);
            return false;
        }
    }

    private void buildCertificateInvalidNoAccessErrorResponse(HttpServletResponse response,
                                                              CertificateValidationException e) throws IOException {
        log.info("TPP unauthorised because CertificateValidationException: {}", e.getMessage());
        setResponseStatusAndErrorCode(response, CERTIFICATE_INVALID_NO_ACCESS);
    }

    private void setResponseStatusAndErrorCode(HttpServletResponse response,
                                               MessageErrorCode messageErrorCode) throws IOException {
        tppErrorMessageWriter.writeError(response, new TppErrorMessage(ERROR, messageErrorCode));
    }

    private boolean isCertificateExpired(Date date) {
        return Optional.ofNullable(date)
            .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
            .map(d -> d.isBefore(LocalDateTime.now()))
            .orElse(true);
    }

    private void buildRoleInvalidErrorResponse(HttpServletResponse response,
                                               TppCertificateData tppCertificateData) throws IOException {
        log.info("Access forbidden for TPP with authorisation number: [{}]", tppCertificateData.getPspAuthorisationNumber());
        setResponseStatusAndErrorCode(response, ROLE_INVALID);
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
            .map(xs2aTppInfoMapper::mapToTppRoles)
            .ifPresent(roles -> setTppRolesAndUpdateTppInfo(tppInfo, roles));
    }

    private void setTppRolesAndUpdateTppInfo(TppInfo tppInfo, List<TppRole> roles) {
        if (!roles.isEmpty()) {
            tppInfo.setTppRoles(roles);
            tppService.updateTppInfo(tppInfo);
        }
    }

    private void buildCertificateExpiredErrorResponse(HttpServletResponse response) throws IOException {
        log.info("TPP Certificate is expired");
        setResponseStatusAndErrorCode(response, CERTIFICATE_EXPIRED);
    }
}
