package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded.AisScaAuthenticatedStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DecoupledAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final Xs2aAuthorisationSubResources AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = buildAccountConsentAuthorization();
    private static final UpdateConsentPsuDataReq UPDATE_CONSENT_PSU_DATA = new UpdateConsentPsuDataReq();
    private static final UpdateConsentPsuDataResponse UPDATE_CONSENT_PSU_DATA_RESPONSE = buildUpdateConsentPsuDataResponse();
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null);
    private static final AccountConsent ACCOUNT_CONSENT = builderAccountConsent();
    private static final AccountConsent ACCOUNT_CONSENT_NULL = null;
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildConsentAuthResponse();
    private static final String SOME_STRING = "";

    @InjectMocks
    private DecoupledAisAuthorizationService decoupledAisAuthorizationService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private AisScaAuthenticatedStage aisScaAuthenticatedStage;

    @Test
    public void createConsentAuthorization_success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(ACCOUNT_CONSENT);
        when(aisConsentService.createAisConsentAuthorization(CONSENT_ID, ScaStatus.STARTED, PSU_ID_DATA))
            .thenReturn(Optional.of(SOME_STRING));
        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID);
        // Then
        assertThat(actualResponse).isEqualTo(Optional.of(CREATE_CONSENT_AUTHORIZATION_RESPONSE));
    }

    @Test
    public void createConsentAuthorization_consentIsNULL_success() {
        // Given
        String str = "";
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(ACCOUNT_CONSENT_NULL);
        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID);
        // Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void updateConsentPsuData_success() {
        // Given

        when(scaStageAuthorisationFactory.getService(any(String.class)))
            .thenReturn(aisScaAuthenticatedStage);
        when(aisScaAuthenticatedStage.apply(UPDATE_CONSENT_PSU_DATA))
            .thenReturn(UPDATE_CONSENT_PSU_DATA_RESPONSE);
        when(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(UPDATE_CONSENT_PSU_DATA_RESPONSE, UPDATE_CONSENT_PSU_DATA))
            .thenReturn(UPDATE_CONSENT_PSU_DATA);

        // When
        UpdateConsentPsuDataResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(UPDATE_CONSENT_PSU_DATA, ACCOUNT_CONSENT_AUTHORIZATION);
        // Then
        assertThat(actualResponse).isEqualTo(UPDATE_CONSENT_PSU_DATA_RESPONSE);
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID)).thenReturn(ACCOUNT_CONSENT_AUTHORIZATION);
        // When
        AccountConsentAuthorization actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);
        // Then
        assertThat(actualResponse).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    public void getAccountConsentAuthorizationById_fail() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID)).thenReturn(null);
        // When
        AccountConsentAuthorization actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);
        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        // Given
        when(aisConsentService.getAuthorisationSubResources(CONSENT_ID)).thenReturn(Optional.of(STRING_LIST));
        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = decoupledAisAuthorizationService.getAuthorisationSubResources(CONSENT_ID);
        // Then
        assertThat(actualResponse).isEqualTo(Optional.of(AUTHORISATION_SUB_RESOURCES));
    }

    @Test
    public void getAuthorisationSubResources_fail() {
        // Given
        when(aisConsentService.getAuthorisationSubResources(CONSENT_ID)).thenReturn(Optional.empty());
        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = decoupledAisAuthorizationService.getAuthorisationSubResources(CONSENT_ID);
        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID)).thenReturn(Optional.of(SCA_STATUS));
        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        // Then
        assertThat(actualResponse).isEqualTo(Optional.of(SCA_STATUS));
    }

    @Test
    public void getAuthorisationScaStatus_fail() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID)).thenReturn(Optional.empty());
        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType_test() {
        //When
        ScaApproach actualResponse = decoupledAisAuthorizationService.getScaApproachServiceType();
        //Then
        assertThat(actualResponse).isNotNull();
    }

    private static AccountConsentAuthorization buildAccountConsentAuthorization() {
        AccountConsentAuthorization accountConsentAuthorization = new AccountConsentAuthorization();
        accountConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        return accountConsentAuthorization;
    }

    private static UpdateConsentPsuDataResponse buildUpdateConsentPsuDataResponse() {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setScaStatus(ScaStatus.RECEIVED);
        return response;
    }

    private static AccountConsent builderAccountConsent() {
        List<AccountReference> list = Collections.singletonList(new AccountReference());
        AccountConsent accountConsent = new AccountConsent(CONSENT_ID,
                                                    new Xs2aAccountAccess(list, list, list,null,null),
                                                    false, null, 1,
                                                null, null,false,
                                            false, null, null, null);
        return accountConsent;
    }

    private static CreateConsentAuthorizationResponse buildConsentAuthResponse() {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorizationId("");
        response.setScaStatus(ScaStatus.STARTED);
        response.setResponseLinkType(ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION);
        return response;
    }
}
