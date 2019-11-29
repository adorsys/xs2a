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

package de.adorsys.psd2.xs2a.web.mapper;


import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentModelMapperTest {

    private final static String CONSENT_STATUS = "received";
    private final static String CONSENT_ID = "S7tlYXaar8j7l5IMK89iNJB8SkG5ricoOaEYHyku_AO9BF6MIP29SN_tXtDvaQb3c8b_NsohCWlFlYN0ds8u89WFnjze07vwpAgFM45MlQk=_=_psGLvQpt9Q";
    private final static String AUTHORISATION_ID = "authorisation ID";
    private final static String PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";
    private final static String SELF_LINK = "self";
    private final static String LOCALHOST_LINK = "http://localhost";
    private final static String PAYMENT_PRODUCT = "sepa-credit-transfers";

    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String ACCOUNT_ID = "accountId";
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private ConsentModelMapper consentModelMapper;

    @Mock
    private HrefLinkMapper hrefLinkMapper;

    @Mock
    private ScaMethodsMapper scaMethodsMapper;

    @Mock
    private AccountModelMapper accountModelMapper;

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    private CreateConsentResponse createConsentResponseWithScaMethods;
    private CreateConsentResponse createConsentResponseWithoutScaMethods;
    private JsonReader jsonReader;
    private PsuIdData psuIdData;
    private Map bodyMap;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();
        createConsentResponseWithScaMethods = jsonReader.getObjectFromFile("json/service/mapper/create-consent-response-with-sca-methods.json", CreateConsentResponse.class);
        createConsentResponseWithoutScaMethods = jsonReader.getObjectFromFile("json/service/mapper/create-consent-response-without-sca-methods.json", CreateConsentResponse.class);
        psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        bodyMap = getBodyMap();
    }

    @Test
    public void mapToConsentsResponse201_withScaMethods_shouldReturnArrayOfThem() {
        // Given
        ScaMethods methods = new ScaMethods();
        methods.add(new AuthenticationObject());
        when(scaMethodsMapper.mapToScaMethods(anyList())).thenReturn(methods);
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());

        // When
        ConsentsResponse201 actual = consentModelMapper.mapToConsentsResponse201(createConsentResponseWithScaMethods);

        // Then
        checkCommonFields(actual);
        assertFalse(actual.getScaMethods().isEmpty());
    }

    @Test
    public void mapToConsentsResponse201_withoutScaMethods_shouldNotReturnEmptyArray() {
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());

        // When
        ConsentsResponse201 actual = consentModelMapper.mapToConsentsResponse201(createConsentResponseWithoutScaMethods);

        // Then
        checkCommonFields(actual);
        assertNull(actual.getScaMethods());
    }

    @Test
    public void mapToCancellations() {
        //Given
        List<String> cancellationIds = Arrays.asList("c0121ca2-ab3a-4564-b915-6e40e8b40f50", "743d0a45-7233-4fbf-9799-c657f327836c");
        //When
        Cancellations cancellations = consentModelMapper.mapToCancellations(new Xs2aPaymentCancellationAuthorisationSubResource(cancellationIds));
        //Then
        assertNotNull(cancellations);
        CancellationList cancellationList = cancellations.getCancellationIds();
        assertNotNull(cancellationList);
        assertEquals(cancellationIds, new ArrayList<>(cancellationList));
    }

    @Test
    public void mapToCreateConsentReq_AvailableAccountsWithBalance() {
        //Given
        when(xs2aObjectMapper.convertValue(buildAccountReferenceWithoutIds(), AccountReference.class)).thenReturn(buildXs2aAccountReference());
        Consents consent = jsonReader.getObjectFromFile("json/ConsentsAvailableAccountsWithBalances.json", Consents.class);
        CreateConsentReq expected = jsonReader.getObjectFromFile("json/CreateConsentReqAvailableAccountsWithBalances.json", CreateConsentReq.class);
        //When
        CreateConsentReq actual = consentModelMapper.mapToCreateConsentReq(consent, new TppRedirectUri("ok.url", "nok.url"));
        //Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToUpdatePsuData_WithBody() {
        // Given
        UpdateConsentPsuDataReq expected = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-with-password.json",
                                                                        UpdateConsentPsuDataReq.class);
        // When
        UpdateConsentPsuDataReq actual = consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, bodyMap);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToUpdatePsuData_WithEmptyBody() {
        // Given
        UpdateConsentPsuDataReq expected = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-without-password.json",
                                                                        UpdateConsentPsuDataReq.class);
        // When
        UpdateConsentPsuDataReq actual = consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, getEmptyBodyMap());
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToUpdatePsuData_WithoutBody() {
        // Given
        UpdateConsentPsuDataReq expected = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-without-password.json",
                                                                        UpdateConsentPsuDataReq.class);
        // When
        UpdateConsentPsuDataReq actual = consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, null);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToPisUpdatePsuData_WithBody() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest expected = jsonReader.getObjectFromFile("json/service/mapper/update-payment-psu-data-with-password.json",
                                                                                         Xs2aUpdatePisCommonPaymentPsuDataRequest.class);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataRequest actual = consentModelMapper
                                                              .mapToPisUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT, bodyMap);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToPisUpdatePsuData_WithEmptyBody() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest expected = jsonReader.getObjectFromFile("json/service/mapper/update-payment-psu-data-without-password.json",
                                                                                         Xs2aUpdatePisCommonPaymentPsuDataRequest.class);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataRequest actual = consentModelMapper
                                                              .mapToPisUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT, getEmptyBodyMap());
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToPisUpdatePsuData_WithoutBody() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest expected = jsonReader.getObjectFromFile("json/service/mapper/update-payment-psu-data-without-password.json",
                                                                                         Xs2aUpdatePisCommonPaymentPsuDataRequest.class);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataRequest actual = consentModelMapper
                                                              .mapToPisUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT, null);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToConsentStatusResponse200_WithCorrectInput() {
        // Given
        ConsentStatusResponse inputData = new ConsentStatusResponse(ConsentStatus.RECEIVED);
        ConsentStatusResponse200 expected = jsonReader.getObjectFromFile("json/service/mapper/consent-status-response-200.json",
                                                                         ConsentStatusResponse200.class);
        // When
        ConsentStatusResponse200 actual = consentModelMapper.mapToConsentStatusResponse200(inputData);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToConsentStatusResponse200_withPartiallyAuthorised() {
        // Given
        ConsentStatusResponse inputData = new ConsentStatusResponse(ConsentStatus.PARTIALLY_AUTHORISED);
        ConsentStatusResponse200 expected = jsonReader.getObjectFromFile("json/service/mapper/consent/consent-status-response-200-partially-authorised.json",
                                                                         ConsentStatusResponse200.class);

        // When
        ConsentStatusResponse200 actual = consentModelMapper.mapToConsentStatusResponse200(inputData);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToConsentStatusResponse200_WithNull() {
        // When
        ConsentStatusResponse200 actual = consentModelMapper.mapToConsentStatusResponse200(null);
        // Then
        assertNull(actual);
    }

    @Test
    public void mapToConsentInformationResponse200Json_WithCorrectInput() {
        // Given
        Xs2aAccountAccess accountAccess = createAccountAccess();
        AccountConsent accountConsent = createConsent(accountAccess);
        ConsentInformationResponse200Json expected = jsonReader.getObjectFromFile("json/service/mapper/consent-information-response-200-json.json",
                                                                                  ConsentInformationResponse200Json.class);
        // When
        ConsentInformationResponse200Json actual = consentModelMapper.mapToConsentInformationResponse200Json(accountConsent);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void mapToConsentInformationResponse200Json_WithNull() {
        // When
        ConsentInformationResponse200Json actual = consentModelMapper.mapToConsentInformationResponse200Json(null);
        // Then
        assertNull(actual);
    }

    private void checkCommonFields(ConsentsResponse201 actual) {
        assertNotNull(actual);
        assertEquals(CONSENT_STATUS, actual.getConsentStatus().toString());
        assertEquals(CONSENT_ID, actual.getConsentId());
        assertEquals(PSU_MESSAGE, actual.getPsuMessage());
        assertFalse(actual.getLinks().isEmpty());

        assertNotNull(actual.getLinks().get(SELF_LINK));
        HrefType selfMap = (HrefType) actual.getLinks().get(SELF_LINK);
        assertEquals(LOCALHOST_LINK, selfMap.getHref());
    }

    private Map<String, HrefType> buildLinks() {
        return Collections.singletonMap(SELF_LINK, new HrefType(LOCALHOST_LINK));
    }

    private Xs2aCreatePisCancellationAuthorisationResponse buildXs2aCreatePisCancellationAuthorisationResponse() {
        return new Xs2aCreatePisCancellationAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, PaymentType.SINGLE, null);
    }

    private Map getBodyMap() {
        Map value = new LinkedHashMap();
        value.put("password", "12345");
        Map bodyMap = new LinkedHashMap();
        bodyMap.put("psuData", value);
        return bodyMap;
    }

    private Map getEmptyBodyMap() {
        return new LinkedHashMap();
    }

    private AccountConsent createConsent(Xs2aAccountAccess access) {
        return new AccountConsent(CONSENT_ID, access, access, false, LocalDate.of(2019, 8, 22), 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap());
    }

    private TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    private static de.adorsys.psd2.model.AccountReference buildAccountReferenceWithoutIds() {
        de.adorsys.psd2.model.AccountReference accountReference = new de.adorsys.psd2.model.AccountReference();
        accountReference.setIban(IBAN);
        accountReference.setBban(BBAN);
        accountReference.setPan(PAN);
        accountReference.setMaskedPan(MASKED_PAN);
        accountReference.setMsisdn(MSISDN);
        accountReference.setCurrency(EUR_CURRENCY.toString());
        return accountReference;
    }

    private static Xs2aAccountAccess createAccountAccess() {
        AccountReference accountReference = buildXs2aAccountReference();
        return new Xs2aAccountAccess(Collections.singletonList(accountReference), Collections.singletonList(accountReference), Collections.singletonList(accountReference), null, null, null);
    }
}
