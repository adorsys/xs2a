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

package de.adorsys.psd2.xs2a.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * The intend of this class is to return a mock certificate, when we don't want
 * to enter manually everytime the qwac certificate in case of test.
 * launch it with the "mock-qwac" profile.
 */
@Profile("mock-qwac")
@Service
@PropertySource("classpath:qwac.properties")
public class RequestProviderServiceMock extends RequestProviderService {

    @Value("${qwac-certificate-mock}")
    private String qwacCertificateMock;

    @Autowired
    public RequestProviderServiceMock(HttpServletRequest httpServletRequest,
                                      InternalRequestIdService internalRequestIdService) {
        super(httpServletRequest, internalRequestIdService);
    }

    @Override
    public String getEncodedTppQwacCert() {
        return StringUtils.defaultIfBlank(super.getEncodedTppQwacCert(), qwacCertificateMock);
    }
}
