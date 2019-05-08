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


package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.domain.ScaApproachHolder;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScaApproachResolverTest {
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @InjectMocks
    private ScaApproachResolver scaApproachResolver;

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private ScaApproachHolder scaApproachHolder;
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private Xs2aAisConsentService xs2aAisConsentService;

    @Before
    public void setUp() {
        when(aspspProfileService.getScaApproaches())
            .thenReturn(buildScaApproaches(EMBEDDED, REDIRECT));
    }

    @Test
    public void resolveScaApproach_shouldReturn_Redirect() {
        //Given
        when(requestProviderService.resolveTppRedirectPreferred())
            .thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_shouldReturn_Embedded() {
        //Given
        when(requestProviderService.resolveTppRedirectPreferred())
            .thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredAbsent_Redirect_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.empty());

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_Redirect_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_Redirect_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredAbsent_Embedded_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.empty());

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_Embedded_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_Embedded_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_Decoupled_shouldReturn_Decoupled() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(DECOUPLED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_Decoupled_shouldReturn_Decoupled() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(DECOUPLED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_EmbeddedDecoupledRedirect_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED, DECOUPLED, REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_RedirectEmbeddedDecoupled_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT, EMBEDDED, DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_DecoupledEmbeddedRedirect_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(DECOUPLED, EMBEDDED, REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_EmbeddedDecoupled_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED, DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredTrue_DecoupledEmbedded_shouldReturn_Decoupled() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(DECOUPLED, EMBEDDED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(DECOUPLED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredAbsent_RedirectEmbeddedDecoupled_shouldReturn_Redirect() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT, EMBEDDED, DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.empty());

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(REDIRECT);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredAbsent_EmbeddedDecoupledRedirect_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED, DECOUPLED, REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.empty());

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredAbsent_DecoupledEmbeddedRedirect_shouldReturn_Decoupled() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(DECOUPLED, EMBEDDED, REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.empty());

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(DECOUPLED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_DecoupledEmbeddedRedirect_shouldReturn_Decoupled() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(DECOUPLED, EMBEDDED, REDIRECT));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(DECOUPLED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_EmbeddedRedirectDecoupled_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(EMBEDDED, REDIRECT, DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_RedirectEmbeddedDecoupled_shouldReturn_Embedded() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT, EMBEDDED, DECOUPLED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(EMBEDDED);
    }

    @Test
    public void resolveScaApproach_TppRedirectPreferredFalse_RedirectDecoupledEmbedded_shouldReturn_Decoupled() {
        //Given
        when(aspspProfileService.getScaApproaches()).thenReturn(buildScaApproaches(REDIRECT, DECOUPLED, EMBEDDED));
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(false));

        //When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        //Then
        assertThat(actualResult).isEqualTo(DECOUPLED);
    }

    @Test
    public void resolveScaApproach_withForcedApproach_shouldReturnForced() {
        // Given
        when(scaApproachHolder.getScaApproach()).thenReturn(DECOUPLED);
        when(scaApproachHolder.isNotEmpty()).thenReturn(true);

        // When
        ScaApproach actualResult = scaApproachResolver.resolveScaApproach();

        // Then
        assertThat(actualResult).isEqualTo(DECOUPLED);

    }

    @Test
    public void forceDecoupledScaApproach_shouldSetDecoupled() {
        // Given
        ArgumentCaptor<ScaApproach> scaApproachArgumentCaptor = ArgumentCaptor.forClass(ScaApproach.class);

        // When
        scaApproachResolver.forceDecoupledScaApproach();

        // Then
        verify(scaApproachHolder, times(1)).setScaApproach(scaApproachArgumentCaptor.capture());
        assertThat(scaApproachArgumentCaptor.getValue()).isEqualTo(DECOUPLED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveScaApproach_scaApproachResponseIsEmpty() {
        scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID);
    }

    @Test
    public void getCancellationScaApproach() {
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(ServiceType.PIS);
        when(pisAuthorisationService.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CANCELLATION))
            .thenReturn(Optional.of(new AuthorisationScaApproachResponse(REDIRECT)));

        ScaApproach scaApproach = scaApproachResolver.getCancellationScaApproach(AUTHORISATION_ID);

        assertThat(scaApproach).isEqualTo(REDIRECT);
        verify(pisAuthorisationService, times(1))
            .getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CANCELLATION));
    }

    @Test
    public void getInitiationScaApproach() {
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(ServiceType.AIS);
        when(xs2aAisConsentService.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(Optional.of(new AuthorisationScaApproachResponse(REDIRECT)));

        ScaApproach scaApproach = scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID);

        assertThat(scaApproach).isEqualTo(REDIRECT);
        verify(xs2aAisConsentService, times(1))
            .getAuthorisationScaApproach(eq(AUTHORISATION_ID));
    }

    private List<ScaApproach> buildScaApproaches(ScaApproach... scaApproaches) {
        return Arrays.asList(scaApproaches);
    }
}
