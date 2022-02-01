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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthorisationMapperTest {

    private static final String SELF_LINK = "self";
    private static final String LOCALHOST_LINK = "http://localhost";
    private static final String CONSENT_ID = "DW99bwBsY6gXXCzmJaSfzfS3HcA1mC8hy4FkCUo6eBDIki1Qpmw2skEbBFt-CYBr0b4qJhJu5iOkq15arw2rnNWFnjze07vwpAgFM45MlQk=_=_psGLvQpt9Q";
    private static final String AUTHORISATION_ID = "a1";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String ENCRYPTED_PASSWORD = "csdfgsdfg";

    private final JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AuthorisationMapper mapper;
    @Mock
    private HrefLinkMapper hrefLinkMapper;
    @Mock
    private ScaMethodsMapper scaMethodsMapper;
    @Mock
    private CoreObjectsMapper coreObjectsMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private AuthorisationModelMapper authorisationModelMapper;
    @Mock
    private TppMessageGenericMapper tppMessageGenericMapper;
    @Mock
    private AmountModelMapper amountModelMapper;

    @Test
    void mapToAuthorisations_equals_success() {
        // given
        Authorisations expectedAuthorisations = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/Authorisations.json", Authorisations.class);
        Xs2aAuthorisationSubResources xs2AAuthorisationSubResources = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/Xs2aAutorisationSubResources.json", Xs2aAuthorisationSubResources.class);

        // when
        Authorisations actualAuthorisations = mapper.mapToAuthorisations(xs2AAuthorisationSubResources);

        // then
        assertEquals(expectedAuthorisations, actualAuthorisations);
    }

    @Test
    void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aCreatePisAuthorisationResponse() {
        // given
        StartScaprocessResponse expectedStartScaProcessResponse = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/StartScaProcessResponse-expected.json", StartScaprocessResponse.class);

        Xs2aCreatePisAuthorisationResponse xs2aCreatePisAuthorisationResponse = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/StartScaProcessResponse-ResponseObject.json", Xs2aCreatePisAuthorisationResponse.class);
        ResponseObject<Xs2aCreatePisAuthorisationResponse> responseObject = ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                                                                                .body(xs2aCreatePisAuthorisationResponse)
                                                                                .build();
        when(authorisationModelMapper.mapToStartScaProcessResponse(xs2aCreatePisAuthorisationResponse))
            .thenReturn(expectedStartScaProcessResponse);

        // when
        StartScaprocessResponse actualStartScaProcessResponse = (StartScaprocessResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertEquals(expectedStartScaProcessResponse, actualStartScaProcessResponse);
        verify(authorisationModelMapper).mapToStartScaProcessResponse(xs2aCreatePisAuthorisationResponse);
    }

    @Test
    void mapToPisCreateOrUpdateAuthorisationResponse_for_nullBody() {
        // given
        ResponseObject<String> responseObjectNullBody = ResponseObject.<String>builder()
            .body(null)
            .build();

        // when
        StartScaprocessResponse actualStartScaProcessResponse = (StartScaprocessResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObjectNullBody);

        // then
        assertNull(actualStartScaProcessResponse);
    }

    @Test
    void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aCreatePisAuthorisationResponse_otherConditionBody() {
        // given
        ResponseObject<String> responseObject = ResponseObject.<String>builder()
            .body("")
            .build();

        // when
        StartScaprocessResponse actualStartScaProcessResponse = (StartScaprocessResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertNull(actualStartScaProcessResponse);
    }

    @Test
    void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aUpdatePisCommonPaymentPsuDataResponse() {
        //Given
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());

        UpdatePsuAuthenticationResponse expectedUpdatePsuAuthenticationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/UpdatePsuAuthenticationResponse-expected.json", UpdatePsuAuthenticationResponse.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aUpdatePisCommonPaymentPsuDataResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/UpdatePsuAuthenticationResponse-ResponseObject.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .body(xs2aUpdatePisCommonPaymentPsuDataResponse)
                                                                                       .build();

        List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject> xs2aScaMethods = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/scaMethods.json", new TypeReference<List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject>>() {
        });
        ScaMethods scaMethods = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/scaMethods.json", ScaMethods.class);
        when(scaMethodsMapper.mapToScaMethods(xs2aScaMethods)).thenReturn(scaMethods);

        ChallengeData challengeData = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/challengeData.json", ChallengeData.class);
        de.adorsys.psd2.xs2a.core.sca.ChallengeData xs2aChallengeData = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/challengeData.json", de.adorsys.psd2.xs2a.core.sca.ChallengeData.class);
        when(coreObjectsMapper.mapToChallengeData(xs2aChallengeData)).thenReturn(challengeData);

        //When
        UpdatePsuAuthenticationResponse actualUpdatePsuAuthenticationResponse =
            (UpdatePsuAuthenticationResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        //Then
        assertNotNull(actualUpdatePsuAuthenticationResponse.getLinks());
        assertFalse(actualUpdatePsuAuthenticationResponse.getLinks().isEmpty());

        assertFalse(actualUpdatePsuAuthenticationResponse.getScaMethods().isEmpty());

        assertEquals(expectedUpdatePsuAuthenticationResponse.getChosenScaMethod(), actualUpdatePsuAuthenticationResponse.getChosenScaMethod());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getPsuMessage(), actualUpdatePsuAuthenticationResponse.getPsuMessage());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getAuthorisationId(), actualUpdatePsuAuthenticationResponse.getAuthorisationId());
        assertNotNull(actualUpdatePsuAuthenticationResponse.getChallengeData());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getScaStatus(), actualUpdatePsuAuthenticationResponse.getScaStatus());
    }

    @Test
    void mapToAisCreateOrUpdateAuthorisationResponse_for_CreateConsentAuthorizationResponse() {
        // Given
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/CreateConsentAuthorisationResponse.json", CreateConsentAuthorizationResponse.class);
        StartScaprocessResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/start-scaprocess-response-expected.json", StartScaprocessResponse.class);

        when(authorisationModelMapper.mapToStartScaProcessResponse(createConsentAuthorizationResponse))
            .thenReturn(expected);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(createConsentAuthorizationResponse)
                                                                   .build();
        // When
        StartScaprocessResponse actualStartScaProcessResponse =
            (StartScaprocessResponse) mapper.mapToConsentCreateOrUpdateAuthorisationResponse(responseObject);

        // Then
        assertEquals(expected, actualStartScaProcessResponse);
        verify(authorisationModelMapper).mapToStartScaProcessResponse(createConsentAuthorizationResponse);
    }

    @Test
    void mapToAisCreateOrUpdateAuthorisationResponse_for_UpdateConsentPsuDataResponse() {
        // Given
        UpdatePsuAuthenticationResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/update-psu-authentication-response-expected.json", UpdatePsuAuthenticationResponse.class);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
            .body(getTestUpdateConsentPsuDataResponse())
            .build();
        // When
        UpdatePsuAuthenticationResponse actualProcessResponse =
            (UpdatePsuAuthenticationResponse) mapper.mapToConsentCreateOrUpdateAuthorisationResponse(responseObject);

        // Then
        assertEquals(expected, actualProcessResponse);
        verify(authorisationModelMapper, never()).mapToStartScaProcessResponse(any(CreateConsentAuthorizationResponse.class));
    }

    @Test
    void mapToAisCreateOrUpdateAuthorisationResponse_forNullBody() {
        // Given
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
            .body(null)
            .build();
        // When
        StartScaprocessResponse actualStartScaProcessResponse =
            (StartScaprocessResponse) mapper.mapToConsentCreateOrUpdateAuthorisationResponse(responseObject);

        // Then
        assertNull(actualStartScaProcessResponse);
    }

    @Test
    void mapToAisCreateOrUpdateAuthorisationResponse_forOtherConditionBody() {
        // Given
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
            .body(new AuthorisationProcessorResponse())
            .build();
        // When
        StartScaprocessResponse actualStartScaProcessResponse =
            (StartScaprocessResponse) mapper.mapToConsentCreateOrUpdateAuthorisationResponse(responseObject);

        // Then
        assertNull(actualStartScaProcessResponse);
    }

    @Test
    void mapToScaStatusResponse() {
        Xs2aScaStatusResponse xs2aScaStatusResponse = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/Xs2aScaStatusResponse.json", Xs2aScaStatusResponse.class);
        ScaStatusResponse expected = jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/ScaStatusResponse-expected.json", ScaStatusResponse.class);

        when(hrefLinkMapper.mapToLinksMap(any())).thenReturn(null);
        when(tppMessageGenericMapper.mapToTppMessageGenericList(any())).thenReturn(null);

        ScaStatusResponse actual = mapper.mapToScaStatusResponse(xs2aScaStatusResponse);
        assertEquals(expected, actual);
    }

    @Test
    void mapToPasswordFromBody_nullBody() {
        String actualPassword = mapper.mapToPasswordFromBody(null);
        assertNull(actualPassword);
    }

    @Test
    void mapToPasswordFromBody_emptyBody() {
        String actualPassword = mapper.mapToPasswordFromBody(new HashMap<String, String>());
        assertNull(actualPassword);
    }

    @Test
    void mapToPasswordFromBody_validBody() {
        //When
        String actualPassword = mapper.mapToPasswordFromBody(getTestValidBody());
        //Then
        assertEquals(TEST_PASSWORD, actualPassword);
    }

    @Test
    void mapToXs2aCreatePisAuthorisationRequest_validBody() {
        //Given
        String paymentProduct = "1111111";
        String paymentId = "payment id";
        Xs2aCreatePisAuthorisationRequest expected =
            new Xs2aCreatePisAuthorisationRequest(paymentId, new PsuIdData(), paymentProduct, null, TEST_PASSWORD);
        //When
        Xs2aCreatePisAuthorisationRequest actual =
            mapper.mapToXs2aCreatePisAuthorisationRequest(new PsuIdData(), paymentId, null, paymentProduct, getTestValidBody());
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToPisUpdatePsuAuthenticationResponse_responseXs2aCurrencyConversionInfo_notNull() {
        // given
        UpdatePsuAuthenticationResponse expectedUpdatePsuAuthenticationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/update-psu-auth-response-expected.json", UpdatePsuAuthenticationResponse.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aUpdatePisCommonPaymentPsuDataResponse =
            jsonReader.getObjectFromFile("json/service/mapper/authorisation-mapper/update-psu-authentication-response-xs2acurrencyconversoninfo.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
            .body(xs2aUpdatePisCommonPaymentPsuDataResponse)
            .build();

        // when
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(null);
        when(scaMethodsMapper.mapToScaMethods(any())).thenReturn(null);
        when(coreObjectsMapper.mapToChallengeData(any())).thenReturn(null);
        when(amountModelMapper.mapToAmount(responseObject.getBody().getXs2aCurrencyConversionInfo().getTransactionFees()))
            .thenReturn(new Amount().currency("EUR").amount("123"));
        when(amountModelMapper.mapToAmount(responseObject.getBody().getXs2aCurrencyConversionInfo().getCurrencyConversionFees()))
            .thenReturn(new Amount().currency("USD").amount("234"));
        when(amountModelMapper.mapToAmount(responseObject.getBody().getXs2aCurrencyConversionInfo().getEstimatedTotalAmount()))
            .thenReturn(new Amount().currency("UAH").amount("345"));
        when(amountModelMapper.mapToAmount(responseObject.getBody().getXs2aCurrencyConversionInfo().getEstimatedInterbankSettlementAmount()))
            .thenReturn(new Amount().currency("GBP").amount("456"));
        UpdatePsuAuthenticationResponse actualUpdatePsuAuthenticationResponse =
            (UpdatePsuAuthenticationResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        //Then
        assertEquals(expectedUpdatePsuAuthenticationResponse, actualUpdatePsuAuthenticationResponse);
    }

    private Map<String, HrefType> buildLinks() {
        return Collections.singletonMap(SELF_LINK, new HrefType(LOCALHOST_LINK));
    }

    private UpdateConsentPsuDataResponse getTestUpdateConsentPsuDataResponse() {
        UpdateConsentPsuDataResponse updateConsentPsuDataResponse =
            new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED,
                CONSENT_ID,
                AUTHORISATION_ID,
                new PsuIdData(null, null, null, null, null));
        updateConsentPsuDataResponse.setChosenScaMethod(getTestAuthObject());
        updateConsentPsuDataResponse.setChallengeData(getTestChallengeData());
        updateConsentPsuDataResponse.setAvailableScaMethods(List.of(getTestSmsOtp(), getTestPushOtp(), getTestFlashOtp()));
        updateConsentPsuDataResponse.setPsuMessage("some message");
        updateConsentPsuDataResponse.setLinks(getTestLinks());
        return updateConsentPsuDataResponse;
    }

    private AuthenticationObject getTestSmsOtp() {
        AuthenticationObject smsOtp = new AuthenticationObject();
        smsOtp.setAuthenticationType("SMS_OTP");
        smsOtp.setAuthenticationVersion("1");
        smsOtp.setAuthenticationMethodId("sms");
        smsOtp.setName("sms_name");
        smsOtp.setExplanation("sms explanation");
        return smsOtp;
    }

    private AuthenticationObject getTestPushOtp() {
        AuthenticationObject pushOtp = new AuthenticationObject();
        pushOtp.setAuthenticationType("PUSH_OTP");
        pushOtp.setAuthenticationVersion("1");
        pushOtp.setAuthenticationMethodId("push");
        pushOtp.setName("mobile");
        pushOtp.setExplanation("push explanation");
        return pushOtp;
    }

    private AuthenticationObject getTestFlashOtp() {
        AuthenticationObject flash = new AuthenticationObject();
        flash.setAuthenticationType("FLASH");
        flash.setAuthenticationVersion("1");
        flash.setAuthenticationMethodId("card");
        flash.setName("flash-card");
        flash.setExplanation("flash-card explanation");
        return flash;
    }

    private de.adorsys.psd2.xs2a.core.sca.ChallengeData getTestChallengeData() {
        de.adorsys.psd2.xs2a.core.sca.ChallengeData challengeData = new de.adorsys.psd2.xs2a.core.sca.ChallengeData();
        challengeData.setImage(null);
        challengeData.setData(List.of("some data"));
        challengeData.setImageLink("some link");
        challengeData.setOtpMaxLength(1);
        challengeData.setOtpFormat(null);
        challengeData.setAdditionalInformation("some info");
        return challengeData;
    }

    private AuthenticationObject getTestAuthObject() {
        AuthenticationObject authObject = new AuthenticationObject();
        authObject.setAuthenticationType("FLASH");
        authObject.setAuthenticationVersion("1");
        authObject.setAuthenticationMethodId("card");
        authObject.setName("flash-card");
        authObject.setExplanation("flash-card explanation");
        return authObject;
    }

    private Links getTestLinks() {
        Links links = new Links();
        links.setSelf(new HrefType(LOCALHOST_LINK));
        return links;
    }

    private Map<String, Object> getTestValidBody() {
        Map<String, Object> validBody = new HashMap<>();
        Map<String, String> psuDataMap = new LinkedHashMap<>();
        psuDataMap.put("password", TEST_PASSWORD);
        psuDataMap.put("encrypted_password", ENCRYPTED_PASSWORD);
        validBody.put("psuData", psuDataMap);
        return validBody;
    }
}
