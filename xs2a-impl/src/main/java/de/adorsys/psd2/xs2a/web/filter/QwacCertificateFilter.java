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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.filter.holder.QwacCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class QwacCertificateFilter extends AbstractXs2aFilter {
    private final QwacCertificateService qwacCertificateService;
    private final RequestProviderService requestProviderService;

    public QwacCertificateFilter(TppErrorMessageWriter tppErrorMessageWriter,
                                 Xs2aEndpointChecker xs2aEndpointChecker,
                                 QwacCertificateService qwacCertificateService,
                                 RequestProviderService requestProviderService) {
        super(tppErrorMessageWriter, xs2aEndpointChecker);
        this.qwacCertificateService = qwacCertificateService;
        this.requestProviderService = requestProviderService;
    }

    @Override
    protected void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response,
                                          FilterChain chain) throws IOException, ServletException {
        String encodedTppQwacCert = requestProviderService.getEncodedTppQwacCert();
        if (StringUtils.isNotBlank(encodedTppQwacCert)
            && !qwacCertificateService.isApplicable(request, response, encodedTppQwacCert)) {
            return;
        }
        chain.doFilter(request, response);
    }
}
