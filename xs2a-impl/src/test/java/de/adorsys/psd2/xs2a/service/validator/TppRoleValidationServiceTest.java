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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class TppRoleValidationServiceTest {

    @InjectMocks
    private TppRoleValidationService tppRoleValidationService;

    @Test
    void shouldSuccess_when_correctRole() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/v1/accounts");

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.AISP), request)).isTrue();
    }

    @Test
    void shouldFail_when_wrongRole() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/v1/payments/sepa");

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.PIISP), request)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("params")
    void shouldSuccess_when_aspspRole(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.ASPSP), request)).isTrue();
    }

    private static Stream<Arguments> params() {
        return Stream.of(Arguments.arguments("GET", "/v1/consents"),
                         Arguments.arguments("POST", "/v1/payments/sepa"),
                         Arguments.arguments("GET", "/v1/accounts")
        );
    }

    private TppInfo buildTppInfo(TppRole tppRole) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setTppRoles(singletonList(tppRole));
        return tppInfo;
    }
}
