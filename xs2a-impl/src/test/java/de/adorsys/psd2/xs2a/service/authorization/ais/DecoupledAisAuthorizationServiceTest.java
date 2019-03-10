package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
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

import java.time.LocalDate;
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
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = buildAccountConsentAuthorization();
    private static final ScaStatus SCA_STATUS = ScaStatus.STARTED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final AccountConsent ACCOUNT_CONSENT = createConsent(CONSENT_ID);
    private static final Xs2aAuthorisationSubResources AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthorizationResponse();
    private static final UpdateConsentPsuDataReq UPDATE_CONSENT_PSU_DATA_REQ = new UpdateConsentPsuDataReq();
    private static final UpdateConsentPsuDataResponse UPDATE_CONSENT_PSU_DATA_RESPONSE = new UpdateConsentPsuDataResponse();
    private static final AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> AIS_SCA_STAGE = null;

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
        //Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(ACCOUNT_CONSENT);
        when(aisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA))
            .thenReturn(Optional.of(AUTHORISATION_ID));

        //When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, CONSENT_ID);

        //Then
        assertThat(actualResponse.get().getAuthorizationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.get()).isEqualTo(CREATE_CONSENT_AUTHORIZATION_RESPONSE);
    }

    @Test
    public void createConsentAuthorization_failed() {
        //Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(null);

        //When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateConsentPsuData() {
        //Given
        when(scaStageAuthorisationFactory.getService(SERVICE_PREFIX + SEPARATOR + SCA_APPROACH.name() + SEPARATOR + ACCOUNT_CONSENT_AUTHORIZATION.getScaStatus().name()))
            .thenReturn(aisScaAuthenticatedStage);
        when(aisScaAuthenticatedStage.apply(UPDATE_CONSENT_PSU_DATA_REQ))
            .thenReturn(UPDATE_CONSENT_PSU_DATA_RESPONSE);

        //When
        UpdateConsentPsuDataResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(UPDATE_CONSENT_PSU_DATA_REQ, ACCOUNT_CONSENT_AUTHORIZATION);

        //Then
        assertThat(actualResponse).isEqualTo(UPDATE_CONSENT_PSU_DATA_RESPONSE);
    }

    @Test
    public void getAccountConsentAuthorizationById() {
        //Given
        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(ACCOUNT_CONSENT_AUTHORIZATION);

        //When
        AccountConsentAuthorization actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        //Then
        assertThat(actualResponse).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    public void getAuthorisationSubResources_success() {
        //Given
        when(aisConsentService.getAuthorisationSubResources(CONSENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        //When
        Optional<Xs2aAuthorisationSubResources> actualResponse = decoupledAisAuthorizationService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.get()).isEqualTo(AUTHORISATION_SUB_RESOURCES);
    }

    @Test
    public void getAuthorisationSubResources_failed() {
        //Given
        when(aisConsentService.getAuthorisationSubResources(CONSENT_ID))
            .thenReturn(Optional.empty());

        //When
        Optional<Xs2aAuthorisationSubResources> actualResponse = decoupledAisAuthorizationService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        //Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        //When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getAuthorisationScaStatus_failed() {
        //Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        //When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType() {
        //When
        ScaApproach actualResponse = decoupledAisAuthorizationService.getScaApproachServiceType();

        //Then
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

    private static AccountConsent createConsent(String id) {
        return new AccountConsent(id, createEmptyAccountAccess(), false, LocalDate.now(), 4, null, ConsentStatus.VALID, false, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL);
    }

    private static Xs2aAccountAccess createEmptyAccountAccess() {
        return new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES);
    }
}
