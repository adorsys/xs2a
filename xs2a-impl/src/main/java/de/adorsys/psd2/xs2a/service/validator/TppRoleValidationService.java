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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.validator.certificate.util.TppRole;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class TppRoleValidationService {

	private Map<String, TppRole> patternRoleMap;
	private List<AntPathRequestMatcher> matchers;
	// NOPMD TODO API_BASE_PATH should not be define here,
	// https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/140
	private final static String API_BASE_PATH = "/api/v1";

	@PostConstruct
	void initCertificatePathMatchers() {
		patternRoleMap = new HashMap<>();
		patternRoleMap.put(API_BASE_PATH + "/accounts/**", TppRole.AISP);
		patternRoleMap.put(API_BASE_PATH + "/consents/**", TppRole.AISP);
		patternRoleMap.put(API_BASE_PATH + "/funds-confirmations/**", TppRole.PIISP);
		patternRoleMap.put(API_BASE_PATH + "/bulk-payments/**", TppRole.PISP);
		patternRoleMap.put(API_BASE_PATH + "/payments/**", TppRole.PISP);
		patternRoleMap.put(API_BASE_PATH + "/periodic-payments/**", TppRole.PISP);

		regexMatchers(patternRoleMap.keySet());
	}

	private void regexMatchers(Set<String> regexPatterns) {
		matchers = new ArrayList<AntPathRequestMatcher>();
		for (String pattern : regexPatterns) {
			matchers.add(new AntPathRequestMatcher(pattern));
		}
	}

	/**
	 * Check and validate if a request with tpp roles is allow to pass
	 * 
	 * @param request
	 * @param roles
	 * @return true or false
	 */
	public boolean validate(HttpServletRequest request, List<TppRole> roles) {

		for (AntPathRequestMatcher matcher : matchers) {
			if (matcher.matches(request)) {
				TppRole tppRole = patternRoleMap.get(matcher.getPattern());
				return Optional.ofNullable(roles).map(r -> r.contains(tppRole)).orElse(false);
			}
		}
		return true;
	}

}
