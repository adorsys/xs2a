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
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.mapper.TppInfoRolesMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aTppInfoMapper;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * The intend of this class is to return a mock certificate, when we don't want
 * to enter manually everytime the qwac certificate in case of test.
 * launch it with the "mock-qwac" profile.
 */
@Profile("mock-qwac")
@Component
@PropertySource("classpath:qwac.properties")
public class QwacCertificateFilterMock extends QwacCertificateFilter {
    @Value("${qwac-certificate-mock}")
    private String qwacCertificateMock;
    private final RequestProviderService requestProviderService;

    public QwacCertificateFilterMock(RequestPathResolver requestPathResolver, TppInfoHolder tppInfoHolder,
                                     RequestProviderService requestProviderService, TppErrorMessageBuilder tppErrorMessageBuilder,
                                     TppRoleValidationService tppRoleValidationService, TppService tppService,
                                     AspspProfileServiceWrapper aspspProfileService, Xs2aTppInfoMapper xs2aTppInfoMapper,
                                     TppInfoRolesMapper tppInfoRolesMapper, TppErrorMessageWriter tppErrorMessageWriter) {
        super(requestPathResolver, tppInfoHolder,
              requestProviderService, tppErrorMessageBuilder,
              tppRoleValidationService, tppService,
              aspspProfileService, xs2aTppInfoMapper,
              tppInfoRolesMapper, tppErrorMessageWriter);
        this.requestProviderService = requestProviderService;
    }

    @Override
    public String getEncodedTppQwacCert() {
        return StringUtils.defaultIfBlank(requestProviderService.getEncodedTppQwacCert(), qwacCertificateMock);
    }
}
