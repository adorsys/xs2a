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

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class TppRoleValidationServiceTest {

    @InjectMocks
    private TppRoleValidationService tppRoleValidationService;


    @Test
    public void shouldSuccess_when_correctRole() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/v1/accounts");

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.AISP), request)).isTrue();
    }

    @Test
    public void shouldFail_when_wrongRole() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/v1/payments/sepa");

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.PIISP), request)).isFalse();
    }

    private TppInfo buildTppInfo(TppRole tppRole) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setTppRoles(singletonList(tppRole));
        return tppInfo;
    }
}
