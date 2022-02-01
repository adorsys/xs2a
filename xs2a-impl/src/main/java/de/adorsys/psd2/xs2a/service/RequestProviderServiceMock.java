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
