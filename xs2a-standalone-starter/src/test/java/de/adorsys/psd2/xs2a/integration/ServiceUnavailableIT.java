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


import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.stub.impl.AisConsentSpiMockImpl;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.exception.GlobalExceptionHandlerController;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.UpdateConsentPsuDataValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.web.controller.ConsentController;
import de.adorsys.psd2.xs2a.web.filter.OauthModeFilter;
import de.adorsys.psd2.xs2a.web.filter.QwacCertificateFilter;
import de.adorsys.psd2.xs2a.web.filter.SignatureFilter;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class,
    AisConsentSpiMockImpl.class
})
@TestPropertySource(locations = "/qwac.properties")
public class ServiceUnavailableIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String AUTH_REQ = "/json/payment/req/auth_request.json";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String DEDICATED_CONSENT_REQUEST_JSON_PATH = "/json/account/req/DedicatedConsent.json";
    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String SERVICE_UNAVAILABLE_ERROR_MESSAGE_JSON_PATH = "/json/account/res/ServiceUnavailableErrorMessage.json";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private HttpHeaders httpHeadersImplicit = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;
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
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Autowired
    private OauthModeFilter oauthModeFilter;
    @Autowired
    private QwacCertificateFilter qwacCertificateFilter;
    @Autowired
    private SignatureFilter signatureFilter;
    @Autowired
    private ConsentController consentController;
    @Autowired
    private GlobalExceptionHandlerController globalExceptionHandlerController;
    @MockBean
    private UpdateConsentPsuDataValidator updateConsentPsuDataValidator;
    @MockBean
    private AisConsentSpiMockImpl aisConsentSpiMock;
    @Value("${qwac-certificate-mock}")
    private String qwacCertificateMock;
    private Supplier<ResourceAccessException> resourceAccessExceptionSupplier = () -> new ResourceAccessException("");

    @Rule
    public TestWatcher watcher = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            try {
                onStartingTest(description);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface ResourceAvailable {
        boolean profile() default true;
        boolean cms() default true;
        boolean connector() default true;
    }

    @Before
    public void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept", "application/json");
        headerMap.put("tpp-qwac-certificate", qwacCertificateMock);
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        headerMap.put("PSU-IP-Address", "1.1.1.1");
        headerMap.put("TPP-Redirect-URI", "ok.uri");
        headerMap.put("TPP-Implicit-Authorisation-Preferred", "false");

        httpHeadersImplicit.setAll(headerMap);
    }

    private void onStartingTest(Description description) throws Exception {
        ResourceAvailable resourceAvailable = description.getAnnotation(ResourceAvailable.class);
        makePreparationsCommon();
        if (resourceAvailable == null) {
            makePreparationsCms(false);
            makePreparationsProfile(false);
            makePreparationsConnector(false);
        } else {
            makePreparationsCms(!resourceAvailable.cms());
            makePreparationsProfile(!resourceAvailable.profile());
            makePreparationsConnector(!resourceAvailable.connector());
        }
    }

    @Test
    @ResourceAvailable(profile = false)
    public void aspsp_profile_not_accessible_in_qwac_filter() throws Exception {
        MockMvc mockMvc = buildMockMvcWithFilters(qwacCertificateFilter);
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(profile = false)
    public void aspsp_profile_not_accessible_in_oauth_filter() throws Exception {
        MockMvc mockMvc = buildMockMvcWithFilters(oauthModeFilter);
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(profile = false)
    public void aspsp_profile_not_accessible_in_signature_filter() throws Exception {
        MockMvc mockMvc = buildMockMvcWithFilters(signatureFilter);
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(profile = false)
    public void aspsp_profile_not_accessible_in_interceptor() throws Exception {
        MockMvc mockMvc = buildMockMvcWithoutFilters();
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(profile = false)
    public void aspsp_profile_not_accessible_in_application() throws Exception {
        MockMvc mockMvc = buildMockMvcNoFiltersNoInterceptors();
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(cms = false)
    public void cms_not_accessible_in_interceptor() throws Exception {
        MockMvc mockMvc = buildMockMvcWithoutFilters();
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(cms = false)
    public void cms_not_accessible_in_application() throws Exception {
        MockMvc mockMvc = buildMockMvcNoFiltersNoInterceptors();
        create_consent_service_unavailable_test(mockMvc);
    }

    @Test
    @ResourceAvailable(connector = false)
    public void connector_not_accessible_in_application() throws Exception {
        update_consent_service_unavailable_test(mockMvc);
    }

    private void create_consent_service_unavailable_test(MockMvc mockMvc) throws Exception {
        service_unavailable_test(mockMvc, DEDICATED_CONSENT_REQUEST_JSON_PATH, post(UrlBuilder.buildConsentCreation()));
    }

    private void update_consent_service_unavailable_test(MockMvc mockMvc) throws Exception {
        service_unavailable_test(mockMvc, AUTH_REQ, put(UrlBuilder.buildConsentUpdateAuthorisationUrl(CONSENT_ID, AUTHORISATION_ID)));
    }

    private void service_unavailable_test(MockMvc mockMvc, String contentFilePath, MockHttpServletRequestBuilder requestBuilder) throws Exception {
        //Given
        requestBuilder.headers(httpHeadersImplicit);
        requestBuilder.content(resourceToString(contentFilePath, UTF_8));
        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);
        //Then
        resultActions.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(SERVICE_UNAVAILABLE_ERROR_MESSAGE_JSON_PATH, UTF_8)));
    }

    //MockMvc builders
    private MockMvc buildMockMvcWithFilters(Filter... filters) {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                   .addFilters(filters)
                   .build();
    }

    private MockMvc buildMockMvcWithoutFilters() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                   .build();
    }

    private MockMvc buildMockMvcNoFiltersNoInterceptors() {
        return MockMvcBuilders.standaloneSetup(consentController)
                   .setControllerAdvice(globalExceptionHandlerController)
                   .build();
    }

    //Preparations
    private void makePreparationsCms(boolean throwException) throws Exception {
        givenReturnOrThrowException(
            aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(any(String.class), any(AisConsentAuthorizationRequest.class)),
            buildCmsResponse(buildCmsResponse(buildCreateAisConsentAuthorizationResponse())),
            throwException);
        AisAccountConsent aisAccountConsent = AisConsentBuilder.buildAisAccountConsent(DEDICATED_CONSENT_REQUEST_JSON_PATH, ScaApproach.EMBEDDED, ENCRYPT_CONSENT_ID, xs2aObjectMapper);
        givenReturnOrThrowException(
            aisConsentServiceEncrypted.createConsent(any(CreateAisConsentRequest.class)),
            buildCmsResponse(new CreateAisConsentResponse(ENCRYPT_CONSENT_ID, aisAccountConsent, Arrays.asList(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA))),
            throwException);
        givenReturnOrThrowException(
            aisConsentServiceEncrypted.updateAspspAccountAccessWithResponse(eq(ENCRYPT_CONSENT_ID), any(AisAccountAccessInfo.class)),
            buildCmsResponse(aisAccountConsent),
            throwException);
        givenReturnOrThrowException(
            aisConsentServiceEncrypted.getAisAccountConsentById(any(String.class)),
            buildCmsResponse(aisAccountConsent),
            throwException);
        givenReturnOrThrowException(
            aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(any(String.class), any(String.class)),
            buildCmsResponse(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(ScaApproach.EMBEDDED)),
            throwException);
        givenReturnOrThrowException(
            aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID),
            buildCmsResponse(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)),
            throwException);
        givenReturnOrThrowException(
            tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()),
            buildCmsResponse(false),
            throwException);
        givenReturnOrThrowException(
            eventServiceEncrypted.recordEvent(any(EventBO.class)),
            true,
            throwException);
        givenReturnOrThrowException(
            aspspDataService.readAspspConsentData(any(String.class)),
            Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)),
            throwException);
        givenReturnOrThrowException(
            aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(any(String.class), any(AisConsentAuthorizationRequest.class)),
            buildCmsResponse(buildCreateAisConsentAuthorizationResponse()),
            throwException);
    }

    private void makePreparationsProfile(boolean throwException) {
        ScaApproach scaApproach = ScaApproach.EMBEDDED;
        givenReturnOrThrowException(aspspProfileService.getAspspSettings(), AspspSettingsBuilder.buildAspspSettings(), throwException);
        givenReturnOrThrowException(aspspProfileService.getScaApproaches(), Collections.singletonList(scaApproach), throwException);
        givenReturnOrThrowException(aspspProfileServiceWrapper.isPsuInInitialRequestMandated(), false, throwException);
        givenReturnOrThrowException(aspspProfileServiceWrapper.getTppSignatureRequired(), false, throwException);
        givenReturnOrThrowException(aspspProfileServiceWrapper.isCheckTppRolesFromCertificateSupported(), false, throwException);
    }

    private void makePreparationsConnector(boolean throwException) {
        givenReturnOrThrowException(
            aisConsentSpiMock.authorisePsu(any(SpiContextData.class), any(SpiPsuData.class), anyString(), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)),
            SpiResponse.builder().payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS)).build(),
            throwException
        );
    }

    private void makePreparationsCommon() {
        given(tppService.getTppInfo()).willReturn(TPP_INFO);
        given(tppService.getTppId()).willReturn(TPP_INFO.getAuthorisationNumber());
        given(updateConsentPsuDataValidator.validate(any(UpdateConsentPsuDataRequestObject.class))).willReturn(ValidationResult.valid());
    }

    private <T> void givenReturnOrThrowException(T methodCall, T returnValue, boolean throwException) {
        if (throwException) {
            given(methodCall).willThrow(resourceAccessExceptionSupplier.get());
        } else {
            given(methodCall).willReturn(returnValue);
        }
    }

    private CreateAisConsentAuthorizationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAisConsentAuthorizationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, null);
    }

    private <T> CmsResponse<T> buildCmsResponse(T payload) {
        return CmsResponse.<T>builder()
                   .payload(payload)
                   .build();
    }
}
