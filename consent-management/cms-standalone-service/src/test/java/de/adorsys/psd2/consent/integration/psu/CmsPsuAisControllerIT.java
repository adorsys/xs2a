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

package de.adorsys.psd2.consent.integration.psu;

import de.adorsys.psd2.consent.ConsentManagementStandaloneApp;
import de.adorsys.psd2.consent.config.WebConfig;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class CmsPsuAisControllerIT {

    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_ID = "bf489af6-a2cb-4b75-b71d-d66d58b934d7";
    private static final String REDIRECT_ID = "9d7effac-da7f-43c7-9fcc-d66166839c62";
    private static final ScaStatus STATUS = ScaStatus.RECEIVED;
    private static final String SMS = "SMS";
    private static final String TAN = "TAN";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorisationRepository authorisationRepository;
    @MockBean
    private ConsentJpaRepository consentJpaRepository;
    @MockBean
    private AisConsentVerifyingRepository aisConsentRepository;
    @MockBean
    private AisConsentUsageRepository aisConsentUsageRepository;

    @SpyBean
    private AuthorisationSpecification authorisationSpecification;
    @SpyBean
    private AisConsentSpecification aisConsentSpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private ConsentEntity consentEntity;
    private AuthorisationEntity authorisationEntity;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("psu-id", "PSU ID");
        httpHeaders.add("psu-id-type", "PSU ID TYPE");
        httpHeaders.add("psu-corporate-id", "PSU CORPORATE ID");
        httpHeaders.add("psu-corporate-id-type", "PSU CORPORATE ID TYPE");
        httpHeaders.add("PSU-IP-Address", "1.1.1.1");
        httpHeaders.add("instance-id", INSTANCE_ID);

        authorisationEntity = jsonReader.getObjectFromFile("json/consent/integration/psu/authorisation-entity.json", AuthorisationEntity.class);
        authorisationEntity.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));
        authorisationEntity.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plusDays(1));

        consentEntity = jsonReader.getObjectFromFile("json/consent/integration/psu/consent-entity.json", ConsentEntity.class);
        consentEntity.setData(jsonReader.getBytesFromFile("json/consent/integration/psu/ais-consent-data.json"));
    }

    @Test
    void updatePsuDataInConsent() throws Exception {
        given(authorisationRepository.findOne(any())).willReturn(Optional.of(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updatePsuDataInConsentUrl(CONSENT_ID, AUTHORISATION_ID))
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/psu/psu-id-data.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(authorisationSpecification).byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsent_badRequest() throws Exception {
        given(authorisationRepository.findOne(any())).willReturn(Optional.of(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updatePsuDataInConsentUrl(CONSENT_ID, AUTHORISATION_ID))
                                                           .content("{}");
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().string(""));

        verify(authorisationSpecification).byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(authorisationRepository.findOne(any())).willReturn(Optional.of(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updateAuthorisationStatusUrl(CONSENT_ID, AUTHORISATION_ID, STATUS.name()))
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/psu/authorisation-holder.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
        verify(authorisationSpecification).byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(SMS, authorisationEntity.getAuthenticationMethodId());
        assertEquals(TAN, authorisationEntity.getScaAuthenticationData());
    }

    @Test
    void confirmConsent() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(consentJpaRepository.findByExternalId(CONSENT_ID)).willReturn(Optional.of(consentEntity));
        given(aisConsentRepository.verifyAndSave(consentEntity)).willReturn(consentEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.confirmConsentUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void rejectConsent() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(aisConsentRepository.verifyAndSave(consentEntity)).willReturn(consentEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.rejectConsentUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
        assertEquals(ConsentStatus.REJECTED, consentEntity.getConsentStatus());
    }

    @Test
    void getConsentsForPsu() throws Exception {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/consent/integration/psu/psu-id-data.json", PsuIdData.class);

        given(consentJpaRepository.findAll(any(), eq(Pageable.unpaged()))).willReturn(new PageImpl<>(Collections.singletonList(consentEntity)));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getConsentsForPsuUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-ais-account-consent.json")));

        verify(aisConsentSpecification).byPsuDataInListAndInstanceIdAndAdditionalTppInfo(psuIdData, INSTANCE_ID, null, Collections.emptyList(), null);
    }

    @Test
    void revokeConsent() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(aisConsentRepository.verifyAndSave(consentEntity)).willReturn(consentEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.revokeConsentUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
        assertEquals(ConsentStatus.REVOKED_BY_PSU, consentEntity.getConsentStatus());
    }

    @Test
    void authorisePartiallyConsent() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(aisConsentRepository.verifyAndSave(consentEntity)).willReturn(consentEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.authorisePartiallyConsentUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("true"));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
        assertEquals(ConsentStatus.PARTIALLY_AUTHORISED, consentEntity.getConsentStatus());
    }

    @Test
    void getConsentIdByRedirectId() throws Exception {
        given(authorisationRepository.findOne(any())).willReturn(Optional.of(authorisationEntity));
        given(consentJpaRepository.findByExternalId(authorisationEntity.getParentExternalId())).willReturn(Optional.of(consentEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getConsentIdByRedirectIdUrl(REDIRECT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-ais-consent-response.json")));

        verify(authorisationSpecification).byExternalIdAndInstanceId(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getConsentByConsentId() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getConsentByConsentIdUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-ais-consent-response2.json")));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId() throws Exception {
        given(authorisationRepository.findOne(any())).willReturn(Optional.of(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getAuthorisationByAuthorisationIdUrl(AUTHORISATION_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        CmsPsuAuthorisation cmsPsuAuthorisation = jsonReader.getObjectFromFile("json/consent/integration/psu/expect/cms-psu-authorisation.json", CmsPsuAuthorisation.class);
        cmsPsuAuthorisation.setAuthorisationExpirationTimestamp(authorisationEntity.getAuthorisationExpirationTimestamp());
        cmsPsuAuthorisation.setRedirectUrlExpirationTimestamp(authorisationEntity.getRedirectUrlExpirationTimestamp());
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.writeValueAsString(cmsPsuAuthorisation)));


        verify(authorisationSpecification).byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void putAccountAccessInConsent() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(aisConsentRepository.verifyAndSave(consentEntity)).willReturn(consentEntity);

        AisConsentUsage aisConsentUsage = new AisConsentUsage();
        aisConsentUsage.setUsage(10);
        given(aisConsentUsageRepository.findReadByConsentAndUsageDate(any(ConsentEntity.class), any(LocalDate.class)))
            .willReturn(Collections.singletonList(aisConsentUsage));

        given(aisConsentRepository.verifyAndSave(consentEntity)).willReturn(consentEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.putAccountAccessInConsentUrl(CONSENT_ID))
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/psu/account-access-request.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
        verify(aisConsentUsageRepository).saveAll(Collections.singletonList(aisConsentUsage));
        assertEquals(0, aisConsentUsage.getUsage());
    }

    @Test
    void psuDataAuthorisations() throws Exception {
        given(consentJpaRepository.findOne(any())).willReturn(Optional.of(consentEntity));
        given(authorisationRepository.findAllByParentExternalIdAndType(consentEntity.getExternalId(), AuthorisationType.CONSENT))
            .willReturn(Collections.singletonList(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.psuDataAuthorisationsUrl(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-ais-psu-data-authorisation.json")));

        verify(aisConsentSpecification).byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID);
    }
}
