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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import de.adorsys.psd2.validator.certificate.util.CertificateExtractorUtil;
import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;

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
public class QwacCertificateFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("OncePerRequestFilter just supports HTTP requests");
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (Objects.isNull(authentication)) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String encodedTppQwacCert = getEncodedTppQwacCert(httpRequest);

			if (StringUtils.isNotBlank(encodedTppQwacCert)) {

				try {
					TppCertificateData tppCertificateData = CertificateExtractorUtil.extract(encodedTppQwacCert);
					HashMap<String, String> credential = new HashMap<>();
					credential.put("authorityCountry", tppCertificateData.getPspAuthorityCountry());
					credential.put("authorityId", tppCertificateData.getPspAuthorityId());
					credential.put("authorityName", tppCertificateData.getPspAuthorityName());
					credential.put("authorizationNumber", tppCertificateData.getPspAuthorizationNumber());
					credential.put("name", tppCertificateData.getPspName());

					List<GrantedAuthority> authorities = tppCertificateData.getPspRoles().stream()
							.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
							.collect(Collectors.toList());

					authentication = new UsernamePasswordAuthenticationToken(
							tppCertificateData.getPspAuthorizationNumber(), credential, authorities);

					SecurityContextHolder.getContext().setAuthentication(authentication);

				} catch (CertificateValidationException e) {
					log.debug(e.getMessage());
					((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
					return;
				}

			}
		}

		chain.doFilter(request, response);

	}

	@Override
	public void destroy() {
	}

	public String getEncodedTppQwacCert(HttpServletRequest httpRequest) {
		return httpRequest.getHeader("tpp-qwac-certificate");
	}

}
