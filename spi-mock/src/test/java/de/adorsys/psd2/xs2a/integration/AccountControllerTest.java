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

package de.adorsys.psd2.xs2a.integration;


import de.adorsys.aspsp.xs2a.spi.ASPSPXs2aApplication;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.EventServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.service.AisConsentServiceRemote;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aUsageType;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test", "mockspi"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = ASPSPXs2aApplication.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
public class AccountControllerTest {
    private final static String ACCOUNT_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private final static String CONSENT_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String ACCESS_EXCEEDED_JSON_PATH = "/json/account/AccessExceededResponse.json";
    private final static UUID X_REQUEST_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private EventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceRemote aisConsentServiceRemote;
    @MockBean
    private Xs2aAisConsentMapper xs2aAisConsentMapper;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;
    @MockBean
    private AisConsentDataService aisConsentDataService;
    @MockBean
    private AccountSpi accountSpi;
    @MockBean
    private SpiToXs2aAccountDetailsMapper accountDetailsMapper;

    @Before
    public void init() {
        // common actions for all tests
        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.getTppInfo())
            .willReturn(TPP_INFO);
        given(tppService.getTppId())
            .willReturn(TPP_INFO.getAuthorisationNumber());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.buildTppUniqueParamsHolder()))
            .willReturn(false);
        given(eventServiceEncrypted.recordEvent(any(Event.class)))
            .willReturn(true);
        given(aisConsentServiceRemote.getAisAccountConsentById(CONSENT_ID)).willReturn(Optional.of(new AisAccountConsent()));
        given(consentRestTemplate.getForEntity(any(String.class), any(Class.class))).willReturn(ResponseEntity.ok(Void.class));

        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("tpp-qwac-certificate", "qwac certificate");
        httpHeaders.add("x-request-id", X_REQUEST_ID.toString());
        httpHeaders.add("consent-id", "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc");
        httpHeaders.add("PSU-ID", "PSU-123");
        httpHeaders.add("PSU-ID-Type", "Some type");
        httpHeaders.add("PSU-Corporate-ID", "Some corporate id");
        httpHeaders.add("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeaders.add("PSU-IP-Address", "1.1.1.1");
        httpHeaders.add("accept", "application/json, application/xml");
    }

    @Test
    public void getTransactions_ShouldFail_WithoutEndSlash() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionsUrlWithoutSlash(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void getTransactions_ShouldFail_WithEndSlash() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionsUrlWithSlash(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void getAccountList_ShouldFail_WithNoUsageCounter()  throws Exception  {
        // Given
        AccountConsent accountConsent = buildAccountConsent(0);
        given(xs2aAisConsentMapper.mapToAccountConsent(new AisAccountConsent())).willReturn(accountConsent);

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetAccountList());
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isTooManyRequests())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(ACCESS_EXCEEDED_JSON_PATH, UTF_8)));
    }

    @Test
    public void getAccountList_TwoRequestSuccessfulThirdRequestFailed()  throws Exception  {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetAccountList());
        requestBuilder.headers(httpHeaders);

        AspspConsentData aspspConsentData = new AspspConsentData(new byte[0], CONSENT_ID);
        SpiPsuData spiPsuData = new SpiPsuData(null, null, null, null);
        SpiContextData spiContextData = new SpiContextData(spiPsuData, TPP_INFO, X_REQUEST_ID);
        SpiResponse<List<SpiAccountDetails>> response = buildListSpiResponse(aspspConsentData);
        Xs2aAccountDetails accountDetails = buildXs2aAccountDetails();
        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();

        given(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID)).willReturn(aspspConsentData);
        given(accountSpi.requestAccountList(spiContextData, false, spiAccountConsent, aspspConsentData)).willReturn(response);
        given(accountDetailsMapper.mapToXs2aAccountDetailsList(anyListOf(SpiAccountDetails.class))).willReturn(Collections.singletonList(accountDetails));

        for (int usage = 2; usage >= 0; usage--) {
            AisAccountConsent aisAccountConsent = buildAisAccountConsent(usage);
            given(aisConsentServiceRemote.getAisAccountConsentById(CONSENT_ID)).willReturn(Optional.of(aisAccountConsent));
            AccountConsent accountConsent = buildAccountConsent(aisAccountConsent.getUsageCounter());
            given(xs2aAisConsentMapper.mapToAccountConsent(aisAccountConsent)).willReturn(accountConsent);
            given(xs2aAisConsentMapper.mapToSpiAccountConsent(accountConsent)).willReturn(spiAccountConsent);

            // When
            ResultActions resultActions = mockMvc.perform(requestBuilder);

            // Then
            if (usage > 0) {
                resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
            } else {
                resultActions.andExpect(status().isTooManyRequests())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(content().json(IOUtils.resourceToString(ACCESS_EXCEEDED_JSON_PATH, UTF_8)));
            }
        }
    }

    @NotNull
    private Xs2aAccountDetails buildXs2aAccountDetails() {
        return new Xs2aAccountDetails("accountDetail", "y1", "y2", "y3", "y4",
                                      "y5", "y6", Currency.getInstance("EUR"), "y8", "y9", CashAccountType.CACC,
                                      AccountStatus.ENABLED, "y11", "linked3", Xs2aUsageType.PRIV, "details3", new ArrayList<>());
    }

    private SpiResponse<List<SpiAccountDetails>> buildListSpiResponse(AspspConsentData aspspConsentData) {
        return (SpiResponse<List<SpiAccountDetails>>) SpiResponse.<List<SpiAccountDetails>>builder()
                                                                .payload(Collections.EMPTY_LIST)
                                                                .aspspConsentData(aspspConsentData)
                                                                .success();
    }

    private AisAccountConsent buildAisAccountConsent(int usageCounter) {
        AisAccountConsent aisAccountConsent = new AisAccountConsent();
        aisAccountConsent.setUsageCounter(usageCounter);
        return aisAccountConsent;
    }

    private AccountConsent buildAccountConsent(int usageCounter) {
        Xs2aAccountAccess xs2aAccountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);
        return new AccountConsent(null, xs2aAccountAccess, false, LocalDate.now().plusDays(1), 10,
                                  null, ConsentStatus.VALID, false, false,
                                  null, TPP_INFO, null, false, Collections.emptyList(), OffsetDateTime.now(), usageCounter);
    }
}

