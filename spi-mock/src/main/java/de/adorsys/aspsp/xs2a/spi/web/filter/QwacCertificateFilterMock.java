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

package de.adorsys.aspsp.xs2a.spi.web.filter;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.filter.QwacCertificateFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * The intend of this class is to return a mock certificate, when we don't want
 * to enter manually everytime the qwac certificate in case of test.
 * launch it with the "mockspi" profile.
 */
@Profile("mockspi")
@Component
@PropertySource("classpath:qwac.properties")
public class QwacCertificateFilterMock extends QwacCertificateFilter {
    @Value("${qwac-certificate-mock}")
    private String qwacCertificateMock;

    public QwacCertificateFilterMock(TppRoleValidationService tppRoleMatcher, TppInfoHolder tppInfoHolder, RequestProviderService requestProviderService) {
        super(tppRoleMatcher, tppInfoHolder, requestProviderService);
    }

    @Override
    public String getEncodedTppQwacCert(HttpServletRequest httpRequest) {
        String certificateInHeader = httpRequest.getHeader("tpp-qwac-certificate");

        return StringUtils.defaultIfBlank(certificateInHeader, qwacCertificateMock);
    }
}
