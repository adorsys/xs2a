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

package de.adorsys.psd2.xs2a.web.mapper;


import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentModelMapperTest {

    private final static String CONSENT_STATUS = "received";
    private final static String CONSENT_ID = "S7tlYXaar8j7l5IMK89iNJB8SkG5ricoOaEYHyku_AO9BF6MIP29SN_tXtDvaQb3c8b_NsohCWlFlYN0ds8u89WFnjze07vwpAgFM45MlQk=_=_psGLvQpt9Q";
    private final static String AUTHORISATION_ID = "authorisation ID";
    private final static String PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";
    private final static String SELF_LINK = "self";
    private final static String LOCALHOST_LINK = "http://localhost";
    private final static String PAYMENT_PRODUCT = "sepa-credit-transfers";

    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String ACCOUNT_ID = "accountId";
    private static final String IBAN = "DE62500105179972514662";
    private static final String BBAN = "89370400440532010000";
    private static final String PAN = "5254120000003241";
    private static final String MASKED_PAN = "525412******3241";
    private static final String MSISDN = "Test MSISDN";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final String INVALID_DOMAIN_MESSAGE = "TPP URIs are not compliant with the domain secured by the eIDAS QWAC certificate of the TPP in the field CN or SubjectAltName of the certificate";

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
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private TppMessageGenericMapper tppMessageGenericMapper;
    @Mock
    private CoreObjectsMapper coreObjectsMapper;

    private CreateConsentResponse createConsentResponseWithScaMethods;
    private CreateConsentResponse createConsentResponseWithoutScaMethods;
    private JsonReader jsonReader;
    private PsuIdData psuIdData;
    private Map bodyMap;

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReader();
        createConsentResponseWithScaMethods = jsonReader.getObjectFromFile("json/service/mapper/create-consent-response-with-sca-methods.json", CreateConsentResponse.class);
        createConsentResponseWithoutScaMethods = jsonReader.getObjectFromFile("json/service/mapper/create-consent-response-without-sca-methods.json", CreateConsentResponse.class);
        psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        bodyMap = getBodyMap();
    }

    @Test
    void mapToConsentsResponse201_withScaMethods_shouldReturnArrayOfThem() {
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
    void mapToConsentsResponse201_withScaMethods_withWarnings() {
        // Given
        ScaMethods methods = new ScaMethods();
        methods.add(new AuthenticationObject());
        when(scaMethodsMapper.mapToScaMethods(anyList())).thenReturn(methods);
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());
        createConsentResponseWithScaMethods.getTppMessageInformation().addAll((Collections.singleton(TppMessageInformation.buildWarning(INVALID_DOMAIN_MESSAGE))));

        // When
        ConsentsResponse201 actual = consentModelMapper.mapToConsentsResponse201(createConsentResponseWithScaMethods);

        // Then
        checkCommonFields(actual);
        assertFalse(actual.getScaMethods().isEmpty());
    }

    @Test
    void mapToConsentsResponse201_withoutScaMethods_shouldNotReturnEmptyArray() {
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());

        // When
        ConsentsResponse201 actual = consentModelMapper.mapToConsentsResponse201(createConsentResponseWithoutScaMethods);

        // Then
        checkCommonFields(actual);
        assertNull(actual.getScaMethods());
    }

    @Test
    void mapToCancellations() {
        //Given
        List<String> authorisationIds = Arrays.asList("c0121ca2-ab3a-4564-b915-6e40e8b40f50", "743d0a45-7233-4fbf-9799-c657f327836c");
        //When
        Authorisations authorisations = consentModelMapper.mapToAuthorisations(new Xs2aPaymentCancellationAuthorisationSubResource(authorisationIds));
        //Then
        assertNotNull(authorisations);
        AuthorisationsList authorisationsList = authorisations.getAuthorisationIds();
        assertNotNull(authorisationsList);
        assertEquals(authorisationIds, new ArrayList<>(authorisationsList));
    }

    @Test
    void mapToCreateConsentReq_AvailableAccountsWithBalance() {
        //Given
        when(xs2aObjectMapper.convertValue(buildAccountReferenceWithoutIds(), AccountReference.class)).thenReturn(buildXs2aAccountReference());
        Consents consent = jsonReader.getObjectFromFile("json/ConsentsAvailableAccountsWithBalances.json", Consents.class);
        CreateConsentReq expected = jsonReader.getObjectFromFile("json/CreateConsentReqAvailableAccountsWithBalances.json", CreateConsentReq.class);
        //When
        CreateConsentReq actual = consentModelMapper.mapToCreateConsentReq(consent, new TppRedirectUri("ok.url", "nok.url"), null, null, null);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToCreateConsentReq_AdditionalAccountInformation() {
        //Given
        when(xs2aObjectMapper.convertValue(buildAccountReferenceWithoutIds(), AccountReference.class)).thenReturn(buildXs2aAccountReference());
        when(xs2aObjectMapper.convertValue(buildAdditionalInformationAccountReference(), AccountReference.class)).thenReturn(buildAdditionalInformationXs2aAccountReference());
        Consents consent = jsonReader.getObjectFromFile("json/ConsentsAdditionalAccountInformation.json", Consents.class);
        CreateConsentReq expected = jsonReader.getObjectFromFile("json/CreateConsentReqAdditionalAccountInformation.json", CreateConsentReq.class);
        //When
        CreateConsentReq actual = consentModelMapper.mapToCreateConsentReq(consent, new TppRedirectUri("ok.url", "nok.url"), null, null, null);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToCreateConsentReq_AdditionalAccountInformationOwnerNameEmpty() {
        //Given
        when(xs2aObjectMapper.convertValue(buildAccountReferenceWithoutIds(), AccountReference.class)).thenReturn(buildXs2aAccountReference());
        Consents consent = jsonReader.getObjectFromFile("json/ConsentsAdditionalAccountInformation.json", Consents.class);
        consent.getAccess().getAdditionalInformation().setOwnerName(Collections.emptyList());
        CreateConsentReq expected = jsonReader.getObjectFromFile("json/CreateConsentReqAdditionalAccountInformationOwnerNameEmpty.json", CreateConsentReq.class);
        //When
        CreateConsentReq actual = consentModelMapper.mapToCreateConsentReq(consent, new TppRedirectUri("ok.url", "nok.url"), null, null, null);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToCreateConsentReq_TppBrandLoggingInformation() {
        //Given
        when(xs2aObjectMapper.convertValue(buildAccountReferenceWithoutIds(), AccountReference.class)).thenReturn(buildXs2aAccountReference());
        Consents consent = jsonReader.getObjectFromFile("json/ConsentsAvailableAccountsWithBalances.json", Consents.class);
        CreateConsentReq expected = jsonReader.getObjectFromFile("json/CreateConsentReqTppBrandLoggingInformation.json", CreateConsentReq.class);
        //When
        CreateConsentReq actual = consentModelMapper.mapToCreateConsentReq(consent, new TppRedirectUri("ok.url", "nok.url"), null, "tppBrandLoggingInformation", null);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToUpdatePsuData_WithBody() {
        // Given
        ConsentAuthorisationsParameters expected = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-with-password.json",
                                                                                ConsentAuthorisationsParameters.class);
        // When
        ConsentAuthorisationsParameters actual = consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, bodyMap);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToUpdatePsuData_WithEmptyBody() {
        // Given
        ConsentAuthorisationsParameters expected = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-without-password.json",
                                                                                ConsentAuthorisationsParameters.class);
        // When
        ConsentAuthorisationsParameters actual = consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, getEmptyBodyMap());
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToUpdatePsuData_WithoutBody() {
        // Given
        ConsentAuthorisationsParameters expected = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-without-password.json",
                                                                                ConsentAuthorisationsParameters.class);
        // When
        ConsentAuthorisationsParameters actual = consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, null);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToPisUpdatePsuData_WithBody() {
        // Given
        PaymentAuthorisationParameters expected = jsonReader.getObjectFromFile("json/service/mapper/update-payment-psu-data-with-password.json",
                                                                               PaymentAuthorisationParameters.class);
        // When
        PaymentAuthorisationParameters actual = consentModelMapper
                                                    .mapToPisUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT, bodyMap);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToPisUpdatePsuData_WithEmptyBody() {
        // Given
        PaymentAuthorisationParameters expected = jsonReader.getObjectFromFile("json/service/mapper/update-payment-psu-data-without-password.json",
                                                                               PaymentAuthorisationParameters.class);
        // When
        PaymentAuthorisationParameters actual = consentModelMapper
                                                    .mapToPisUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT, getEmptyBodyMap());
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToPisUpdatePsuData_WithoutBody() {
        // Given
        PaymentAuthorisationParameters expected = jsonReader.getObjectFromFile("json/service/mapper/update-payment-psu-data-without-password.json",
                                                                               PaymentAuthorisationParameters.class);
        // When
        PaymentAuthorisationParameters actual = consentModelMapper
                                                    .mapToPisUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT, null);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToConsentStatusResponse200_WithCorrectInput() {
        // Given
        ConsentStatusResponse inputData = new ConsentStatusResponse(ConsentStatus.RECEIVED, PSU_MESSAGE);
        ConsentStatusResponse200 expected = jsonReader.getObjectFromFile("json/service/mapper/consent-status-response-200.json",
                                                                         ConsentStatusResponse200.class);
        // When
        ConsentStatusResponse200 actual = consentModelMapper.mapToConsentStatusResponse200(inputData);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToConsentStatusResponse200_WithNull() {
        // When
        ConsentStatusResponse200 actual = consentModelMapper.mapToConsentStatusResponse200(null);
        // Then
        assertNull(actual);
    }

    @Test
    void mapToConsentInformationResponse200Json_WithCorrectInput() {
        // Given
        AisConsent accountConsent = getAisConsent();

        ConsentInformationResponse200Json expected = jsonReader.getObjectFromFile("json/service/mapper/consent-information-response-200-json.json",
                                                                                  ConsentInformationResponse200Json.class);

        de.adorsys.psd2.model.AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-reference.json",
                                                                                               de.adorsys.psd2.model.AccountReference.class);
        when(accountModelMapper.mapToAccountReferences(accountConsent.getAccess().getAccounts()))
            .thenReturn(Collections.singletonList(accountReference));


        // When
        ConsentInformationResponse200Json actual = consentModelMapper.mapToConsentInformationResponse200Json(accountConsent);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToConsentInformationResponse200Json_WithNull() {
        // When
        ConsentInformationResponse200Json actual = consentModelMapper.mapToConsentInformationResponse200Json(null);
        // Then
        assertNull(actual);
    }

    @Test
    void mapToAccountAccessDomain_mapToAdditionalInformationAccess_noAdditionalInformationAccess() {
        //Given
        AisConsent aisConsent = getAisConsentWithAdditionalInfoNullValues();
        //When
        ConsentInformationResponse200Json consentInformationResponse = consentModelMapper.mapToConsentInformationResponse200Json(aisConsent);
        //Then
        assertNull(consentInformationResponse.getAccess().getAdditionalInformation());
    }

    @Test
    void mapToAccountAccessDomain_mapToAdditionalInformationAccess_withAdditionalInformationAccess() {
        //Given
        AisConsent aisConsent = getAisConsentWithFullAdditionalInfo();
        AdditionalInformationAccess expected = jsonReader.getObjectFromFile("json/service/mapper/consent-model-mapper/additional-info-expected.json", AdditionalInformationAccess.class);
        de.adorsys.psd2.model.AccountReference accountReference = jsonReader
                                                                      .getObjectFromFile("json/service/mapper/consent-model-mapper/account-references.json", de.adorsys.psd2.model.AccountReference.class);
        //When
        when(accountModelMapper.mapToAccountReferences(any())).thenReturn(List.of(accountReference));
        ConsentInformationResponse200Json consentInformationResponse = consentModelMapper.mapToConsentInformationResponse200Json(aisConsent);
        AdditionalInformationAccess actual = consentInformationResponse.getAccess().getAdditionalInformation();
        //Then
        assertNotNull(actual);
        assertNotNull(actual.getOwnerName());
        assertNotNull(actual.getTrustedBeneficiaries());
        assertEquals(expected, actual);
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

    private Map getBodyMap() {
        Map<String, String> value = new LinkedHashMap<>();
        value.put("password", "12345");
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("psuData", value);
        return bodyMap;
    }

    private Map getEmptyBodyMap() {
        return new LinkedHashMap();
    }

    private AisConsent getAisConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        AisConsentData consentData = AisConsentData.buildDefaultAisConsentData();
        aisConsent.setConsentData(consentData);
        return aisConsent;
    }

    private AisConsent getAisConsentWithFullAdditionalInfo() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/mapper/consent-model-mapper/ais-consent-additional-info.json", AisConsent.class);
        AisConsentData consentData = AisConsentData.buildDefaultAisConsentData();
        aisConsent.setConsentData(consentData);
        return aisConsent;
    }

    private AisConsent getAisConsentWithAdditionalInfoNullValues() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/mapper/consent-model-mapper/ais-consent-additional-info-null-values.json", AisConsent.class);
        AisConsentData consentData = AisConsentData.buildDefaultAisConsentData();
        aisConsent.setConsentData(consentData);
        return aisConsent;
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY, null);
    }

    private static AccountReference buildAdditionalInformationXs2aAccountReference() {
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(IBAN);
        return accountReference;

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

    private static de.adorsys.psd2.model.AccountReference buildAdditionalInformationAccountReference() {
        de.adorsys.psd2.model.AccountReference accountReference = new de.adorsys.psd2.model.AccountReference();
        accountReference.setIban(IBAN);
        return accountReference;
    }
}
