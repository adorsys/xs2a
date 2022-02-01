/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */


package de.adorsys.psd2.xs2a.integration;


import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aUsageType;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test", "mock-qwac"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
class AccountControllerIT {
    private static final String ACCOUNT_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String CONSENT_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final HttpHeaders httpHeadersWithoutPsuIpAddress = new HttpHeaders();
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String ACCESS_EXCEEDED_JSON_PATH = "/json/account/res/AccessExceededResponse.json";
    private static final UUID X_REQUEST_ID = UUID.randomUUID();

    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private ConsentServiceEncrypted consentServiceEncrypted;
    @MockBean
    private Xs2aAisConsentMapper xs2aAisConsentMapper;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;
    @MockBean
    private AccountSpi accountSpi;
    @MockBean
    private SpiToXs2aAccountDetailsMapper accountDetailsMapper;
    @MockBean
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @MockBean
    private SpiAspspConsentDataProvider aspspConsentDataProvider;

    @BeforeEach
    void init() {
        // common actions for all tests
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(aspspProfileService.getScaApproaches(null))
            .willReturn(Collections.singletonList(ScaApproach.REDIRECT));
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(eventServiceEncrypted.recordEvent(any(EventBO.class)))
            .willReturn(true);
        given(consentServiceEncrypted.getConsentById(CONSENT_ID)).willReturn(CmsResponse.<CmsConsent>builder()
                                                                                 .payload(new CmsConsent())
                                                                                 .build());
        given(consentRestTemplate.postForEntity(anyString(), any(EventBO.class), eq(Boolean.class)))
            .willReturn(new ResponseEntity<>(true, HttpStatus.OK));
        given(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID)).willReturn(aspspConsentDataProvider);
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("x-request-id", X_REQUEST_ID.toString());
        httpHeaders.add("consent-id", CONSENT_ID);
        httpHeaders.add("PSU-ID", "PSU-123");
        httpHeaders.add("PSU-ID-Type", "Some type");
        httpHeaders.add("PSU-Corporate-ID", "Some corporate id");
        httpHeaders.add("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeaders.add("PSU-IP-Address", "1.1.1.1");
        httpHeaders.add("accept", "application/json, application/xml");

        httpHeadersWithoutPsuIpAddress.putAll(httpHeaders);
        httpHeadersWithoutPsuIpAddress.remove("PSU-IP-Address");
    }

