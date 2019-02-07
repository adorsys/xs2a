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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScaApproachResolverTest {
    @InjectMocks
    private ScaApproachResolver scaApproachResolver;

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;

    @Before
    public void setUp() {
        when(aspspProfileService.getScaApproaches())
            .thenReturn(buildScaApproaches(EMBEDDED, REDIRECT));
    }

    @Test
    public void shouldReturn_Redirect() {
        //When
        when(requestProviderService.resolveTppRedirectPreferred())
            .thenReturn(true);

        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void shouldReturn_Embedded() {
        //When
        when(requestProviderService.resolveTppRedirectPreferred())
            .thenReturn(false);

        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    private List<ScaApproach> buildScaApproaches(ScaApproach... scaApproaches) {
        return Arrays.asList(scaApproaches);
    }
}
