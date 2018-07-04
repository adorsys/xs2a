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

import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.psd2.validator.certificate.CertificateValidatorFactory;
import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.CertificateException;

@Slf4j
@Component
@Order(1)
public class CertificateFilter implements Filter {

	private SimpleCertificateBucket blockedCertBucket;
	private SimpleCertificateBucket rootCertBucket;
	private SimpleCertificateBucket intermediateCertBucket;

	@Autowired
	private AspspProfileService aspspProfileService;

	@Override
	public void init(FilterConfig filterConfig) {

		blockedCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("blockedcert"));
		rootCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("rootcert", "MyRootCA.pem"));
		intermediateCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("intermediatecert"));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (aspspProfileService.getTppSignatureRequired()) {
			if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
				throw new ServletException("OncePerRequestFilter just supports HTTP requests");
			}

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String encodedTppCert = httpRequest.getHeader("tpp-certificate");

			CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
					rootCertBucket, intermediateCertBucket);
			try {
				validatorFactory.validate(encodedTppCert);

				TppCertificateData tppCertData = CertificateExtractorUtil.extract(encodedTppCert);
				request.setAttribute("tppCertData", tppCertData);

				chain.doFilter(request, response);
			} catch (CertificateException | CertificateValidationException e) {
				log.debug(e.getMessage());
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

}
