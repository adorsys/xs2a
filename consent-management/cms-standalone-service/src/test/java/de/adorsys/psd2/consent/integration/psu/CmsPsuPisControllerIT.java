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
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.consent.repository.specification.PisPaymentDataSpecification;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
class CmsPsuPisControllerIT {
    private static final String AUTHORISATION_ID = "9d7effac-da7f-43c7-9fcc-d66166839c62";
    private static final String REDIRECT_ID = "9d7effac-da7f-43c7-9fcc-d66166839c62";
    private static final String PAYMENT_ID = "cea9dda3-5154-420d-b1a7-6b4798fccb4b";
    private static final ScaStatus STATUS = ScaStatus.RECEIVED;
    private static final TransactionStatus PAYMENT_STATUS = TransactionStatus.RCVD;
    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String SMS = "SMS";
    private static final String TAN = "TAN";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorisationRepository authorisationRepository;
    @MockBean
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @MockBean
    private PisPaymentDataRepository pisPaymentDataRepository;

    @SpyBean
    private AuthorisationSpecification authorisationSpecification;
    @SpyBean
    private PisPaymentDataSpecification pisPaymentDataSpecification;
    @SpyBean
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private AuthorisationEntity authorisationEntity;
    private PisCommonPaymentData pisCommonPaymentData;

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
        authorisationEntity.setParentExternalId(PAYMENT_ID);

        pisCommonPaymentData = jsonReader.getObjectFromFile("json/consent/integration/psu/common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentData));
        pisCommonPaymentData.setPayment(jsonReader.getBytesFromFile("json/consent/integration/psu/payment-initiation-resp.json"));
    }

    @Test
    void updatePsuInPayment() throws Exception {
        given(authorisationRepository.findOne(any(Specification.class))).willReturn(Optional.of(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updatePsuInPaymentUrl(AUTHORISATION_ID))
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/psu/psu-id-data.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(authorisationSpecification).byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentIdByRedirectId() throws Exception {
        given(authorisationRepository.findOne(any(Specification.class))).willReturn(Optional.of(authorisationEntity));
        given(pisCommonPaymentDataRepository.findByPaymentId(authorisationEntity.getParentExternalId()))
            .willReturn(Optional.of(pisCommonPaymentData));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPaymentIdByRedirectIdUrl(REDIRECT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-payment-response.json")));

        verify(authorisationSpecification).byExternalIdAndInstanceId(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentByPaymentId() throws Exception {
        given(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID)).willReturn(Optional.of(Collections.emptyList()));
        given(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).willReturn(Optional.of(pisCommonPaymentData));

        given(pisPaymentDataRepository.findAll(any(Specification.class))).willReturn(Collections.emptyList());
        given(pisCommonPaymentDataRepository.findOne(any(Specification.class))).willReturn(Optional.of(pisCommonPaymentData));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPaymentByPaymentIdUrl(PAYMENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-payment.json")));

        verify(pisPaymentDataSpecification).byPaymentIdAndInstanceId(PAYMENT_ID, INSTANCE_ID);
        verify(pisCommonPaymentDataSpecification).byPaymentIdAndInstanceId(PAYMENT_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentIdByRedirectIdForCancellation() throws Exception {
        given(authorisationRepository.findOne(any(Specification.class))).willReturn(Optional.of(authorisationEntity));
        given(pisCommonPaymentDataRepository.findByPaymentId(authorisationEntity.getParentExternalId()))
            .willReturn(Optional.of(pisCommonPaymentData));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPaymentIdByRedirectIdForCancellationUrl(REDIRECT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-payment-response.json")));

        verify(authorisationSpecification).byExternalIdAndInstanceId(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId() throws Exception {
        given(authorisationRepository.findOne(any(Specification.class))).willReturn(Optional.of(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getPisAuthorisationByAuthorisationIdUrl(AUTHORISATION_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        CmsPsuAuthorisation cmsPsuAuthorisation = jsonReader.getObjectFromFile("json/consent/integration/psu/expect/cms-psu-authorisation.json", CmsPsuAuthorisation.class);
        cmsPsuAuthorisation.setAuthorisationExpirationTimestamp(authorisationEntity.getAuthorisationExpirationTimestamp());
        cmsPsuAuthorisation.setRedirectUrlExpirationTimestamp(authorisationEntity.getRedirectUrlExpirationTimestamp());
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.writeValueAsString(cmsPsuAuthorisation)));

        verify(authorisationSpecification).byExternalIdAndInstanceId(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus() throws Exception {
        given(authorisationRepository.findOne(any(Specification.class))).willReturn(Optional.of(authorisationEntity));
        given(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).willReturn(Optional.of(pisCommonPaymentData));

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updatePisAuthorisationStatusUrl(PAYMENT_ID, AUTHORISATION_ID, STATUS))
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/psu/authorisation-holder.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(authorisationSpecification).byExternalIdAndInstanceId(REDIRECT_ID, INSTANCE_ID);

        assertEquals(SMS, authorisationEntity.getAuthenticationMethodId());
        assertEquals(TAN, authorisationEntity.getScaAuthenticationData());
    }

    @Test
    void updatePaymentStatus() throws Exception {
        given(pisCommonPaymentDataRepository.findOne(any(Specification.class))).willReturn(Optional.of(pisCommonPaymentData));
        given(pisCommonPaymentDataRepository.save(pisCommonPaymentData)).willReturn(pisCommonPaymentData);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.updatePsuPaymentStatusUrl(PAYMENT_ID, PAYMENT_STATUS.name()));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(pisCommonPaymentDataSpecification).byPaymentIdAndInstanceId(PAYMENT_ID, INSTANCE_ID);

        assertEquals(PAYMENT_STATUS, pisCommonPaymentData.getTransactionStatus());
    }

    @Test
    void psuAuthorisationStatuses() throws Exception {
        given(pisCommonPaymentDataRepository.findOne(any(Specification.class))).willReturn(Optional.of(pisCommonPaymentData));
        given(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .willReturn(Collections.singletonList(authorisationEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.psuAuthorisationStatusesUrl(PAYMENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-pis-psu-data-authorisation.json")));

        verify(pisCommonPaymentDataSpecification).byPaymentIdAndInstanceId(PAYMENT_ID, INSTANCE_ID);
    }
}