    @Test
    void getTransactions_ShouldFail_WithoutEndSlash() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionsUrl(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void getTransactions_ShouldFail_WithEndSlash() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionsUrl(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void getAccountList_WithoutPsuIpAddressWithNoUsageCounter_ShouldFail() throws Exception {
        // Given
        AisConsent aisConsent = buildAccountConsent(Collections.singletonMap("/v1/accounts", 0));
        given(xs2aAisConsentMapper.mapToAisConsent(new CmsConsent())).willReturn(aisConsent);

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetAccountList());
        requestBuilder.headers(httpHeadersWithoutPsuIpAddress);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isTooManyRequests())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(ACCESS_EXCEEDED_JSON_PATH, UTF_8)));
    }

    @Test
    void getAccountList_WithPsuIpAddressWithNoUsageCounter_Success() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetAccountList());
        requestBuilder.headers(httpHeaders);

        SpiResponse<List<SpiAccountDetails>> response = buildListSpiResponse();
        Xs2aAccountDetails accountDetails = buildXs2aAccountDetails();
        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();

        given(accountSpi.requestAccountList(notNull(), eq(false), eq(spiAccountConsent), eq(aspspConsentDataProvider))).willReturn(response);
        given(accountDetailsMapper.mapToXs2aAccountDetailsList(anyList())).willReturn(Collections.singletonList(accountDetails));

        CmsConsent cmsConsent = buildAisAccountConsent(Collections.singletonMap("/v1/accounts", 0));
        given(consentServiceEncrypted.getConsentById(CONSENT_ID)).willReturn(CmsResponse.<CmsConsent>builder()
                                                                                 .payload(cmsConsent)
                                                                                 .build());
        given(aisConsentServiceEncrypted.updateAspspAccountAccess(eq(CONSENT_ID), any()))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());
        AisConsent aisConsent = createConsent();
        given(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).willReturn(spiAccountConsent);
        given(xs2aAisConsentMapper.mapToAisConsent(any(CmsConsent.class))).willReturn(aisConsent);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void getAccountList_WithPsuIpAddressWithNoUsageCounter_oneOffConsent_AccessExceeded() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetAccountList());
        requestBuilder.headers(httpHeaders);

        SpiResponse<List<SpiAccountDetails>> response = buildListSpiResponse();
        Xs2aAccountDetails accountDetails = buildXs2aAccountDetails();
        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();

        given(accountSpi.requestAccountList(notNull(), eq(false), eq(spiAccountConsent), eq(aspspConsentDataProvider))).willReturn(response);
        given(accountDetailsMapper.mapToXs2aAccountDetailsList(anyList())).willReturn(Collections.singletonList(accountDetails));

        CmsConsent cmsConsent = buildAisAccountConsent(Collections.singletonMap("/v1/accounts", 0));
        given(consentServiceEncrypted.getConsentById(CONSENT_ID)).willReturn(CmsResponse.<CmsConsent>builder()
                                                                                 .payload(cmsConsent)
                                                                                 .build());

        given(aisConsentServiceEncrypted.updateAspspAccountAccess(eq(CONSENT_ID), any()))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());

        AisConsent aisConsent = buildAccountConsent(Collections.singletonMap("/v1/accounts", 0));
        aisConsent.setRecurringIndicator(false);
        given(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).willReturn(spiAccountConsent);
        given(xs2aAisConsentMapper.mapToAisConsent(any(CmsConsent.class))).willReturn(aisConsent);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isTooManyRequests())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void getAccountList_TwoRequestSuccessfulThirdRequestFailed() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetAccountList());
        requestBuilder.headers(httpHeadersWithoutPsuIpAddress);

        SpiResponse<List<SpiAccountDetails>> response = buildListSpiResponse();
        Xs2aAccountDetails accountDetails = buildXs2aAccountDetails();
        SpiAccountConsent spiAccountConsent = new SpiAccountConsent();

        given(accountSpi.requestAccountList(notNull(), eq(false), eq(spiAccountConsent), eq(aspspConsentDataProvider))).willReturn(response);
        given(accountDetailsMapper.mapToXs2aAccountDetailsList(anyList())).willReturn(Collections.singletonList(accountDetails));

        for (int usage = 2; usage >= 0; usage--) {
            CmsConsent cmsConsent = buildAisAccountConsent(Collections.singletonMap("/v1/accounts", usage));
            given(consentServiceEncrypted.getConsentById(CONSENT_ID)).willReturn(CmsResponse.<CmsConsent>builder()
                                                                                     .payload(cmsConsent)
                                                                                     .build());
            given(aisConsentServiceEncrypted.updateAspspAccountAccess(eq(CONSENT_ID), any()))
                .willReturn(CmsResponse.<CmsConsent>builder()
                                .payload(cmsConsent)
                                .build());

            AisConsent aisConsent = buildAccountConsent(cmsConsent.getUsages());
            given(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).willReturn(spiAccountConsent);
            given(xs2aAisConsentMapper.mapToAisConsent(any(CmsConsent.class))).willReturn(aisConsent);

            // When
            ResultActions resultActions = mockMvc.perform(requestBuilder);

            // Then
            if (usage > 0) {
                resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
            } else {
                resultActions.andExpect(status().isTooManyRequests())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(content().json(IOUtils.resourceToString(ACCESS_EXCEEDED_JSON_PATH, UTF_8)));
            }
        }
    }

    @NotNull
    private Xs2aAccountDetails buildXs2aAccountDetails() {
        return new Xs2aAccountDetails("accountDetail", "y1", "y2", "y3", "y4",
                                      "y5", "y6", Currency.getInstance("EUR"), "y8", "y9", "product", CashAccountType.CACC,
                                      AccountStatus.ENABLED, "y11", "linked3", Xs2aUsageType.PRIV, "details3", new ArrayList<>(), null, null);
    }

    private SpiResponse<List<SpiAccountDetails>> buildListSpiResponse() {
        return SpiResponse.<List<SpiAccountDetails>>builder()
                   .payload(Collections.emptyList())
                   .build();
    }

    private CmsConsent buildAisAccountConsent(Map<String, Integer> usageCounter) {
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setUsages(usageCounter);
        return cmsConsent;
    }

    private AisConsent buildAccountConsent(Map<String, Integer> usageCounter) {
        AisConsent aisConsent = createConsent();
        aisConsent.setUsages(usageCounter);
        return aisConsent;
    }

    private AisConsent createConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/consent/xs2a-account-consent.json", AisConsent.class);
        aisConsent.setConsentTppInformation(buildConsentTppInformation());
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        return aisConsent;
    }

    private static ConsentTppInformation buildConsentTppInformation() {
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(TPP_INFO);
        return consentTppInformation;
    }
}
