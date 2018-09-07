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

package de.adorsys.aspsp.xs2a.web.filter;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.web.util.SecurityUtil;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.Objects;


@Component
@Slf4j
public class ConsentAccessFilter extends GenericFilterBean {

    @Autowired
    ConsentService consentService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {


        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new ServletException("OncePerRequestFilter just supports HTTP requests");
        }

        String consent_id = ((HttpServletRequest) request).getHeader("consent-id");
        if (Objects.nonNull(consent_id)) {
            ResponseObject<AccountConsent> accountConsentResponse = consentService.getAccountConsentById(consent_id);
            if (!accountConsentResponse.hasError()){
                AccountConsent accountConsent = accountConsentResponse.getBody();
                TppCertificateData tppCertificateData = SecurityUtil.getTppCertificateData();
                if (Objects.nonNull(tppCertificateData)){
                    if (!Objects.equals(tppCertificateData.getPspAuthorityId(), accountConsent.getTppId())){
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

}
