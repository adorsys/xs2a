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


package de.adorsys.psd2.xs2a.integration.consent;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
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
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class DeleteConsentIT {
    private static final String ENCRYPTED_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

    private HttpHeaders httpHeaders = new HttpHeaders();

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
    private ConsentServiceEncrypted consentServiceEncrypted;
    @MockBean
    private PisAuthorisationConfirmationService pisAuthorisationConfirmationService;
    @MockBean
    private AisConsentSpi aisConsentSpi;

    @Autowired
    private ConsentDataMapper consentDataMapper;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void init() {
        httpHeaders.setAll(buildRequestHeaders());

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
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    void deleteConsent_successful() throws Exception {
        // Given
        doReturn(CmsResponse.<CmsConsent>builder()
                     .payload(buildCmsConsent(TPP_INFO))
                     .build())
            .when(consentServiceEncrypted).getConsentById(ENCRYPTED_CONSENT_ID);
        given(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());
        given(consentServiceEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, ConsentStatus.TERMINATED_BY_TPP))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildDeleteConsentUrl(ENCRYPTED_CONSENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    void deleteConsent_withWrongTpp_shouldReturnConsentInvalid() throws Exception {
        // Given
        String wrongTppId = "Wrong TPP ID";
        given(consentServiceEncrypted.getConsentById(ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(buildCmsConsent(TppInfoBuilder.buildTppInfo(wrongTppId)))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildDeleteConsentUrl(ENCRYPTED_CONSENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isForbidden())
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/res/wrong-tpp-response.json")));
    }

    @NotNull
    private HashMap<String, String> buildRequestHeaders() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        headerMap.put("PSU-IP-Address", "1.1.1.1");
        headerMap.put("TPP-Redirect-URI", "ok.uri");
        return headerMap;
    }

    private CmsConsent buildCmsConsent(TppInfo tppInfo) {
        AisConsentData aisConsentData = jsonReader.getObjectFromFile("json/consent/ais-consent-data.json", AisConsentData.class);
        CmsConsent consent = jsonReader.getObjectFromFile("json/consent/cms-consent-response.json", CmsConsent.class);
        ConsentTppInformation tppInformation = new ConsentTppInformation();
        tppInformation.setTppInfo(tppInfo);
        consent.setTppInformation(tppInformation);
        consent.setConsentData(consentDataMapper.getBytesFromConsentData(aisConsentData));
        return consent;
    }
}
