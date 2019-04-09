package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded.AisScaAuthenticatedStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DecoupledAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = buildAccountConsentAuthorization();
    private static final ScaStatus SCA_STATUS = ScaStatus.STARTED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final AccountConsent ACCOUNT_CONSENT = buildConsent(CONSENT_ID);
    private static final Xs2aAuthorisationSubResources AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthorizationResponse();
    private static final UpdateConsentPsuDataReq UPDATE_CONSENT_PSU_DATA_REQ = new UpdateConsentPsuDataReq();
    private static final UpdateConsentPsuDataResponse UPDATE_CONSENT_PSU_DATA_RESPONSE = new UpdateConsentPsuDataResponse();

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
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(ACCOUNT_CONSENT));
        when(aisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA))
            .thenReturn(Optional.of(AUTHORISATION_ID));

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(CREATE_CONSENT_AUTHORIZATION_RESPONSE);
    }

    @Test
    public void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateConsentPsuData_success() {
        // Given
        when(scaStageAuthorisationFactory.getService(SERVICE_PREFIX + SEPARATOR + SCA_APPROACH.name() + SEPARATOR + ACCOUNT_CONSENT_AUTHORIZATION.getScaStatus().name()))
            .thenReturn(aisScaAuthenticatedStage);
        when(aisScaAuthenticatedStage.apply(UPDATE_CONSENT_PSU_DATA_REQ))
            .thenReturn(UPDATE_CONSENT_PSU_DATA_RESPONSE);

        // When
        UpdateConsentPsuDataResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(UPDATE_CONSENT_PSU_DATA_REQ, ACCOUNT_CONSENT_AUTHORIZATION);

        // Then
        assertThat(actualResponse).isEqualTo(UPDATE_CONSENT_PSU_DATA_RESPONSE);
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(ACCOUNT_CONSENT_AUTHORIZATION));

        // When
        Optional<AccountConsentAuthorization> actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    public void getAccountConsentAuthorizationById_wrongIds_fail() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID, WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<AccountConsentAuthorization> actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        // Given
        when(aisConsentService.getAuthorisationSubResources(CONSENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = decoupledAisAuthorizationService.getAuthorisationSubResources(CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(AUTHORISATION_SUB_RESOURCES);
    }

    @Test
    public void getAuthorisationSubResources_wrongConsentId_fail() {
        // Given
        when(aisConsentService.getAuthorisationSubResources(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = decoupledAisAuthorizationService.getAuthorisationSubResources(WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getAuthorisationScaStatus_wrongIds_fail() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType_success() {
        // When
        ScaApproach actualResponse = decoupledAisAuthorizationService.getScaApproachServiceType();

        // Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    private static CreateConsentAuthorizationResponse buildCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();
        resp.setConsentId(CONSENT_ID);
        resp.setAuthorizationId(AUTHORISATION_ID);
        resp.setScaStatus(ScaStatus.STARTED);
        resp.setResponseLinkType(ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION);
        return resp;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static AccountConsentAuthorization buildAccountConsentAuthorization() {
        AccountConsentAuthorization accountConsentAuthorization = new AccountConsentAuthorization();
        accountConsentAuthorization.setScaStatus(ScaStatus.STARTED);
        return accountConsentAuthorization;
    }

    private static AccountConsent buildConsent(String id) {
        return  new AccountConsent(id, new Xs2aAccountAccess(null, null, null, null, null), false, LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false, null, null, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), 0);
    }
}
